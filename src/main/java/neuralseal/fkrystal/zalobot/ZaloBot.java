package neuralseal.fkrystal.zalobot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpServer;
import neuralseal.fkrystal.zalobot.handler.Handler;
import neuralseal.fkrystal.zalobot.models.*;
import neuralseal.fkrystal.zalobot.types.ChatActions;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ZaloBot {
	// TODO [FAKE] : Class's private fields
	private final List<Handler> handlers = new ArrayList<>();
	private final String token;
	private final String baseUrlWithToken;
	private final OkHttpClient client;
	private final ObjectMapper mapper;
	private final String USER_AGENT = "NeuralSeal's Fkrystal zalo-bot v"+Constants.VERSION;
	private HttpServer webhookServer;

	// TODO [FAKE] : Class's public fields
	public final StandaloneAPI api = new StandaloneAPI();

	// TODO [FAKE] : Class's private & static fields
	private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

	protected ZaloBot(String token, @Nullable String botManagementName) {
		Constants.botMap.put((!Objects.isNull(botManagementName))? botManagementName : "ZaloBot-"+(Constants.botMap.size()+1), this);
		this.token = token;
		this.baseUrlWithToken = "https://bot-api.zaloplatforms.com/bot"+token;
		this.client = new OkHttpClient.Builder().readTimeout(65, TimeUnit.SECONDS).build();
		this.mapper = new ObjectMapper();
	}

	public String token() {
		return this.token;
	}

	public void addHandler(Handler handler) {
		this.handlers.add(handler);
	}
	public void addHandler(List<Handler> listOfHandler) {
		for (Handler handler : listOfHandler) {
			addHandler(handler);
		}
	}
	private void processUpdate(Update update) {
		for (Handler handler : handlers) {
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

	public void keepMeAlivePlease() {
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			System.err.println("Interrupted");
		}
	}




	// TODO [FAKE] : API methods
	public class StandaloneAPI {
		// TODO FAKE : User methods
		public CompletableFuture<User> getMe() {return doPost("getMe", null, new TypeReference<>() {});}

		@SuppressWarnings("unchecked")
		public CompletableFuture<Update> getUpdates(int timeout, @Nullable Integer offset) {
			ObjectNode requestData = mapper.createObjectNode();
			requestData.put("timeout", timeout);
			if (offset != null) {
				requestData.put("offset", offset);
			}
			RequestBody body = RequestBody.create(requestData.toString(), JSON);
			return doPost("getUpdates", body, new TypeReference<>() {});
		}

		// TODO [FAKE] : Webhook methods
		public CompletableFuture<Webhook> getWebhook() {return doPost("getWebhookInfo", null, new TypeReference<>() {});}
		public boolean setWebhook(String webhookUrl, String webhookSecretToken) {
			try {
				Webhook info = api.getWebhook().join();
				if (info.url() == null || info.url().isEmpty()) {
					System.out.println("Webhook URL is empty. Setting to: " + webhookUrl);

					ObjectNode requestData = mapper.createObjectNode();
					requestData.put("url", webhookUrl);
					requestData.put("secret_token", webhookSecretToken);
					RequestBody body = RequestBody.create(requestData.toString(), JSON);

					return (doPost("setWebhook", body, new TypeReference<Generic<Webhook>>() {}).join()).url().equals(webhookUrl);
				} else if (!info.url().equals(webhookUrl)) {
					System.out.println("Webhook URL mismatch! Replacing old URL with: " + webhookUrl);

					ObjectNode requestData = mapper.createObjectNode();
					requestData.put("url", webhookUrl);
					requestData.put("secret_token", webhookSecretToken);
					RequestBody body = RequestBody.create(requestData.toString(), JSON);

                    return (doPost("setWebhook", body, new TypeReference<Generic<Webhook>>() {}).join()).url().equals(webhookUrl);
				} else {
					System.out.println("Webhook is already correctly configured to " + webhookUrl);
					return true;
				}

			} catch (Exception e) {
				System.out.println("No existing webhook found (API returned error). Setting it up now...");
				try {
					ObjectNode requestData = mapper.createObjectNode();
					requestData.put("url", webhookUrl);
					requestData.put("secret_token", webhookSecretToken);
					RequestBody body = RequestBody.create(requestData.toString(), JSON);

					return (doPost("setWebhook", body, new TypeReference<Generic<Webhook>>() {}).join()).url().equals(webhookUrl);
				} catch (Exception ex) {
					System.err.println("CRITICAL: Failed to set webhook! " + ex.getMessage());
					return false;
				}
			}
		}
		public boolean deleteWebhook() {
			return (doPost("deleteWebhook", null, new TypeReference<Generic<Webhook>>() {}).join()).url().isEmpty();
		}

		// TODO [FAKE] : Message methods
		public CompletableFuture<Message> sendMessage(String chatId, String text) {
			ObjectNode requestData = mapper.createObjectNode();
			requestData.put("chat_id", chatId);
			requestData.put("text", text);
			RequestBody body = RequestBody.create(requestData.toString(), JSON);
			return doPost("sendMessage", body, new TypeReference<>() {});
		}

		public CompletableFuture<Boolean> sendChatAction(String chatId, ChatActions action) {
			ObjectNode requestData = mapper.createObjectNode();
			requestData.put("chat_id", chatId);
			requestData.put("action", action.getChatAction());
			RequestBody body = RequestBody.create(requestData.toString(), JSON);
			return doPost("sendChatAction", body, new TypeReference<>() {});
		}
		// TODO: send Photo, Sticker (it's impossible for now lol)
	}

	// TODO [FAKE] : Local HTTP server for webhook receiver
	public void startWebhookServer(int port, String path, @Nullable String expectedSecretToken) {
		try {
			this.webhookServer = HttpServer.create(new InetSocketAddress(port), 0);
			this.webhookServer.createContext(path, exchange -> {
                if ("POST".equals(exchange.getRequestMethod())) {
                    if (expectedSecretToken != null && !expectedSecretToken.isEmpty()) {
                        String receivedToken = exchange.getRequestHeaders().getFirst("X-Bot-Api-Secret-Token");
                        if (!expectedSecretToken.equals(receivedToken)) {
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
                                Update update = mapper.readValue(jsonBody, Update.class);
                                if (update != null) {
                                    processUpdate(update);
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
			this.webhookServer.setExecutor(Executors.newCachedThreadPool());
			this.webhookServer.start();
			System.out.println("Webhook Server listening on port " + port);

		} catch (IOException e) {
			throw new RuntimeException("Failed to start server", e);
		}
	}

	public void stopWebhookServer() {
		if (this.webhookServer != null) {
			this.webhookServer.stop(0);
			System.out.println("Webhook server stopped.");
		}
	}



	// TODO [FAKE] : Helpers
	// TODO [FAKE] : all the endpoint's HTTP methods is POST btw
	private <T> CompletableFuture<T> doPost(String endpoint, @Nullable RequestBody body, TypeReference<Generic<T>> responseType) {
		Request request = new Request.Builder()
				.url(this.baseUrlWithToken + "/" + endpoint)
				.post(!Objects.isNull(body) ? body : RequestBody.create("{}", JSON))
				.header("User-Agent", USER_AGENT)
				.build();
		return async(request, responseType);
	}
	private <T> CompletableFuture<T> async(Request req, TypeReference<Generic<T>> rep) {
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
					Generic<T> apiResponse = mapper.readValue(body.string(), rep);
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