package Utility;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES
{
	private byte[] key;

	private static final String ALGORITHM = "AES";

	public AES(byte[] key)
	{
		this.key = key;
	}

	/**
	 * Encrypts the given plain text
	 *
	 * @param plainText The plain text to encrypt
	 */
	public byte[] encrypt(byte[] plainText) throws Exception
	{
		SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);

		return cipher.doFinal(plainText);
	}

	/**
	 * Decrypts the given byte array
	 *
	 * @param cipherText The data to decrypt
	 */
	public byte[] decrypt(byte[] cipherText) throws Exception
	{
		SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);

		return cipher.doFinal(cipherText);
	}

	public byte[] getKey() {
		return key;
	}
	
	public static void main(String[] args ) throws Exception {


//		byte[] encryptionKey = "MZygpewJsCpRrfOr".getBytes(StandardCharsets.UTF_8);
//		byte[] plainText = "Hello world!".getBytes(StandardCharsets.UTF_8);
//		AES advancedEncryptionStandard = new AES(
//				encryptionKey);
//		byte[] cipherText = advancedEncryptionStandard.encrypt(plainText);
//		byte[] decryptedCipherText = advancedEncryptionStandard.decrypt(cipherText);
//
//		System.out.println(new String(plainText));
//		System.out.println(new String(cipherText));
//		System.out.println(new String(decryptedCipherText));
		String keyTest = "testing";
		byte[] key = new SHA1().hexaStringToByteArray(new SHA1().sha1(keyTest));
		key = Arrays.copyOf(key, 16);
		System.out.println(key.length);
		String plainText = "hello";
		byte[] plain = plainText.getBytes();
		AES aes = new AES(key);
		byte[] cipher = aes.encrypt(plain);
		System.out.println(new String (cipher));
		System.out.println(new String (aes.decrypt(cipher)));
	}

}