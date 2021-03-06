package pl.greywarden.tools.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.greywarden.tools.model.EncryptionType;
import pl.greywarden.tools.model.database.DatabaseContent;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EncryptionService {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${security.init-vector}")
    private byte[] initVector;

    @Value("${security.salt}")
    private byte[] salt;

    public String encryptDatabaseContent(DatabaseContent databaseContent, String encryptionPassword, EncryptionType encryptionType) {
        switch (encryptionType) {
            case AES256CBC:
                return encryptUsingAES256CBC(databaseContent, encryptionPassword);
            default:
                throw new UnsupportedOperationException();
        }
    }

    public DatabaseContent decryptDatabaseContent(String encrypted, String encryptionPassword, EncryptionType encryptionType) throws Exception {
        switch (encryptionType) {
            case AES256CBC:
                return decryptUsingAES256CBC(encrypted, encryptionPassword);
            default:
                throw new UnsupportedOperationException();
        }
    }


    private DatabaseContent decryptUsingAES256CBC(String encrypted, String encryptionPassword) throws Exception {
        var cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

        var factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        var keySpec = new PBEKeySpec(encryptionPassword.toCharArray(), salt, 65536, 256);
        var secretKey = new SecretKeySpec(factory.generateSecret(keySpec).getEncoded(), "AES");
        var iv = new IvParameterSpec(initVector);

        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

        var decrypted = cipher.doFinal(Base64.decodeBase64(encrypted));
        return objectMapper.readValue(decrypted, DatabaseContent.class);
    }

    @SneakyThrows
    private String encryptUsingAES256CBC(DatabaseContent databaseContent, String encryptionPassword) {
        var content = objectMapper.writeValueAsString(databaseContent);

        var cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        var factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        var keySpec = new PBEKeySpec(encryptionPassword.toCharArray(), salt, 65536, 256);
        var secretKey = new SecretKeySpec(factory.generateSecret(keySpec).getEncoded(), "AES");
        var iv = new IvParameterSpec(initVector);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

        byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeBase64String(encrypted);
    }
}
