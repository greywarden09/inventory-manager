package pl.greywarden.tools.configuration;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ObjectMapperConfiguration {
    private final ConfigurableApplicationContext springContext;

    @Bean
    public ObjectMapper objectMapper() {
        var objectMapper = new ObjectMapper();
        var databaseModule = new SimpleModule();
        for (var serializer: springContext.getBeansOfType(JsonSerializer.class).values()) {
            databaseModule.addSerializer(serializer);
        }
        objectMapper.registerModule(databaseModule);
        return objectMapper;
    }
}
