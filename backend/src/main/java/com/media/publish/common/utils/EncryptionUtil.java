package com.media.publish.common.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    public static String encrypt(String content, String secretKey) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(
            hashKey(secretKey).getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encryptedContent, String secretKey) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(
            hashKey(secretKey).getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedContent));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    private static String hashKey(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(key.getBytes(StandardCharsets.UTF_8));
            return new String(hash, 0, 16, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return key;
        }
    }

    public static String mask(String content) {
        if (content == null || content.length() <= 8) {
            return "******";
        }
        return content.substring(0, 4) + "******" + content.substring(content.length() - 4);
    }
}
