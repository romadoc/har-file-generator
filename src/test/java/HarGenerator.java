import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.harreader.model.*;
import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.codeborne.selenide.Selenide.*;

public class HarGenerator {
    @Test
    public void generateHar() throws IOException {
        BrowserUpProxyServer server = new BrowserUpProxyServer();
        server.setTrustAllServers(true);

        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        serverSocket.close();
        server.start();
        Proxy seleniumProxy = new Proxy();
        seleniumProxy.setHttpProxy("localhost:" + server.getPort());
        seleniumProxy.setSslProxy("localhost:" + server.getPort());

        System.setProperty("selenide.proxy.host", "localhost");
        System.setProperty("selenide.proxy.capture.networkTraffic", "true");
        System.setProperty("selenide.proxy.capture.urlPatterns", ".*");

        Configuration.proxyEnabled = true;
        Configuration.proxyHost = "localhost";
        Configuration.proxyPort = port;
        Configuration.browserCapabilities.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
        Configuration.browserCapabilities.setCapability(CapabilityType.PROXY, seleniumProxy);

        server.setHarCaptureTypes(com.browserup.bup.proxy.CaptureType.getAllContentCaptureTypes());
        server.setHarCaptureTypes(com.browserup.bup.proxy.CaptureType.getCookieCaptureTypes());
        server.setHarCaptureTypes(com.browserup.bup.proxy.CaptureType.getHeaderCaptureTypes());
        server.setHarCaptureTypes(com.browserup.bup.proxy.CaptureType.getRequestCaptureTypes());
        server.setHarCaptureTypes(com.browserup.bup.proxy.CaptureType.getResponseCaptureTypes());

        server.newHar("TestHar");
        //открывающаяся страница. сделать метод не тестовым, а статиком, передавая сюда адрес страницы
        open("http://google.com");

        // Блок получения харника
        sleep(5000); //спим 5 сек, что бы запросы выполнились
        Har har = server.getHar();
        //сохранение файла в директорию проекта
        String userDir = System.getProperty("user.dir");
        File projectDir = new File(userDir);
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
        File harFile = new File(projectDir, "logOfBrowser"+ formater.format(new Date()) + ".har");
        har.writeTo(harFile);
        System.out.println("Har file saved to " + harFile.getAbsolutePath());
        server.abort();
    }
}
