package cc.baka9.catseedlogin.velocity;

import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Config {

    public static boolean Enable;
    public static String Host;
    public static int Port;
    public static String LoginServerName;
    public static String AuthKey;

    public static void load(PluginMain plugin) {

        Logger logger = plugin.getLogger();
        Path dataDirectory = plugin.getDataDirectory();

        File dataFolder = dataDirectory.toFile();

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        String fileName = "config.properties";
        File configFile = new File(dataFolder, fileName);
        if (!configFile.exists()) {
            try (InputStream in = plugin.getClass().getClassLoader().getResourceAsStream("config.properties")) {
                if (in == null) {
                    logger.error("无法在插件 JAR 中找到 'config.properties' 资源文件。");
                    return;
                }
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(configFile)) {
            prop.load(input);
            Enable = Boolean.parseBoolean(prop.getProperty("Enable", "true"));
            Host = prop.getProperty("Host", "localhost");
            Port = Integer.parseInt(prop.getProperty("Port", "25565"));
            LoginServerName = prop.getProperty("LoginServerName", "login");
            AuthKey = prop.getProperty("AuthKey", "your_auth_key_here");

            logger.info("Host: " + Host);
            logger.info("Port: " + Port);
            logger.info("LoginServerName: " + LoginServerName);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
