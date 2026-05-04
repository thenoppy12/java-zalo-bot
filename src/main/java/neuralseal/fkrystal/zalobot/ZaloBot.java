package neuralseal.fkrystal.zalobot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpServer;
import neuralseal.fkrystal.zalobot.models.*;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;


/**
 * A Zalo bot instance.
 */
public class ZaloBot {
	private final List<Handler> _handlers = new ArrayList<>();
	private final String _token;
	private final String _baseUrlWithToken;
	private final ObjectMapper _mapper;
	private final String _botManagementName;
	private WebhookServerData _webhookServerData;
	private final String _USER_AGENT = "NeuralSeal's Fkrystal zalo-bot v"+Constants.VERSION;
	private HttpServer _webhookServer;
	private final MediaType _JSON = MediaType.get("application/json; charset=utf-8");
    /**
     * Don't create new HttpClient for bot to fetch online contents. Use this internal one for better perfomance!
     */
	public final OkHttpClient client = Constants.sharedOkHttp;
    /**
     * Access {@link ZaloBot.StandaloneAPI} here, don't create new one.
     */
	public final StandaloneAPI api = new StandaloneAPI();

    /**
	 * nah, it's protected.
     * @param token nah, it's protected.
     * @param botManagementName nah, it's protected.
     */
	protected ZaloBot(String token, @Nullable String botManagementName) {
		this._botManagementName = (!Objects.isNull(botManagementName))? botManagementName : "ZaloBot-"+((new Random().nextInt(0, Integer.MAX_VALUE)));
		this._token = token;
		this._baseUrlWithToken = "https://bot-api.zaloplatforms.com/bot"+token;
		this._mapper = new ObjectMapper();
	}

// ------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Start the bot, which just simply start the webhook server.
     */
	public void start() {
		this._startWebhookServer(_webhookServerData);
	}

    /**
     * Stop the bot, which just simply stop the webhook server.
     */
	public void stop() {
		if (this._webhookServer != null) {
			this._webhookServer.stop(0);
			System.out.println("Webhook server stopped.");
		}
	}

	/**
	 * Get the bot's management name, for easier management?
	 * @return User-set bot's management name
	 */
	public String name() {
		return this._botManagementName;
	}

	/**
	 * Get the bot's token.
	 * @return Bot's token.
	 */
	public String token() {
		return this._token;
	}

    /**
	 * Add a {@link Handler} to the bot, like receiving event and dispatcher I guess.
     * @param handler A class implement {@link Handler}
	 * @return Current {@link ZaloBot} instance
     */
	public ZaloBot addHandler(Handler handler) {
		this._handlers.add(handler);
		return this;
	}

    /**
	 * Add a list of {@link Handler} to the bot, like receiving event and dispatcher I guess.
     * @param listOfHandler yes. list of class implement {@link Handler}
	 * @return Current {@link ZaloBot} instance
     */
	public ZaloBot addHandler(List<Handler> listOfHandler) {
		for (Handler handler : listOfHandler) {
			addHandler(handler);
		}
		return this;
	}

    /**
	 * Set the bot's webhook server arguments, like port, webhook path, and webhook secret.
     * @param data {@link WebhookServerData}
	 * @return Current {@link ZaloBot} instance
     */
	public ZaloBot setWebHookServerData(WebhookServerData data) {
		this._webhookServerData = data;
		return this;
	}

    /**
     * Loop the current thread, avoid bot to stop?
     */
	public void keepMeAlivePlease() {
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			System.err.println("Interrupted");
		}
	}

	private void _processUpdate(Update update) {
		for (Handler handler : _handlers) {
			if (handler.checkUpdate(update)) {
				handler.handleUpdate(update, this)
						.exceptionally(ex -> {
							System.err.println("Handler crashed: " + ex.getMessage());
							return null;
						});
				break;
			}
		}
	}

	private boolean over2000(String string) {
		return string.length() > 2000;
	}

