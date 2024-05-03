import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.Key;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;

public class Cliente extends Thread {
    private String host;
    private int port;
    private BigInteger P;
    private Integer G;
    private Key publicKey;

    public Cliente(String host, int port, BigInteger P, Integer G, Key publicKey) {
        this.host = host;
        this.port = port;
        this.P = P;
        this.G = G;
        this.publicKey = publicKey;

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
                String mensajeEntrada = userInput;
                Log.add(userInput);
                userInput = "1,"+userInput;

                
                while (true) {
                    out.println(userInput);
                    String rtaServer = in.readLine();
                    String[] message = rtaServer.split(",");

                    if (message[0].equals("2")){
                        System.out.println("CLIENT: Respuesta del servidor: " + message[2]);
                        try {
                            String cifradoBase64 = message[2];
                            byte[] cifrado = Base64.getDecoder().decode(cifradoBase64);
                            byte[] descifrado = CifradoAsimétrico.descifrar(publicKey, "RSA", cifrado);
                            String mensajeDescifrado = new String(descifrado);
                    
                            System.out.println("CLIENTE: Mensaje descifrado: " + mensajeDescifrado);
                            if (mensajeDescifrado.equals(mensajeEntrada)){
                                userInput = "OK";
                                Log.add(userInput);
                                userInput = "4,"+userInput;
                         
                            } 
                        } catch (Exception e) {
                            System.out.println("Error descifrando el mensaje: " + e.getMessage());
                            e.printStackTrace();
                        }
                  
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
