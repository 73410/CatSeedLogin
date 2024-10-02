package cc.baka9.catseedlogin.velocity;

import cc.baka9.catseedlogin.util.CommunicationAuth;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * 代理与 Bukkit 的通讯交流
 */
public class Communication {

    private final Logger logger;

    public Communication(Logger logger) {
        this.logger = logger;
    }

    public int sendConnectRequest(String playerName) {
        try (Socket socket = getSocket(); BufferedWriter bufferedWriter = getSocketBufferedWriter(socket)) {
            // 请求类型
            bufferedWriter.write("Connect");
            bufferedWriter.newLine();
            // 玩家名
            bufferedWriter.write(playerName);
            bufferedWriter.newLine();

            bufferedWriter.flush();
            return socket.getInputStream().read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void sendKeepLoggedInRequest(String playerName) {
        try (Socket socket = getSocket(); BufferedWriter bufferedWriter = getSocketBufferedWriter(socket)) {
            // 请求类型
            bufferedWriter.write("KeepLoggedIn");
            bufferedWriter.newLine();
            // 玩家名
            bufferedWriter.write(playerName);
            bufferedWriter.newLine();
            // 时间戳
            String time = String.valueOf(System.currentTimeMillis());
            bufferedWriter.write(time);
            bufferedWriter.newLine();
            // 签名
            String sign = CommunicationAuth.encryption(playerName, time, Config.AuthKey);
            bufferedWriter.write(sign);
            bufferedWriter.newLine();

            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Socket getSocket() throws IOException {
        try {
            return new Socket(Config.Host, Config.Port);
        } catch (IOException e) {
            logger.warn("请检查装载登录插件的子服是否在 velocity.toml 中开启了代理功能，以及 Host 和 Port 是否与代理端的配置相同");
            throw new IOException(e);
        }
    }

    private BufferedWriter getSocketBufferedWriter(Socket socket) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }
}
