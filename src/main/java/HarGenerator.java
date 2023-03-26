/**
 * Данный способ генерации har файла сделан в виде метода генерации
 * har файла через передачу URL в метод
 * PS работает только если передать уже готовый адрес в метод, без предварительного запуска вебдрайвера
 * Для прохождения тестового сценария и снятия данных в har, использовать HarGeneratorTest
 */

import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.harreader.model.Har;
import com.codeborne.selenide.*;
import com.codeborne.selenide.proxy.SelenideProxyServer;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.codeborne.selenide.Selenide.*;


public class HarGenerator {
    private static BrowserUpProxyServer server = new BrowserUpProxyServer();

    public static void getHar(String urlSite) {
        prepareProxy();
        open(urlSite);
        sleep(5000); // открыв нужную страницу, спим 5 сек, что бы запросы выполнились и загрузились
        createHar();
    }

    private static void prepareProxy() {
        server.setTrustAllServers(true);
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int port = serverSocket.getLocalPort();
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        System.out.println(Thread.currentThread().getId());
        //open();
    }

    private static void createHar() {
        Har har = server.getHar();
        //сохранение файла в директорию проекта
        String userDir = System.getProperty("user.dir");
        File projectDir = new File(userDir);
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
        File harFile = new File(projectDir, "logOfBrowser" + formater.format(new Date()) + ".har");
        try {
            har.writeTo(harFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Har file saved to " + harFile.getAbsolutePath());
        server.abort();
    }


}
