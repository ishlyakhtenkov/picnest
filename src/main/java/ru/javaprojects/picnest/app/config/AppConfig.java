package ru.javaprojects.picnest.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.session.AbstractSessionEvent;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.thymeleaf.dialect.springdata.SpringDataDialect;
import ru.javaprojects.picnest.app.AuthUser;
import ru.javaprojects.picnest.common.util.JacksonObjectMapper;

@Configuration
@EnableScheduling
@EnableCaching
@EnableEncryptableProperties
public class AppConfig {
    @Bean
    public SpringDataDialect springDataDialect() {
        return new SpringDataDialect();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JacksonObjectMapper.getMapper();
    }

    @Bean
    public MessageSource messageSource() {
        var messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new AppSessionRegistry();
    }

    public static class AppSessionRegistry extends SessionRegistryImpl {
        @Override
        public void onApplicationEvent(AbstractSessionEvent event) {
            super.onApplicationEvent(event);
            if (event instanceof HttpSessionCreatedEvent sessionCreatedEvent) {
                String sessionId = sessionCreatedEvent.getSession().getId();
                if (getSessionInformation(sessionId) == null && AuthUser.safeGet() != null) {
                    registerNewSession(sessionId, AuthUser.safeGet());
                }
            }
        }
    }
}
