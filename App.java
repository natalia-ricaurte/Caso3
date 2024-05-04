import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class App {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        int port = 1234;

        // Cifrado
        String hexString = "00b3ac49dc4df4793c296113a6b5c1398180ca7607897c311c16d11e695dfa03" +
                        "f9ccb1e826b394208f697cac3b26caa299ffeebabbebb91006108818669e1787" +
                        "f2b0355f58aa04e6b4cf27edcb773ce482b5c058cc5c23f605ecb0f60f7ba593" +
                        "3f1e252d0cdd8715152939300af4b02a52f33b443fa55df5b4952721c3d799e6" +
                        "9f";
        BigInteger P = new BigInteger(hexString, 16);
        Integer G = 2;

        RSAKeyPairGenerator keys = new RSAKeyPairGenerator();
        PrivateKey privteKey = keys.getPrivateKey();
        PublicKey publicKey = keys.getPublicKey();

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