// ------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Standalone API mean you can use it separately lmao, but I don't sure about that.<br><br>
	 * Basically all of this is just HTTP requests that reply to user with interaction I guess.
     */
	public class StandaloneAPI {
        /**
		 * Get the bot's user information
         * @return {@link User} of bot
         */
		public CompletableFuture<User> getMe() {
			return _doPost("getMe", null, new TypeReference<>() {});
		}

        /**
		 * Honestly I don't know how to use this "long polling" feature, so I set it to {@link Deprecated} but not for removal because it's still accessible on Zalo I guess.
         * @param timeout what is this?
         * @param offset what is this?
         * @return what is this?
         */
		@Deprecated
		public CompletableFuture<Update> getUpdates(int timeout, @Nullable Integer offset) {
			ObjectNode requestData = _mapper.createObjectNode();
			requestData.put("timeout", timeout);
			if (offset != null) {
				requestData.put("offset", offset);
			}
			RequestBody body = RequestBody.create(requestData.toString(), _JSON);
			return _doPost("getUpdates", body, new TypeReference<>() {});
		}

        /**
		 * Yes, get the bot's configured webhook data. It's contain webhook url and updated timestamp ( no secret btw ).
         * @return {@link Webhook} data of bot
         */
		public CompletableFuture<Webhook> getWebhook() {
			return _doPost("getWebhookInfo", null, new TypeReference<>() {});
		}

        /**
		 * Set webhook url and webhook secret. <br>(Don't confuse with {@link WebhookServerData}, it's for webhook server (receiver), not webhook sender).
         * @param webhookUrl Your webhook server url, normally this API will use ngrok for free
         * @param webhookSecretToken Your webhook secret, for security
         * @return {@code true} if webhook data is sent and edited, {@code false} if not
         */
		public boolean setWebhook(String webhookUrl, String webhookSecretToken) {
			try {
				Webhook info = api.getWebhook().join();
				if (info.url() == null || info.url().isEmpty()) {
					System.out.println("Webhook URL is empty. Setting to: " + webhookUrl);
					ObjectNode requestData = _mapper.createObjectNode();
					requestData.put("url", webhookUrl);
					requestData.put("secret_token", webhookSecretToken);
					RequestBody body = RequestBody.create(requestData.toString(), _JSON);
					return (_doPost("setWebhook", body, new TypeReference<Generic<Webhook>>() {}).join()).url().equals(webhookUrl);
				} else if (!info.url().equals(webhookUrl)) {
					System.out.println("Webhook URL mismatch! Replacing old URL with: " + webhookUrl);
					ObjectNode requestData = _mapper.createObjectNode();
					requestData.put("url", webhookUrl);
					requestData.put("secret_token", webhookSecretToken);
					RequestBody body = RequestBody.create(requestData.toString(), _JSON);
                    return (_doPost("setWebhook", body, new TypeReference<Generic<Webhook>>() {}).join()).url().equals(webhookUrl);
				} else {
					System.out.println("Webhook is already correctly configured to " + webhookUrl);
					return true;
				}
			} catch (Exception e) {
				System.out.println("No existing webhook found (API returned error). Setting it up now...");
				try {
					ObjectNode requestData = _mapper.createObjectNode();
					requestData.put("url", webhookUrl);
					requestData.put("secret_token", webhookSecretToken);
					RequestBody body = RequestBody.create(requestData.toString(), _JSON);
					return (_doPost("setWebhook", body, new TypeReference<Generic<Webhook>>() {}).join()).url().equals(webhookUrl);
				} catch (Exception ex) {
					System.err.println("CRITICAL: Failed to set webhook! " + ex.getMessage());
					return false;
				}
			}
		}

        /**
		 * Like the name of this method.
         * @return {@code true} if webhook data got deleted, {@code false} if not
         */
		public boolean deleteWebhook() {
			return (_doPost("deleteWebhook", null, new TypeReference<Generic<Webhook>>() {}).join()).url().isEmpty();
		}

        /**
         * Send chat message. <br>
         * If the text more than 2000 characters, it fails and return {@code null}<br>
         * For more info about that limit, visit <a href="https://bot.zapps.me/docs/apis/sendMessage/#:~:text=N%E1%BB%99i%20dung%20v%C4%83n%20b%E1%BA%A3n%20c%E1%BB%A7a%20tin%20nh%E1%BA%AFn%20s%E1%BA%BD%20%C4%91%C6%B0%E1%BB%A3c%20g%E1%BB%ADi%2C%20v%E1%BB%9Bi%20%C4%91%E1%BB%99%20d%C3%A0i%20t%E1%BB%AB%201%20%C4%91%E1%BA%BFn%202000%20k%C3%BD%20t%E1%BB%B1">here</a>
         * @param chatId Obtained by using {@code update.message().chat().id()} in your custom {@link Handler} class
         * @param text Text to send.
         * @return {@link Message}, can be ignored or {@code null}, handle it yourself lmao
         */
		public CompletableFuture<Message> sendMessage(String chatId, String text) {
			if (over2000(text)) {
				System.out.println("Text to send, its too long: " + text.length());
				return null;
			}
			ObjectNode requestData = _mapper.createObjectNode();
			requestData.put("chat_id", chatId);
			requestData.put("text", text);
			RequestBody body = RequestBody.create(requestData.toString(), _JSON);
			return _doPost("sendMessage", body, new TypeReference<>() {});
		}

        /**
		 * Send chat status to user (Typing, Uploading photos (still beta))
         * @param chatId Obtained by using {@code update.message().chat().id()} in your custom {@link Handler} class
         * @param action {@link ChatActions}
         * @return Can be ignored.
         */
		public CompletableFuture<Boolean> sendChatAction(String chatId, ChatActions action) {
			ObjectNode requestData = _mapper.createObjectNode();
			requestData.put("chat_id", chatId);
			requestData.put("action", action.getChatAction());
			RequestBody body = RequestBody.create(requestData.toString(), _JSON);
			return _doPost("sendChatAction", body, new TypeReference<>() {});
		}

        /**
		 * Send a photo message (also add a caption if {@code photoCaption} is not {@code null}
		 *  If the text more than 2000 characters, it fails and return {@code null}<br>
		 * For more info about that limit, visit <a href="">here</a>
         * @param chatId Obtained by using {@code update.message().chat().id()} in your custom {@link Handler} class
         * @param photoUrl Direct URL of your photo
         * @param photoCaption A smoll text under the photo, can be null or not more than 2000 characters
         * @return
         */
		@SuppressWarnings("unchecked")
		public CompletableFuture<Message> sendPhoto(String chatId, String photoUrl, @Nullable String photoCaption) {
			ObjectNode requestData = _mapper.createObjectNode();
			requestData.put("chat_id", chatId);
			requestData.put("photo", photoUrl);
			if (photoCaption != null) {
				if (over2000(photoCaption)) {
					System.out.println("photoCaption is exceed 2000 characters, this will be ignored and not append into payload.");
				} else {
					requestData.put("caption", photoCaption);
				}
			}
			RequestBody body = RequestBody.create(requestData.toString(), _JSON);
			return _doPost("sendPhoto", body, new TypeReference<>() {});
		}
		// TODO: Sticker (it's impossible for now lol)
	}

