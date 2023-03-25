/**
 * Сканер свободных портов. Задать стартовый порт и конечный порт. Итог - выведение списка
 * @Author: Raman Darashenka
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class FreePortFinder {
    public static void main(String[] args) throws IOException {

            int startPort = 49152;
            int endPort = 65535;
            List<Integer> freePorts = new ArrayList<>();

            for (int port = startPort; port <= endPort; port++) {
                try (ServerSocket serverSocket = new ServerSocket(port)) {
                    freePorts.add(port);
                } catch (IOException e) {

                }
            }

            System.out.println("Free ports are:");
            for (int port : freePorts) {
                System.out.println(port);
            }
        }

}
