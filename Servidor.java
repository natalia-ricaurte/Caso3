import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.util.Base64;
import java.util.Random;

public class Servidor extends Thread {
    private int port;
    private BigInteger P;
    private Integer G;
    private Key publicKey;
    private Key privateKey;

    public Servidor(int port, BigInteger P, Integer G, Key publicKey, Key privateKey) {
        this.port = port;
        this.P = P;
        this.G = G;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor iniciado en el puerto " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado.");

                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                String clientInput;
                int x = 0;
                while ((clientInput = input.readLine()) != null) {
                    System.out.println("SERVER: Recibido del cliente: " + clientInput);
                    String [] message = clientInput.split(",");
                    if (message[0].equals("1")){

                     try {
                        byte[] cifrado = CifradoAsimétrico.cifrar(privateKey, "RSA", message[1]); 
                        String cifradoBase64 = Base64.getEncoder().encodeToString(cifrado);
                        output.println("3,Cifra," + cifradoBase64); 
                    } catch (Exception e) {
                        System.out.println("Error cifrando el mensaje: " + e.getMessage());
                        e.printStackTrace();
}}
                    // ...

                    else if (message[0].equals("5")){
                        if (message[1].equals("OK")) {
                            Random random = new Random();                            
                            x = random.nextInt(1000);
                            double gx = Math.pow((double)G, (double)x);
                            String mensaje = Integer.toString(G) + "," + P.toString() + "," + Double.toString(gx);
                            byte[] aCifrar = CifradoAsimétrico.cifrar(privateKey, "RSA", mensaje);
                            String parametrosCifrados = Base64.getEncoder().encodeToString(aCifrar);
                            output.println("7,Cifra," + parametrosCifrados);
                            //output.println("7,G,P,Gx,iv,C(K_w-.(G.P.Gx))");
                        }else{
                            output.println("ERROR");
                        }                        
                    }
                    else if (message[0].equals("9")){
                        if (message[1].equals("OK")) {
                            // Calcualr Gy^x
                            // K_AB1
                            // K_AB2
                            int gy = Integer.parseInt(message[2]);
                            double llaveSimetrica = Math.pow((double)gy, (double)x);
                            output.println("12,K_AB1,K_AB2");
                        }else{
                            output.println("ERROR");
                        }   
                    }
                    else if (message[0].equals("13")){
                        if (message[1].equals("Login") && message[2].equals("Contraseña")) {
                            output.println("16,OK");
                        }else{
                            output.println("ERROR");
                        }
                    }
                    else if (message[0].equals("17")){
                        // Responder
                        output.println("19,rta");    
                    }

                    
                }

                socket.close();
                System.out.println("Cliente desconectado.");
            }
        } catch (IOException e) {
            System.err.println("Servidor Exception: " + e.getMessage());
            e.printStackTrace();
        }

        }
}



