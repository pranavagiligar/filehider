package com.quadcore.filehider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
	public static String hash(String message) {
		String salt = "%it%is%my%salt%";
		String text = message + salt;
		
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		md.update(text.getBytes());
		byte [] dig = md.digest();
		
		StringBuffer sb = new StringBuffer();
		for(byte b : dig) {
			sb.append(String.format("%02x", b & 0xff));
		}
		text = sb.toString();
		return text;
	}
}
