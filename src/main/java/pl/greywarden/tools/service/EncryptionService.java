package pl.greywarden.tools.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import pl.greywarden.tools.model.EncryptionType;
import pl.greywarden.tools.model.database.DatabaseContent;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EncryptionService {
    private static final String initVector = "PXj0dF5l23qfdzZz";
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        var secretKey = DigestUtils.sha1Hex(encryptionPassword).substring(0, 32);
        var iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
        var secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");


        var cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);

        var decrypted = cipher.doFinal(Base64.decodeBase64(encrypted));
        return objectMapper.readValue(decrypted, DatabaseContent.class);
    }

    @SneakyThrows
    private String encryptUsingAES256CBC(DatabaseContent databaseContent, String encryptionPassword) {
        var secretKey = DigestUtils.sha1Hex(encryptionPassword).substring(0, 32);
        var content = objectMapper.writeValueAsString(databaseContent);

        var iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
        var secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
        var cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);

        byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeBase64String(encrypted);
    }
}
