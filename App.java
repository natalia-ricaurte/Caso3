import java.math.BigInteger;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class App {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        int port = 1234;

        // Cifrado
        String hexString = "00b0c35faec4541126981a1f850821d616321dfe294d052cbbd89c96e558c37fa6283f4de181abdea2be172ab3896a5ae212416501f000cef38d0f07d00fbbbbf9bac4ced0ab48ba318707c9df7f8475e54a5760ca53f4eb311c8c76d49fb0938217d089271b49" +
                        "0890d881b78a30176cd40eac7fbd127a17ad2b468591a894d9f7";
        BigInteger P = new BigInteger(hexString, 16);
        Integer G = 2;

        RSAKeyPairGenerator keys = new RSAKeyPairGenerator();
        Key privteKey = keys.getPrivateKey();
        Key publicKey = keys.getPublicKey();

        Servidor servidor = new Servidor(port,P,G,publicKey,privteKey);
        Cliente cliente = new Cliente("localhost", port, P, G,publicKey);

        servidor.start(); // Inicia el servidor en su propio hilo
        try {
            Thread.sleep(1000); // Esperamos un poco antes de iniciar el cliente
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrumpido " + ie.getMessage());
        }
        cliente.start(); // Inicia el cliente en su propio hilo
    }

}