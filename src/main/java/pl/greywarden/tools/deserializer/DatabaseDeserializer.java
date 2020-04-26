package pl.greywarden.tools.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import pl.greywarden.tools.EncryptionType;
import pl.greywarden.tools.model.database.Database;
import pl.greywarden.tools.model.database.DatabaseContent;

import java.io.IOException;
import java.util.Optional;

@Component
public class DatabaseDeserializer extends JsonDeserializer<Database> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Database deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        var node = parser.getCodec().<JsonNode>readTree(parser);

        var path = node.get("path").asText();
        var encryption = node.get("encryption").asBoolean();
        var encryptionType = Optional.ofNullable(node.get("encryptionType"))
                .filter(JsonNode::isTextual)
                .map(JsonNode::asText)
                .map(EncryptionType::valueOf)
                .orElse(null);
        var databaseContent = node.get("databaseContent");

        var database = new Database()
                .withPath(path)
                .withEncryption(encryption)
                .withEncryptionType(encryptionType);

        if (encryption) {
            database.setDatabaseContent(databaseContent.asText());
        } else {
            database.setDatabaseContent(objectMapper.readValue(databaseContent.toString(), DatabaseContent.class));
        }
        return database;
    }
}
