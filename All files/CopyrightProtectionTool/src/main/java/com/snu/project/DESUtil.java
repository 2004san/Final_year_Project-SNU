package com.snu.project;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.util.Base64;

public class DESUtil {

    // Generate a secret key from password
    private static SecretKey getKeyFromPassword(String password) throws Exception {
        byte[] keyBytes = password.getBytes("UTF-8");
        // DES key must be exactly 8 bytes
        byte[] keyPadded = new byte[8];
        System.arraycopy(keyBytes, 0, keyPadded, 0, Math.min(keyBytes.length, 8));
        DESKeySpec keySpec = new DESKeySpec(keyPadded);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        return keyFactory.generateSecret(keySpec);
    }

    public static String encrypt(String data, String password) throws Exception {
        SecretKey key = getKeyFromPassword(password);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedData, String password) throws Exception {
        SecretKey key = getKeyFromPassword(password);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes, "UTF-8");
    }
}

