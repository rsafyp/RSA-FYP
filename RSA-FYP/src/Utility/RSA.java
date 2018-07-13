package Utility;

import java.math.BigInteger;
import java.util.Random;


public class RSA {
	/* 
	 * privateKey[0] = d;
	 * privateKey[1] = n;
	 */
	private BigInteger[] privateKey = new BigInteger[2];

	/* 
	 * publicKey[0] = e;
	 * publicKey[1] = n;
	 */
	private BigInteger[] publicKey = new BigInteger [2];

	// key size of the modulus
	// default is 64 bits 
	private int numBits;

	public RSA() {
		
	}
	
	public RSA(int numBits) {
		if (numBits < 64)
			numBits = 64;
		this.numBits = numBits / 2;
		RSAKeyGenerate();
	}

	// get public key
	public void getPublicKey(BigInteger[] pk) {
		pk[0] = publicKey[0];
		pk[1] = publicKey[1];
	}

	// get private key
	public void getPrivateKey(BigInteger[] pk) {
		pk[0] = privateKey[0];
		pk[1] = privateKey[1];
	}

	/*
	 * RSA encrypt
	 * Take any string as input, return a string consisting of encrypted values of each char separated by "|"
	 */
	public String RSAEncrypt (String text) {
		BigInteger[] cipher = null;
		if (text.length() % 5 == 0) {
			cipher = new BigInteger[text.length() / 5];
		} else
			cipher = new BigInteger[text.length() / 5 + 1];
		String cipherText = "";
		byte[] b = new byte[5];
		int j = 0;
		int count = 0;
		for (int i = 0; i < text.length(); i = i + 5) {
			count = 0;
			for (int k = i; k < i + 5 && k < text.length(); k++) {
				b[k % 5] = (byte) text.charAt(k);
				count++;
			}
			if (count != 5) {
				while (count < 5) {
					b[count] = 0;
					count++;
				}
			}
			cipher[j] = fastExponentiation(new BigInteger(b), publicKey[0], publicKey[1]);
			cipherText = cipherText + cipher[j].toString() + "|";
			j++;
		}

		// remove last '|'
		if (cipherText.charAt(cipherText.length() - 1) == '|')
			cipherText = cipherText.substring(0, cipherText.length() - 1);

		return cipherText.trim();
	}

	/*
	 * RSA encrypt
	 * Take any string as input, return a string consisting of encrypted values of each char separated by "|"
	 */
	public String RSAEncrypt (String text, BigInteger e, BigInteger n) {
		BigInteger[] cipher = null;
		if (text.length() % 5 == 0) {
			cipher = new BigInteger[text.length() / 5];
		} else
			cipher = new BigInteger[text.length() / 5 + 1];
		String cipherText = "";
		byte[] b = new byte[5];
		int j = 0;
		int count = 0;
		for (int i = 0; i < text.length(); i = i + 5) {
			count = 0;
			for (int k = i; k < i + 5 && k < text.length(); k++) {
				b[k % 5] = (byte) text.charAt(k);
				count++;
			}
			if (count != 5) {
				while (count < 5) {
					b[count] = 0;
					count++;
				}
			}
			cipher[j] = fastExponentiation(new BigInteger(b), e, n);
			cipherText = cipherText + cipher[j].toString() + "|";
			j++;
		}

		// remove last '|'
		if (cipherText.charAt(cipherText.length() - 1) == '|')
			cipherText = cipherText.substring(0, cipherText.length() - 1);

		return cipherText.trim();
	}

	/*
	 * RSA decrypt
	 * Take a string consisting of encrypted values of each char separated by "|" as input
	 * Return a string
	 */
	public String RSADecrypt(String cipherText) {
		// convert cipherText into long array
		String[] str;
		str = cipherText.split("[|]");
		// decrypt
		String result = "";
		for (int i = 0; i < str.length; i++) {
			result = result + new String(fastExponentiation(new BigInteger(str[i]), privateKey[0], privateKey[1]).toByteArray());
		}
		return result.trim();
	}

	private void RSAKeyGenerate() {
		//		// generate 2 primes p, q with specified bit length
		//		BigInteger p = BigInteger.ZERO, q = BigInteger.ZERO;
		//		Random r = new Random();
		//		boolean state = true;
		//
		//		// continue to generate a value until it is prime
		//		while (state) {
		//			p = new BigInteger(numBits, 10, r);
		//			if (p.isProbablePrime(10))
		//				state = false;
		//		}
		//		state = true;
		//		while (state) {
		//			q = new BigInteger(numBits, 10, r);
		//			if (q.isProbablePrime(10) && p.compareTo(q) != 0)
		//				state = false;
		//		}
		//
		//		// calculate n
		//		BigInteger n = p.multiply(q);

		// TODO same modulus n
		boolean state;
		Random r = new Random();
		BigInteger p = new BigInteger("4199861993");
		BigInteger q = new BigInteger("1572754699");
		BigInteger n = p.multiply(q);
		// TODO delete above after testing

		// calculate EulerTotientN
		BigInteger eulerTotientN = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

		// choose e and calculate d accordingly
		BigInteger[] result = new BigInteger[3];
		state = true;
		BigInteger e = null;
		while (state) {
			e = new BigInteger(numBits, r);
			if (e.compareTo(BigInteger.ONE) > 0 && e.compareTo(eulerTotientN) < 0) {
				extendedEuclideanAlgo(e, eulerTotientN, result);
				if (result[0].compareTo(BigInteger.ONE) ==  0) 
					state = false;
			}
		}
		BigInteger d = result[2];
		if (d.compareTo(BigInteger.ZERO) < 0)
			d = d.add(eulerTotientN);

		privateKey[0] = d;
		privateKey[1] = n;
		publicKey[0] = e;
		publicKey[1] = n;
	}

