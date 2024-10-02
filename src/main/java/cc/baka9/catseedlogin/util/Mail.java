package cc.baka9.catseedlogin.util;

import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import cc.baka9.catseedlogin.bukkit.Config;
import org.bukkit.Bukkit;

public class Mail {

    private static final OkHttpClient client = new OkHttpClient();

    private Mail() {
    }

    public static void sendMail(String receiveMailAccount, String subject, String content) throws Exception {
        CompletableFuture<Void> future = sendMailAsync(receiveMailAccount, subject, content);
        future.get(); // 等待异步操作完成
    }

    private static CompletableFuture<Void> sendMailAsync(String receiveMailAccount, String subject, String content) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 构建请求体
                String sender = Config.EmailVerify.FromPersonal;
                String json = String.format("{\"email\":\"%s\",\"sender\":\"%s\",\"phrase\":\"%s\",\"subject\":\"%s\"}",
                        receiveMailAccount, sender, content, subject);

                // 创建请求
                Request request = new Request.Builder()
                        .url(Config.EmailVerify.api) // 替换为你的后端URL
                        .post(RequestBody.create(json, MediaType.get("application/json; charset=utf-8")))
                        .build();

                // 发送请求并获取响应
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        String errorResponse = response.body().string();
                        Bukkit.getLogger().severe("Failed to send email. Response code: " + response.code() + ", Response body: " + errorResponse);
                        throw new IOException("Unexpected code " + response);
                    }
                    // 处理响应
                    String successResponse = response.body().string();
                    Bukkit.getLogger().info("Email sent successfully: " + successResponse);
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("An error occurred while sending email: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
