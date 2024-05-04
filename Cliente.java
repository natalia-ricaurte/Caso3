import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.util.Random;


public class Cliente extends Thread {
    private String host;
    private int port;
    private BigInteger P;
    private Integer G;
    private PublicKey publicKey;
    private SecretKey k_AB1;
    private SecretKey k_AB2;

    public Cliente(String host, int port, BigInteger P, Integer G, PublicKey publicKey) {
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

                    if (message[0].equals("3")){
                        System.out.println("CLIENT: Respuesta del servidor: " + message[2]);
                        try {
                            String mensaje = message[2];
                            byte[] cifrado = ToByte(mensaje);
                            byte[] descifrado = CifradoAsimétrico.descifrar(publicKey, "RSA", cifrado);
                            String mensajeDescifrado = new String(descifrado);
                    
                            System.out.println("CLIENTE: Mensaje descifrado: " + mensajeDescifrado);
                            if (mensajeDescifrado.equals(mensajeEntrada)){
                                userInput = "OK";
                                Log.add(userInput);
                                userInput = "5,"+userInput;
                            } else {
                                userInput = "ERROR";
                         
                            } 
                        } catch (Exception e) {
                            System.out.println("Error descifrando el mensaje: " + e.getMessage());
                            e.printStackTrace();
                        }
                  
                    }
                    else if (message[0].equals("7")){
                        System.out.println("CLIENT: Respuesta del servidor: " + message[5]);
                        String parametros = message[2] + "," + message[3]+ "," +message[4];
                    
                        String firma = message[5];
        
                        byte [] firmaBytes = ToByte(firma);
                        try {
                            Signature publicSignature = Signature.getInstance("SHA256withRSA");
                            publicSignature.initVerify(publicKey);
                            publicSignature.update(parametros.getBytes(StandardCharsets.UTF_8));
                            boolean verificada = publicSignature.verify(firmaBytes);

                            System.out.println("Firma verificada: " + firmaBytes);

                            if (verificada){

                          

                                Random random = new Random();
                                int y = random.nextInt(1000);

                                double gy = Math.pow((double)G, (double)y);
                                userInput = Double.toString(gy);
                                userInput = "10," + "OK," + userInput;

                                String gx = message[4];
                                Double semilla = Math.pow(Double.parseDouble(gx), (double)y);

                                try {
                                    k_AB1 =llaveSimetrica(semilla.toString(), 0, 32);
                                    k_AB2 =llaveSimetrica(semilla.toString(), 32, 64);
                                } catch (Exception e) {
                                    System.out.println("Error creando la llave simetrica: " + e.getMessage());
                                    e.printStackTrace();
                                }

         
                            } else {
                                userInput = "ERROR";
                                Log.add(userInput);
                            }
                        } catch (InvalidKeyException e) {
                           System.out.println("Error verificando la firma: " + e.getMessage());
                            e.printStackTrace();
                        } catch (NoSuchAlgorithmException e) {
                            System.out.println("Error verificando la firma: " + e.getMessage());
                            e.printStackTrace();
                        } catch (SignatureException e) {
                            System.out.println("Error verificando la firma: " + e.getMessage());
                            e.printStackTrace();
                        }

                        

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
