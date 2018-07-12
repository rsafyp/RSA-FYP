package Utility;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1 {
	public String sha1(String input) throws NoSuchAlgorithmException {
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
		byte[] result = mDigest.digest(input.getBytes());
		
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < result.length; i++) {
			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
		}
		
		return sb.toString();
	}
	
	public byte[] hexaStringToByteArray (String text) {
		byte[]	values = new byte[text.length() / 2];
		for (int i = 0; i  < text.length(); i = i+2) {
			values[i / 2] = (byte) ((Character.digit(text.charAt(i), 16) << 4) + Character.digit(text.charAt(i + 1), 16));
		}
		
		return values;
	}
	
	public static void main (String[] args) throws Exception {
		String text = "testing";
		String msgDigest = new SHA1().sha1(text);
		byte[] v = new SHA1().hexaStringToByteArray(msgDigest);
		for (int i = 0; i < v.length; i++) {
			System.out.print(v[i] + "|");
		}
		System.out.println();
		System.out.println(v.length);
	}
}
