import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Cifrados {
	
	public static byte[] cifrarAsim(Key llave, String algoritmo, String texto) {
		byte[] textoCifrado;
		
		try {
			Cipher cifrador = Cipher.getInstance(algoritmo);
			byte[] textoClaro = texto.getBytes();
			
			cifrador.init(Cipher.ENCRYPT_MODE, llave);
			textoCifrado = cifrador.doFinal(textoClaro);
			
			return textoCifrado;
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			return null;
		}
	}
	
	public static byte[] descifrarAsim(Key llave, String algoritmo, byte[] texto) {
		byte[] textoClaro;
		
		try {
			Cipher cifrador = Cipher.getInstance(algoritmo);
			cifrador.init(Cipher.DECRYPT_MODE, llave);
			textoClaro = cifrador.doFinal(texto);
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			return null;
		}
		return textoClaro;
	}

	private static final String PADDING = "AES/CBC/PKCS5Padding";
    
    public static byte[] cifrarSim(SecretKey llave, IvParameterSpec ivSpec, byte[] texto) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        
		Cipher cifrador = null;
		try {
            cifrador = Cipher.getInstance(PADDING);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

		cifrador.init(Cipher.ENCRYPT_MODE, llave, ivSpec);
		byte[] textoCifrado = cifrador.doFinal(texto);
		return textoCifrado;
    }
    
    public static byte[] descifrarSim(SecretKey llave, IvParameterSpec ivSpec, byte[] textoCifrado) {
        try {
            Cipher descifrador = Cipher.getInstance(PADDING);
            descifrador.init(Cipher.DECRYPT_MODE, llave, ivSpec);
            return descifrador.doFinal(textoCifrado);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}