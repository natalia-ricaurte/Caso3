

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class principal {
	public final static String textoSim = "Mensaje Simétrico";
	public final static String textoAsim = "Mensaje Asimétrico";
	
	public static void main(String[] args) throws NoSuchAlgorithmException {
		simetrico();
		System.out.println();
		Asimetrico();
	}
	
	public static void simetrico() throws NoSuchAlgorithmException {
		System.out.println("Texto a cifrar: " + textoSim);
		
		KeyGenerator keygenerator = KeyGenerator.getInstance("AES"); 
        SecretKey key = keygenerator.generateKey();
		
		byte[] cifradoSim = cifradoSimétrico.cifrar(key, textoAsim);
		System.out.println("Texto cifrado: " + cifradoSim);
		
		byte[] descifradoSim = cifradoSimétrico.descifrar(key, cifradoSim);
		String descifradoClaro = new String(descifradoSim, StandardCharsets.UTF_8);
		System.out.println("Texto descifrado: " + descifradoClaro);
	}
	
	public static void Asimetrico() throws NoSuchAlgorithmException {
		System.out.println("Texto a cifrar: " + textoAsim);
		
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(1024);
		KeyPair keyPair = generator.generateKeyPair();
		PublicKey publica = keyPair.getPublic();
		PrivateKey privada = keyPair.getPrivate();
		

		byte[] cifradoSim = CifradoAsimétrico.cifrar(publica, "RSA", textoAsim);
		System.out.println("Texto cifrado: " + cifradoSim);
		
		byte[] descifradoSim = CifradoAsimétrico.descifrar(privada, "RSA", cifradoSim);
		String descifradoClaro = new String(descifradoSim, StandardCharsets.UTF_8);
		System.out.println("Texto descifrado: " + descifradoClaro);
	}
}
