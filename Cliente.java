import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Cliente extends Thread {
    private String host;
    private int port;

    public Cliente(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(host, port)) {
            System.out.println("Conectado al servidor en " + host + ":" + port);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            
            try (Scanner scanner = new Scanner(System.in)) {
                String userInput;

                ArrayList<String> Log = new ArrayList<String>();

                System.out.print("Ingrese un mensaje (\"exit\" para terminar): ");
                userInput = scanner.nextLine();
                Log.add(userInput);
                userInput = "1,"+userInput;

                
                while (true) {
                    out.println(userInput);
                    String rtaServer = in.readLine();
                    System.out.println("Respuesta del servidor: " + rtaServer);
                    String [] message = rtaServer.split(",");
                    System.out.println(message[0] + " " + message[1]);

                    if (message[0].equals("2")){
                        System.out.println("Respuesta del servidor: " + message[1]);
                        userInput = "OK";
                        Log.add(userInput);
                        userInput = "3,"+userInput;
                    }
                    else {
                        userInput = scanner.nextLine();
                        userInput = "10,"+userInput;
                    }
                    
                }
            }

        } catch (IOException e) {
            System.err.println("Cliente Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
