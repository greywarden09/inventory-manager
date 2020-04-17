package pl.greywarden.tools.configuration;

import com.google.common.eventbus.EventBus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.greywarden.tools.listener.EventListener;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class EventBusConfiguration {
    private final ConfigurableApplicationContext springContext;

    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }

    @PostConstruct
    public void initializeListeners() {
        var eventBus = eventBus();
        springContext.getBeansOfType(EventListener.class).values().forEach(eventBus::register);
    }

}
