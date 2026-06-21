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
 * Lõi của cái bot này
 */
public class ZaloBot {
	private boolean _isServerStarted = false;
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
     * Đừng tạo thêm OkHttp client, dùng cái đi kèm cho tiện
     */
	public final OkHttpClient client = Constants.sharedOkHttp;
    /**
     * Dùng {@link ZaloBot.StandaloneAPI} bằng cái này, đừng tạo thêm cái khác
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
     * Chạy bot, đơn giản thì chỉ là chạy webhook server
     */
	public void start() {
		this._startWebhookServer(_webhookServerData);
	}

    /**
     * Tắt bot, đơn giản thì chỉ là tắt webhook server
     */
	public void stop() {
		if (this._webhookServer != null) {
			this._webhookServer.stop(0);
			this._isServerStarted = false;
			System.out.println("Webhook server đã dừng.");
		}
	}

	/**
	 * Lấy cái tên bot để quản lý
	 * @return Tên bot user tự đặt
	 */
	public String name() {
		return this._botManagementName;
	}

	/**
	 * Lấy token của bot
	 * @return chứ chả lẽ cho bạn cái nịt?
	 */
	public String token() {
		return this._token;
	}

    /**
	 * Thêm {@link Handler} cho bot
     * @param handler Class được implement {@link Handler}
	 * @return {@link ZaloBot} hiện tại
     */
	public ZaloBot addHandler(Handler handler) {
		this._handlers.add(handler);
		return this;
	}

    /**
	 * Thêm cả danh sách các {@link Handler} cho bot
     * @param listOfHandler yes. danh sách class được implement {@link Handler}
	 * @return {@link ZaloBot} hiện tại
     */
	public ZaloBot addHandler(List<Handler> listOfHandler) {
		for (Handler handler : listOfHandler) {
			addHandler(handler);
		}
		return this;
	}

    /**
	 * Set data cho webhook server, của từng bot
     * @param data {@link WebhookServerData}
	 * @return {@link ZaloBot} hiện tại
     */
	public ZaloBot setWebHookServerData(WebhookServerData data) {
		this._webhookServerData = data;
		return this;
	}

    /**
     * Ayo đừng để nó chết chứ?
     */
	public void keepMeAlivePlease() {
		if (!this._isServerStarted) {
			System.out.println("Cảnh báo: Webhook server có thể vẫn chưa được khởi động!!! Việc này sẽ khiến bot không thể nhận event từ Zalo!!!");
		}
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			System.err.println("Đã dừng (thủ công)");
		}
	}

	private void _processUpdate(Received received) {
		for (Handler handler : _handlers) {
			if (handler.checkUpdate(received)) {
				handler.handleUpdate(received, this)
						.exceptionally(ex -> {
							System.err.println("Handler đã nổ: " + ex.getMessage());
							return null;
						});
				break;
			}
		}
	}

	private boolean over2000char(String string) {
		return string.length() > 2000;
	}

