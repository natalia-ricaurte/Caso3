import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Cliente extends Thread {
    private String hostName;
    private int port;

    public Cliente(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    public void run() {
        try (Socket socket = new Socket(hostName, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Conectado al servidor " + hostName + " en el puerto " + port);
            String userInput;

            while (true) {
                System.out.print("Escriba un mensaje: ");
                userInput = scanner.nextLine();
                if ("exit".equalsIgnoreCase(userInput)) break;

                out.println(userInput);
                System.out.println("Respuesta del servidor: " + in.readLine());
            }

        } catch (IOException e) {
            System.err.println("No se pudo conectar al servidor " + hostName + " en el puerto " + port);
            e.printStackTrace();
        }
    }
}