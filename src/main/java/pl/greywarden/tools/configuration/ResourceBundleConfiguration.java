package pl.greywarden.tools.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.greywarden.tools.service.ApplicationSettingsService;

import java.util.ResourceBundle;

@Configuration
@RequiredArgsConstructor
class ResourceBundleConfiguration {
    private final ApplicationSettingsService applicationSettingsService;

    @Bean
    ResourceBundle resourceBundle() {
        return ResourceBundle.getBundle("i18n/strings", applicationSettingsService.getLocale());
    }
}