// ------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Standalone API nghĩa là API có thể dùng riêng, nhưng tôi không chắc về điều đó.<br><br>
	 * Đơn giản thì nó là đống HTTP thôi, để reply interaction của user ấy.
     */
	public class StandaloneAPI {
        /**
		 * Lấy thông tin user bot trên Zalo server
         * @return {@link User} của bot
         */
		public CompletableFuture<User> getMe() {
			return _doPost("getMe", null, new TypeReference<>() {});
		}

        /**
		 * Nói thật thì tôi cũng chả biết cái "long polling" này hoạt động như thế nào<br>
		 * nên tôi sẽ set nó {@link Deprecated} nhưng không gỡ bỏ do Zalo vẫn còn dùng cái này.<br>
		 * (Ai biết chỉ tôi với...)
         * @param timeout cái gì đây?
         * @param offset cái gì đây?
         * @return cái gì đây? đố mà biết được (21/06/26)
         */
		@Deprecated
		public CompletableFuture<Received> getUpdates(int timeout, @Nullable Integer offset) {
			ObjectNode requestData = _mapper.createObjectNode();
			requestData.put("timeout", timeout);
			if (offset != null) {
				requestData.put("offset", offset);
			}
			RequestBody body = RequestBody.create(requestData.toString(), _JSON);
			return _doPost("getUpdates", body, new TypeReference<>() {});
		}

        /**
		 * Đúng như tên, lấy thông tin webhook ở trên Zalo server (không có webhook secret đâu, khỏi lo)
         * @return Dữ liệu {@link Webhook} của bot
         */
		public CompletableFuture<Webhook> getWebhook() {
			return _doPost("getWebhookInfo", null, new TypeReference<>() {});
		}

        /**
		 * Đặt webhook url và webhook secret<br>
		 * (Đừng nhầm với {@link WebhookServerData}, nó dành cho webhook server, không dành cho Zalo)
         * @param webhookUrl Webhook server url, thường thì dùng ngrok cho nó free, mà limit 1 server à...
         * @param webhookSecretToken Webhook secret, add thêm cho nó bảo mật tho.
         * @return {@code true} nếu đã được sửa đổi/tạo mới, ngược lại thì {@code false}
         */
		public boolean setWebhook(String webhookUrl, String webhookSecretToken) {
			try {
				Webhook info = api.getWebhook().join();
				if (info.url() == null || info.url().isEmpty()) {
					System.out.println("Webhook URL trống, đang add vào...");
					ObjectNode requestData = _mapper.createObjectNode();
					requestData.put("url", webhookUrl);
					requestData.put("secret_token", webhookSecretToken);
					RequestBody body = RequestBody.create(requestData.toString(), _JSON);
					return (_doPost("setWebhook", body, new TypeReference<Generic<Webhook>>() {}).join()).url().equals(webhookUrl);
				} else if (!info.url().equals(webhookUrl)) {
					System.out.println("Webhook URL hiện tại khác với cái đang sử dụng. Đang đổi lại...");
					ObjectNode requestData = _mapper.createObjectNode();
					requestData.put("url", webhookUrl);
					requestData.put("secret_token", webhookSecretToken);
					RequestBody body = RequestBody.create(requestData.toString(), _JSON);
                    return (_doPost("setWebhook", body, new TypeReference<Generic<Webhook>>() {}).join()).url().equals(webhookUrl);
				} else {
					System.out.println("Webhook đã được set thành \"" + webhookUrl + "\" sẳn rồi.");
					return true;
				}
			} catch (Exception e) {
				System.out.println("Không tìm thấy bất kì webhook link nào hiện có, đang add thêm...");
				try {
					ObjectNode requestData = _mapper.createObjectNode();
					requestData.put("url", webhookUrl);
					requestData.put("secret_token", webhookSecretToken);
					RequestBody body = RequestBody.create(requestData.toString(), _JSON);
					return (_doPost("setWebhook", body, new TypeReference<Generic<Webhook>>() {}).join()).url().equals(webhookUrl);
				} catch (Exception ex) {
					System.err.println("CRITICAL: Không thể set webhook: " + ex.getMessage());
					return false;
				}
			}
		}

        /**
		 * Như cái tên.
         * @return {@code true} nếu webhook đi tong, ngược lại thì {@code false}
         */
		public boolean deleteWebhook() {
			return (_doPost("deleteWebhook", null, new TypeReference<Generic<Webhook>>() {}).join()).url().isEmpty();
		}

        /**
         * Gửi tin nhắn (dạng văn bản)<br>
         * Nếu văn bản quá 2000 kí tự thì nó trả về {@code null} và không gửi gì cả<br>
         * Muốn thêm thông tin về limit này, đọc ở <a href="https://bot.zapps.me/docs/apis/sendMessage/#:~:text=N%E1%BB%99i%20dung%20v%C4%83n%20b%E1%BA%A3n%20c%E1%BB%A7a%20tin%20nh%E1%BA%AFn%20s%E1%BA%BD%20%C4%91%C6%B0%E1%BB%A3c%20g%E1%BB%ADi%2C%20v%E1%BB%9Bi%20%C4%91%E1%BB%99%20d%C3%A0i%20t%E1%BB%AB%201%20%C4%91%E1%BA%BFn%202000%20k%C3%BD%20t%E1%BB%B1">đây</a>
         * @param chatId Dùng {@code update.message().chat().id()} để lấy nó, trong cái {@link Handler} của bạn
         * @param text Văn bản
		 * @param replyMessageID ID tin nhắn để reply, có cũng được, không thì để thành {@code null}
         * @return {@link Message} hoặc {@code null}, có thể bỏ qua, hoặc lấy dữ liệu nếu cần
         */
		public CompletableFuture<Message> sendMessage(String chatId, String text, @Nullable String replyMessageID) {
			if (over2000char(text)) {
				System.out.println("Kí tự quá nhiều: " + text.length() + " từ. Tối đa: 2000 kí tự.");
				return null;
			}
			ObjectNode requestData = _mapper.createObjectNode();
			requestData.put("chat_id", chatId);
			requestData.put("text", text);
			if  (replyMessageID != null) {
				requestData.put("reply_to_message_id", replyMessageID);
			}
			RequestBody body = RequestBody.create(requestData.toString(), _JSON);
			return _doPost("sendMessage", body, new TypeReference<>() {});
		}

        /**
		 * Gửi hành động trả lời tin nhắn<br>
		 * Có thể là {@link ChatActions}{@code .TYPING} (đang nhập tin nhắn...)<br>
		 * hoặc 1 số khác trong class {@link ChatActions}, nhưng vẫn còn đang thử nghiệm<br>
		 * Sẽ biến mất sau khoảng 10 giây từ lúc method này được gọi, hoặc khi method liên quan đến gửi tin nhắn được gọi
         * @param chatId Dùng {@code update.message().chat().id()} để lấy nó trong cái {@link Handler} của bạn
         * @param action {@link ChatActions}
         * @return {@code true} nếu đã được gửi, ngược lại thì {@code false}<br>
		 * Có thể bỏ qua, do cũng chả có gì để sử dụng ở đây cả
         */
		public CompletableFuture<Boolean> sendChatAction(String chatId, ChatActions action) {
			ObjectNode requestData = _mapper.createObjectNode();
			requestData.put("chat_id", chatId);
			requestData.put("action", action.getChatAction());
			RequestBody body = RequestBody.create(requestData.toString(), _JSON);
			return _doPost("sendChatAction", body, new TypeReference<>() {});
		}

        /**
		 * Gửi tin nhắn (dạng hình ảnh, có thể kèm theo chú thích)<br>
		 * Nếu chú thích ảnh quá 2000 kí tự thì nó sẽ bị bỏ qua<br>
		 * Muốn thêm thông tin về limit này, đọc ở <a href="https://bot.zapps.me/docs/apis/sendPhoto/#:~:text=N%E1%BB%99i%20dung%20v%C4%83n%20b%E1%BA%A3n%20c%E1%BB%A7a%20tin%20nh%E1%BA%AFn%20s%E1%BA%BD%20%C4%91%C6%B0%E1%BB%A3c%20g%E1%BB%ADi%20k%C3%A8m%2C%20v%E1%BB%9Bi%20%C4%91%E1%BB%99%20d%C3%A0i%20t%E1%BB%AB%201%20%C4%91%E1%BA%BFn%202000%20k%C3%BD%20t%E1%BB%B1">đây</a>
         * @param chatId Dùng {@code update.message().chat().id()} để lấy nó trong cái {@link Handler} của bạn
         * @param photoUrl URL dẫn thẳng tới hình, không phải link rút gọn đâu nhé<br>
		 * Ví dụ: https://i.imgur.com/tk59bD7.png
         * @param photoCaption Cái chú thích nho nhỏ, không đặt thì set {@code null}
         * @return {@link Message}, có thể bỏ qua, hoặc lấy dữ liệu nếu cần
         */
		public CompletableFuture<Message> sendPhoto(String chatId, String photoUrl, @Nullable String photoCaption) {
			ObjectNode requestData = _mapper.createObjectNode();
			requestData.put("chat_id", chatId);
			requestData.put("photo", photoUrl);
			if (photoCaption != null) {
				if (over2000char(photoCaption)) {
					System.out.println("Warning: Chú thích ảnh vượt quá 2000 kí tự, sẽ không được add vào và gửi đi.");
				} else {
					requestData.put("caption", photoCaption);
				}
			}
			RequestBody body = RequestBody.create(requestData.toString(), _JSON);
			return _doPost("sendPhoto", body, new TypeReference<>() {});
		}

        /**
		 * Gửi tin nhắn (dạng sticker)<br>
		 * (này rối răm vãi lìn ra)
         * @param chatId Dùng {@code update.message().chat().id()} để lấy nó trong cái {@link Handler} của bạn
         * @param stickerId ID của sticker, muốn thêm thông tin, đọc ở  <a href="https://bot.zapps.me/docs/apis/sendSticker/#:~:text=cu%E1%BB%99c%20tr%C3%B2%20chuy%E1%BB%87n-,sticker,-String">đây</a>
         * @return {@link Message}, có thể bỏ qua, hoặc lấy dữ liệu nếu cần
         */
		public CompletableFuture<Message> sendSticker(String chatId, String stickerId) {
			ObjectNode requestData = _mapper.createObjectNode();
			requestData.put("chat_id", chatId);
			requestData.put("sticker", stickerId);
			RequestBody body = RequestBody.create(requestData.toString(), _JSON);
			return _doPost("sendSticker", body, new TypeReference<>() {});
		}
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
                            System.err.println("BẢO MẬT: Đã block client truy cập không phép (thiếy header auth/header auth không giống như đã đặt).");
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
                                Received received = _mapper.readValue(jsonBody, Received.class);
                                if (received != null) {
                                    _processUpdate(received);
                                }
                            } catch (Exception e) {
                                System.err.println("Lỗi parse json: " + e.getMessage());
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
			this._isServerStarted = true;
			System.out.println("Webhook Server đang được mở tại port " + serverData.port());
		} catch (IOException e) {
			throw new RuntimeException("Mở server thất bại: ", e);
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
						future.completeExceptionally(new RuntimeException("Body trống, không nghĩ nó sẽ tốt đâu."));
						return;
					}
					Generic<T> apiResponse = _mapper.readValue(body.string(), rep);
					if (apiResponse.ok()) {
						future.complete(apiResponse.result());
					} else {
						future.completeExceptionally(new RuntimeException(
								"Lỗi API (" + apiResponse.errorCode() + "): " + apiResponse.description()
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