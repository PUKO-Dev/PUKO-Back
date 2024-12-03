package edu.escuelaing.arsw.puko.config;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Encryption {
    private Encryption() {
    }

    private static final String ALGORITHM_ENCRYPT = "${ALGORITHM_ENCRYPT}";
    private static final String SECRET_KEY_ENCRYPT = "${SECRET-KEY}";
    private static final String ALGORITHM_DECRYPT = "${ALGORITHM_DECRYPT}";
    private static final String SECRET_KEY_DECRYPT = "${SECRET-DECRYP-KEY}";

    public static String encrypt(String data){
        try{
            Cipher cipher = Cipher.getInstance(ALGORITHM_ENCRYPT);
            cipher.init(Cipher.ENCRYPT_MODE, new javax.crypto.spec.SecretKeySpec(SECRET_KEY_ENCRYPT.getBytes(), ALGORITHM_ENCRYPT));
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedData);
        }catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException |
                IllegalBlockSizeException e){
            return "Try again later or reload the page";
        }

    }
    public static String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_DECRYPT);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(SECRET_KEY_DECRYPT.getBytes(), "AES"));
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedData = cipher.doFinal(decodedData);
            return new String(decryptedData);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error decrypting data";
        }
    }
}