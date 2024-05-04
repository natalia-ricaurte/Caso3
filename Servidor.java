import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Servidor extends Thread {
    private int port;
    private BigInteger P;
    private Integer G;
    private Key publicKey;
    private PrivateKey privateKey;
    private SecretKey k_AB1;
    private SecretKey k_AB2;

    public Servidor(int port, BigInteger P, Integer G, Key publicKey, PrivateKey privateKey) {
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

                        byte[] cifrado = CifradoAsimétrico.cifrar(privateKey, "RSA", message[1]); 
                        String mensaje = ToString(cifrado);
                        output.println("3,Cifra," + mensaje); 
      
                    }

                    else if (message[0].equals("5")){
                        if (message[1].equals("OK")) {
                            Random random = new Random();                            
                            x = random.nextInt(1000);
                            double gx = Math.pow((double)G, (double)x);
                            String parametros = Integer.toString(G) + "," + P.toString() + "," + Double.toString(gx);
                            
                            try {
                                Signature privateSignature = Signature.getInstance("SHA256withRSA");
                                privateSignature.initSign(privateKey);
                                privateSignature.update(parametros.getBytes(StandardCharsets.UTF_8));
                                byte[] signature = privateSignature.sign();
                                String firma = ToString(signature);

                                output.println("7,Cifra," + parametros + "," + firma);
                            } catch (InvalidKeyException e) {
                                System.out.println("Error firmando el mensaje: " + e.getMessage());
                                e.printStackTrace();
                            } catch (NoSuchAlgorithmException e) {
                                System.out.println("Error firmando el mensaje: " + e.getMessage());
                                e.printStackTrace();
                            } catch (SignatureException e) {
                                System.out.println("Error firmando el mensaje: " + e.getMessage());
                                e.printStackTrace();
                            }
                        
                        }else{
                            output.println("ERROR");
                        }                        
                    }
                    else if (message[0].equals("10")){
                        if (message[1].equals("OK")) {
                            
                            Double gy = Double.parseDouble(message[2]);
                            
                            Double semilla = Math.pow(gy,x);

                                try {
                                    k_AB1 =llaveSimetrica(semilla.toString(), 0, 32);
                                    k_AB2 =llaveSimetrica(semilla.toString(), 32, 64);
                                } catch (Exception e) {
                                    System.out.println("Error creando la llave simetrica: " + e.getMessage());
                                    e.printStackTrace();
                                }

                            output.println("12,CONTINUAR");
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
        public byte[] ToByte( String texto)
        {	
            byte[] ret = new byte[texto.length()/2];
            for (int i = 0 ; i < ret.length ; i++) {
                ret[i] = (byte) Integer.parseInt(texto.substring(i*2,(i+1)*2), 16);
            }
            return ret;
        }
        
        public String ToString( byte[] bytes )
        {	
            String ret = "";
            for (int i = 0 ; i < bytes.length ; i++) {
                String texto = Integer.toHexString(((char)bytes[i])&0x00ff);
                ret += (texto.length()==1?"0":"") + texto;
            }
            return ret;
        }
        
        private SecretKey llaveSimetrica(String semilla, int inicio, int fin) throws Exception {
       
        // Convertir la semilla a bytes y calcular el hash SHA-512
        byte[] byteSemilla = semilla.trim().getBytes(StandardCharsets.UTF_8);
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] encodedHash = digest.digest(byteSemilla);

        // Extraer una parte del hash para crear la clave secreta
        byte[] keyBytes = new byte[fin - inicio];
        System.arraycopy(encodedHash, inicio, keyBytes, 0, keyBytes.length);

        // Crear y retornar la clave secreta AES
        return new SecretKeySpec(keyBytes, "AES");
    }
}



