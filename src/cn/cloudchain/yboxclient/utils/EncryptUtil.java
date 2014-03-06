package cn.cloudchain.yboxclient.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加密类
 * 
 * @author lazzy
 * 
 */
public class EncryptUtil {

	private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String toHexString(byte[] b) { // String to byte
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
			sb.append(HEX_DIGITS[b[i] & 0x0f]);
		}
		return sb.toString();
	}

	public static final String getMD5String(String path) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(path.getBytes());
			byte messageDigest[] = digest.digest();
			return toHexString(messageDigest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return path;

	}

	public static final String getSHA1(String data) {
		StringBuilder builder = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] bytes = md.digest(data.getBytes());
			builder = new StringBuilder();
			for (byte b : bytes) {
				builder.append(String.format("%02x", b));
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

	public static String hmacSha1(String value, String key) {
		try {
			// Get an hmac_sha1 key from the raw key bytes
			byte[] keyBytes = key.getBytes();
			SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

			// Get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);

			// Compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(value.getBytes());

			// Convert raw bytes to Hex
			return Base64.encodeToString(rawHmac, Base64.URL_SAFE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