// ------------------------------------------------------------------------------------------------------------------------------------------------------

	private void _startWebhookServer(WebhookServerData serverData) {
		try {
			this._webhookServer = HttpServer.create(new InetSocketAddress(serverData.port()), 0);
			this._webhookServer.createContext(serverData.path(), exchange -> {
                if ("POST".equals(exchange.getRequestMethod())) {
                    if (serverData.expectedSecretToken() != null && !serverData.expectedSecretToken().isEmpty()) {
                        String receivedToken = exchange.getRequestHeaders().getFirst("X-Bot-Api-Secret-Token");
                        if (!serverData.expectedSecretToken().equals(receivedToken)) {
                            System.err.println("SECURITY ALERT: Blocked an unauthorized webhook attempt!");
                            exchange.sendResponseHeaders(401, -1);
                            exchange.close();
                            return;
                        }
                    }
                    try (InputStream is = exchange.getRequestBody()) {
                        String jsonBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                        String response = "{\"ok\":true}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                        CompletableFuture.runAsync(() -> {
                            try {
                                Update update = _mapper.readValue(jsonBody, Update.class);
                                if (update != null) {
                                    _processUpdate(update);
                                }
                            } catch (Exception e) {
                                System.err.println("JSON Parse Error: " + e.getMessage());
                            }
                        });

                    } catch (Exception e) {
                        exchange.sendResponseHeaders(500, -1);
                        exchange.close();
                    }
                } else {
                    exchange.sendResponseHeaders(405, -1);
                    exchange.close();
                }
            });
			this._webhookServer.setExecutor(Executors.newCachedThreadPool());
			this._webhookServer.start();
			System.out.println("Webhook Server listening on port " + serverData.port());
		} catch (IOException e) {
			throw new RuntimeException("Failed to start server", e);
		}
	}

// ------------------------------------------------------------------------------------------------------------------------------------------------------

	private <T> CompletableFuture<T> _doPost(String endpoint, @Nullable RequestBody body, TypeReference<Generic<T>> responseType) {
		Request request = new Request.Builder()
				.url(this._baseUrlWithToken + "/" + endpoint)
				.post(!Objects.isNull(body) ? body : RequestBody.create("{}", _JSON))
				.header("User-Agent", _USER_AGENT)
				.build();
		return _async(request, responseType);
	}

	private <T> CompletableFuture<T> _async(Request req, TypeReference<Generic<T>> rep) {
		CompletableFuture<T> future = new CompletableFuture<>();
		client.newCall(req).enqueue(new Callback() {
			@Override
			public void onFailure(@NotNull Call call, @NotNull IOException e) {future.completeExceptionally(e);}
			@Override
			public void onResponse(@NotNull Call call, @NotNull Response response) {
				try (ResponseBody body = response.body()) {
					if (body == null) {
						future.completeExceptionally(new RuntimeException("Empty body, i don't think it's fine."));
						return;
					}
					Generic<T> apiResponse = _mapper.readValue(body.string(), rep);
					if (apiResponse.ok()) {
						future.complete(apiResponse.result());
					} else {
						future.completeExceptionally(new RuntimeException(
								"API Error " + apiResponse.errorCode() + ": " + apiResponse.description()
						));
					}
				} catch (IOException e) {
					future.completeExceptionally(e);
				}
			}
		});
		return future;
	}
}