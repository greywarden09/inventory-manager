package pl.greywarden.tools.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.greywarden.tools.model.database.Database;
import pl.greywarden.tools.model.database.DatabaseContent;
import pl.greywarden.tools.service.EncryptionService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class DatabaseSerializer extends JsonSerializer<Database> {
    private final EncryptionService encryptionService;

    @Override
    public void serialize(Database database, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        var encryption = database.isEncryption();
        var encryptionType = database.getEncryptionType();
        var databaseContent = database.<DatabaseContent>getDatabaseContent();
        var encryptionPassword = database.getEncryptionPassword();

        gen.writeStartObject();
        gen.writeStringField("path", database.getPath());
        gen.writeBooleanField("encryption", encryption);
        gen.writeObjectField("encryptionType", encryptionType);

        if (encryption) {
            gen.writeStringField("databaseContent", encryptionService.encryptDatabaseContent(databaseContent, encryptionPassword, encryptionType));
        } else {
            gen.writeObjectField("databaseContent", databaseContent);
        }

        gen.writeEndObject();
    }

    @Override
    public Class<Database> handledType() {
        return Database.class;
    }
}
