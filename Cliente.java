import java.io.*;
import java.math.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.*;


public class Cliente extends Thread {
    private String host;
    private int port;
    private BigInteger P;
    private Integer G;
    private PublicKey publicKey;
    private SecretKey k_AB1;
    private SecretKey k_AB2;
    private IvParameterSpec iv;

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
                            String firma = message[2];

                            byte [] firmaBytes = ToByte(firma);
               
                            Signature publicSignature = Signature.getInstance("SHA256withRSA");
                            publicSignature.initVerify(publicKey);
                            publicSignature.update(mensajeEntrada.getBytes(StandardCharsets.UTF_8));
                            boolean verificada = publicSignature.verify(firmaBytes);

                            System.out.println("CLIENTE:Firma verificada " + firmaBytes);
                            if (verificada){
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
                        
                        String parametroIv = message[5];
                        byte[] ivBytes = ToByte(parametroIv);
                        iv = new IvParameterSpec(ivBytes);

                        String parametros = message[2] + "," + message[3]+ "," +message[4];
                    
                        String firma = message[6];
        
                        byte [] firmaBytes = ToByte(firma);
                        try {
                            Signature publicSignature = Signature.getInstance("SHA256withRSA");
                            publicSignature.initVerify(publicKey);
                            publicSignature.update(parametros.getBytes(StandardCharsets.UTF_8));
                            boolean verificada = publicSignature.verify(firmaBytes);

                            System.out.println("Firma verificada: " + firmaBytes);

                            if (verificada){
                                SecureRandom random = new SecureRandom();
                                int var = Math.abs(random.nextInt());
                                Long longvar = Long.valueOf(var);
                                BigInteger y = BigInteger.valueOf(longvar);

                                BigInteger bigG = BigInteger.valueOf(G);
                                Double gy = bigG.modPow(y, P).doubleValue();

                                userInput = "10," + "OK," + gy.toString();

                                String gx  = message[4];
                                Double bigGX = Double.parseDouble(gx);
                                Double semilla = Math.pow(bigGX, y.intValue()) % P.intValue();
                                

                                try {
                                    k_AB1 = llaveSimetrica(semilla.toString(), 0, 32);
                                    k_AB2 = llaveSimetrica(semilla.toString(), 32, 64);
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
    
                        String login = "Login";
                        String contraseña = "Contraseña"; 

                        byte[] cifrarL = Cifrados.cifrarSim(k_AB1,iv,login.getBytes());
                        byte[] cifrarC = Cifrados.cifrarSim(k_AB1,iv,contraseña.getBytes());

                        String loginCifrado = ToString(cifrarL);
                        String contraseñaCifrado = ToString(cifrarC);


                        userInput = "13," + loginCifrado + "," + contraseñaCifrado;
                    }
                    else if (message[0].equals("16")) {
                        System.out.println("CLIENT: Respuesta del servidor: " + rtaServer);
                        
                        int randConsulta = ThreadLocalRandom.current().nextInt(0, 100);
                        String consulta = Integer.toString(randConsulta);
                        byte[] consultaBytes = consulta.getBytes();

                        //Consulta C(K_AB1, consulta)
                        byte[] consultaCifrada = Cifrados.cifrarSim(k_AB1, iv, consultaBytes);

                        //Consulta HMAC(K_AB2, consulta)
                        byte[] consultaHash = HMAC(k_AB2, consultaBytes);
                        
                        String consultCifrada = ToString(consultaCifrada);
                        String consultHash = ToString(consultaHash);
                        userInput = "17," + consultCifrada + "," + consultHash;
                        
                    }else if (message[0].equals("19")) {
                        System.out.println("CLIENT: Respuesta del servidor: " + rtaServer);
                        // Verificar la respuesta
                        byte[] cifradaConsulta = ToByte(message[1]);
                        byte[] hashConsulta = ToByte(message[2]);

                        byte[] descifradoConsulta = Cifrados.descifrarSim(k_AB1, iv, cifradaConsulta);

                        boolean hashVerificado = MAC(k_AB2,descifradoConsulta , hashConsulta);
                        System.out.println("21," + hashVerificado);
                    
                        if (hashVerificado) {
                            break;
                        } 
                        else{
                            System.out.println("Error");
                            break;
                        }
                    }
                    else {
                        System.out.println("CLIENT: Respuesta del servidor: " + rtaServer);
                        break;
                    }
  
                }
            } catch (Exception e) {
                System.err.println("Cliente Exception: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (IOException e) {
            System.err.println("Cliente Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Funciones adicionales
    // Convertir un string a un arreglo de bytes
    public byte[] ToByte( String texto)
	{	
		byte[] ret = new byte[texto.length()/2];
		for (int i = 0 ; i < ret.length ; i++) {
			ret[i] = (byte) Integer.parseInt(texto.substring(i*2,(i+1)*2), 16);
		}
		return ret;
	}
    // Convertir un arreglo de bytes a un string
	public String ToString( byte[] bytes )
	{	
		String ret = "";
		for (int i = 0 ; i < bytes.length ; i++) {
			String texto = Integer.toHexString(((char)bytes[i])&0x00ff);
			ret += (texto.length()==1?"0":"") + texto;
		}
		return ret;
	}

    // Crear una llave simetrica a partir de una semilla y un rango de bytes 
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
    // Convertir un string hexadecimal a un string
    public String hexToString(String hex) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < hex.length(); i += 2) {
            String output = hex.substring(i, (i + 2));
            int decimal = Integer.parseInt(output, 16);
            baos.write(decimal);
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }
    // Funcion HMAC para generar un hash de un mensaje con una llave secreta 
    public byte[] HMAC(SecretKey key,byte[] msg) throws Exception {
        Mac mac = Mac.getInstance("HMACSHA256");
        mac.init(key);
        byte[] bytes = mac.doFinal(msg);
        return bytes;
    }
    // Funcion para verificar la integridad de un mensaje con un hash y una llave secreta 
    public boolean MAC( SecretKey key, byte[] msg,byte [] hash ) throws Exception
	{ 
		byte [] nuevo = HMAC(key,msg);
		if (nuevo.length != hash.length) {
			return false;
		}
		for (int i = 0; i < nuevo.length ; i++) {
			if (nuevo[i] != hash[i]) {
           
            return false;
            }
		}

		return true;
	}
    
}