	// implement extended Euclidean algodithm return an array of results
	// result[0] = gcd, result[1] = a, result[2] = b
	private void extendedEuclideanAlgo(BigInteger n1, BigInteger n2, BigInteger[] result) {
		if (n2.compareTo(n1) > 0) {
			BigInteger temp = n1;
			n1 = n2;
			n2 = temp;
		}

		BigInteger a1 = BigInteger.ONE, b1 = BigInteger.ZERO, a2 = BigInteger.ZERO, b2 = BigInteger.ONE;
		BigInteger q, r;
		boolean state = true;
		while (state) {
			q = n1.divide(n2);
			r = n1.mod(n2);
			if (r.compareTo(BigInteger.ZERO) == 0)
				state = false;
			else {
				n1 = n2;
				n2 = r;
				BigInteger t = a2;
				a2 = a1.subtract(q.multiply(a2));
				a1 = t;
				t = b2;
				b2 = b1.subtract(q.multiply(b2));
				b1 = t;
			}
		}

		result[0] = n2;
		result[1] = a2;
		result[2] = b2;
	}

	// implement fast exponentiation algorithm
	private BigInteger fastExponentiation(BigInteger base, BigInteger exponent, BigInteger modulo) {
		String binary = exponent.toString(2);
		BigInteger t = base;
		for (int i = 1; i < binary.length(); i++) {
			if (binary.charAt(i) == '1') {
				t = t.multiply(t).multiply(base);
				if (t.compareTo(modulo) > 0)
					t = t.mod(modulo);
			}
			else {
				t = t.multiply(t);
				if (t.compareTo(modulo) > 0)
					t = t.mod(modulo);
			}

		}
		return t;
	}

	public String commonModulusAttack (String cipher1, String cipher2, BigInteger public1, BigInteger public2, BigInteger modulus) {
		// using extended Euclidean algorithm to find a, b such that public1 * a + public2 * b = 1
		BigInteger[] result = new BigInteger[3];
		extendedEuclideanAlgo (public1, public2, result);
		BigInteger a = null;
		BigInteger b =null;	

		// if gcd = 1, start common modulus attack
		// else return null 
		if (result[0].compareTo(BigInteger.ONE) == 0) {
			if (public1.compareTo(public2) > 0) {
				a = result[1];
				b = result[2];
			} else {
				a = result[2];
				b = result[1];
			}

			// convert cipher into array
			String[] str1 = cipher1.split("[|]");
			String[] str2 = cipher2.split("[|]");

			boolean negA = false;
			boolean negB = false;
			if (a.compareTo(BigInteger.ZERO) < 0) 
				negA = true;
			if (b.compareTo(BigInteger.ZERO) < 0)
				negB = true;

			// decrypt using common modulus attack
			String plain = "";
			for (int i = 0; i < str1.length; i++) {
				// C1 = (cipher1)^a mod n
				BigInteger C1 = null;
				if (negA) {
					BigInteger k = new BigInteger(str1[i]).modInverse(modulus);
					C1 = fastExponentiation(k, BigInteger.ZERO.subtract(a), modulus);
				} 
				else
					C1 = fastExponentiation(new BigInteger(str1[i]), a, modulus);

				// C2 = (cipher2)^b mod n
				BigInteger C2 = null;
				if (negB) {
					BigInteger k = new BigInteger(str2[i]).modInverse(modulus);
					C2 = fastExponentiation(k, BigInteger.ZERO.subtract(b), modulus);
				} 
				else
					C2 = fastExponentiation(new BigInteger(str2[i]), b, modulus);

				plain = plain + new String (C1.multiply(C2).mod(modulus).toByteArray());
			}

			return plain;
		}
		else 
			return null;
	}

	public static void main(String[] args) throws Exception {
		RSA rsa = new RSA(64);
		BigInteger[] publicKey = new BigInteger[2];
		BigInteger[] privateKey = new BigInteger[2];
		rsa.getPublicKey(publicKey);
		rsa.getPrivateKey(privateKey);
		System.out.println("Public key: ");
		System.out.println("e = " + publicKey[0].toString(10));
		System.out.println("n = " + publicKey[1].toString());
		System.out.println("Private key: ");
		System.out.println("d = " + privateKey[0].toString());
		System.out.println("n = " + privateKey[1].toString());

		String plainText = "hi how are you i'm fine thank you";
		String cipherText = rsa.RSAEncrypt(plainText);
		System.out.println("Cipher text: " + cipherText);
		System.out.println("Plain text: " + rsa.RSADecrypt(cipherText));
	}
}
