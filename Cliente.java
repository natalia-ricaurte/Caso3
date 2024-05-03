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

                System.out.print("Inicie la comunicacion con el servidor: ");
                userInput = scanner.nextLine();
                Log.add(userInput);
                userInput = "1,"+userInput;

                
                while (true) {
                    out.println(userInput);
                    String rtaServer = in.readLine();
                    String [] message = rtaServer.split(",");

                    if (message[0].equals("2")){
                        System.out.println("CLIENT: Respuesta del servidor: " + message[1]);
                        userInput = "OK";
                        Log.add(userInput);
                        userInput = "4,"+userInput;
                    }
                    else if (message[0].equals("7")){
                        System.out.println("CLIENT: Respuesta del servidor: "+message[1]
                                            +","+message[2]+","+message[3]+","+message[4]+","+message[5]);
                        // Validar Llave Asimetrica
                        userInput = "OK";
                        Log.add(userInput);
                        userInput = "9," + userInput;

                    }
                    else if (message[0].equals("12")) {
                        System.out.println("CLIENT: Respuesta del servidor: " + rtaServer);
                        userInput = "Login,Contraseña";
                        Log.add(userInput);
                        userInput = "13," + userInput;
                    }
                    else if (message[0].equals("16")) {
                        System.out.println("CLIENT: Respuesta del servidor: " + rtaServer);
                        userInput = "Consulta";
                        Log.add(userInput);
                        userInput = "17," + userInput;
                        
                    }
                    else {
                        System.out.println("CLIENT: Respuesta del servidor: " + rtaServer);
                        break;
                    }
                    
                    
                }
            }

        } catch (IOException e) {
            System.err.println("Cliente Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}