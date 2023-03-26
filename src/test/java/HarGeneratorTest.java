/**
 * Данный способ генерации har файла сделан в виде теста.
 * в блоке @BeforeSuite настройка и запуск прокси
 * в блоке @Test выполнение прописывается тестовый сценарий
 * в блоке @AfterSuite выполняется создание и сохранение har файла. Файл сохраняется в директорию проекта
 * в формате - logOfBrowserДатаВремяСоздания.har
 *
 */

import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.harreader.model.*;
import com.codeborne.selenide.Configuration;

import org.openqa.selenium.Keys;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;
import static com.codeborne.selenide.Selenide.*;


public class HarGeneratorTest {
    BrowserUpProxyServer server = new BrowserUpProxyServer();
    @BeforeSuite
    public void startProxy() {
        //настройка сервера, прокси, старт прокси
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
    }
    @Test
    public void generateHarTest() {
       //выполнение тестового сценария. заметить на свой
        open("http://google.com"); //если open() осуществляется при загрузке фрейморка, то перед его загрузкой должен быть запущен проксисервер
        $x("//input[@name='q']").sendKeys("мемасики про котов", Keys.ENTER);
    }

    @AfterSuite
    public void generateHar() {
        //генерация харника
        sleep(5000); // открыв нужную страницу, спим 5 сек, что бы запросы выполнились и загрузились
        Har har = server.getHar();
        //сохранение har файла в директорию проекта
        String userDir = System.getProperty("user.dir");
        File projectDir = new File(userDir);
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
        File harFile = new File(projectDir, "logOfBrowser"+ formater.format(new Date()) + ".har");
        try {
            har.writeTo(harFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Har file saved to " + harFile.getAbsolutePath());
        server.abort();
    }


}
