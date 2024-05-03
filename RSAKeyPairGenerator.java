import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class RSAKeyPairGenerator {
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public RSAKeyPairGenerator() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void printKeys() {
        System.out.println("Public Key: " + getPublicKey().getEncoded());
        System.out.println("Private Key: " + getPrivateKey().getEncoded());
    }

    public static void main(String[] args) {
        try {
            RSAKeyPairGenerator keyPairGenerator = new RSAKeyPairGenerator();
            keyPairGenerator.printKeys();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Exception encountered in keyPairGenerator");
            e.printStackTrace();
        }
    }
}
