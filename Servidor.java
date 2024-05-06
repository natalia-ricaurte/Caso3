import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class Servidor extends Thread {
    private int port;
    private BigInteger P;
    private Integer G;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private SecretKey k_AB1;
    private SecretKey k_AB2;
    private BigInteger x;
    private IvParameterSpec iv;
    private ManejadorTiempos mt;

    public Servidor(int port, BigInteger P, Integer G, PublicKey publicKey, PrivateKey privateKey) {
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
              
            
                while ((clientInput = input.readLine()) != null) {
                    System.out.println("SERVER: Recibido del cliente: " + clientInput);
                    String [] message = clientInput.split(",");
                    if (message[0].equals("1")){

                        String mensaje = message[1];
                        long inicioFirma = System.nanoTime();
                        Signature privateSignature = Signature.getInstance("SHA256withRSA");
                        privateSignature.initSign(privateKey);
                        privateSignature.update(mensaje.getBytes(StandardCharsets.UTF_8));
                        byte[] signature = privateSignature.sign();

                        String firma = ToString(signature);
                        long finFirma = System.nanoTime();
                        
                        System.out.println("Tiempo de firma: " + (finFirma - inicioFirma) + " nanosegundos");
                        mt.addTServerSign(finFirma - inicioFirma);
                        output.println("3,Cifra," + firma); 

      
                    }

                    else if (message[0].equals("5")){
                        if (message[1].equals("OK")) {
                            SecureRandom random = new SecureRandom();
                            
                            int var = Math.abs(random.nextInt());
                            Long longvar = Long.valueOf(var);
                            x = BigInteger.valueOf(longvar);  

                            BigInteger bG = BigInteger.valueOf(G);
                            double gx = bG.modPow(x, P).doubleValue();

                            byte[] ivBytes = new byte[16];
                            SecureRandom ra = new SecureRandom();
                            ra.nextBytes(ivBytes);
                            iv = new IvParameterSpec(ivBytes);
                            String ivHex = ToString(ivBytes);

                            String parametros = Integer.toString(G) + "," + P.toString() + "," + Double.toString(gx);

                            try {
                                Signature privateSignature = Signature.getInstance("SHA256withRSA");
                                privateSignature.initSign(privateKey);
                                privateSignature.update(parametros.getBytes(StandardCharsets.UTF_8));
                                byte[] signature = privateSignature.sign();
                                String firma = ToString(signature);

                                output.println("7,Cifra," + parametros + "," +ivHex + "," + firma);
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

                            long inicioAutenticacion = System.nanoTime();
                            String gy  = message[2];
                            Double bigGY = Double.parseDouble(gy);
                            
                            Double semilla = Math.pow(bigGY, x.intValue()) % P.intValue();
                            long finAutenticacion = System.nanoTime();

                            System.out.println("Tiempo de autenticacion SERVIDOR: " + (finAutenticacion - inicioAutenticacion) + " nanosegundos");
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
                            String login = message[1];
                            String contraseña = message[2];

                            byte[] cifradoL = ToByte(login);
                            byte[] cifradoC = ToByte(contraseña);

                            byte[] descifradoL = Cifrados.descifrarSim(k_AB1, iv, cifradoL);
                            byte[] descifradoC = Cifrados.descifrarSim(k_AB1, iv, cifradoC);
                            
                            String descL = ToString(descifradoL);
                            String descC = ToString(descifradoC);

                            String mensajeDescifradoL = hexToString(descL);
                            String mensajeDescifradoC = hexToString(descC);
                            System.out.println("CLIENTE: Mensaje descifrado: " + mensajeDescifradoL + ","+ mensajeDescifradoC);
                        
                        if (mensajeDescifradoL.equals("Login") && mensajeDescifradoC.equals("Contraseña")){
                            
                            output.println("16,OK");
                        }else{
                            output.println("ERROR");
                        }
                    }
                    else if (message[0].equals("17")){
                        long inicioConsulta = System.nanoTime();
                        byte [] consultaCifrada = ToByte(message[1]);
                        byte [] consultaHash = ToByte(message[2]);


                        byte [] descifrar = Cifrados.descifrarSim(k_AB1, iv, consultaCifrada);
                        boolean verificacion = MAC(k_AB2, descifrar, consultaHash);

                        long finConsulta = System.nanoTime();

                        System.out.println("Tiempo de consulta: " + (finConsulta - inicioConsulta) + " nanosegundos");
                        if (verificacion){
                            
                            String valorDescifrado = new String(descifrar, StandardCharsets.UTF_8);
                            int respuesta = Integer.parseInt(valorDescifrado ) -1 ;
                            String str_respuesta = Integer.toString(respuesta);
                            byte [] byte_respuesta = str_respuesta.getBytes();

                            byte [] consulta_rta = Cifrados.cifrarSim(k_AB1, iv, byte_respuesta);
                            byte [] hash_rta = HMAC(k_AB2, byte_respuesta);

                            output.println("19," + ToString(consulta_rta) + "," + ToString(hash_rta)); 

                        }else{
                            output.println("ERROR");
                        }  
                    }
                    else if (message[0].equals("21"))
                    {
                            System.out.println("Termino");
                    }
                    
                }
                
                socket.close();
                System.out.println("Cliente desconectado.");
            }
        } catch (IOException e) {
            System.err.println("Servidor Exception: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
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

    public String hexToString(String hex) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < hex.length(); i += 2) {
            String output = hex.substring(i, (i + 2));
            int decimal = Integer.parseInt(output, 16);
            baos.write(decimal);
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

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



