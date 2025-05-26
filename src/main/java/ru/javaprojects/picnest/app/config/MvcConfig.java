package ru.javaprojects.picnest.app.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import ru.javaprojects.picnest.app.AuthUser;

import java.time.Duration;
import java.util.Locale;

@Configuration
@RequiredArgsConstructor
public class MvcConfig implements WebMvcConfigurer {

    @Value("${locale.default}")
    private String defaultLocale;

    // Add authUser to view model
    private final HandlerInterceptor authInterceptor = new WebRequestHandlerInterceptorAdapter(new WebRequestInterceptor() {
        @Override
        public void postHandle(WebRequest request, ModelMap model) {
            if (model != null) {
                AuthUser authUser = AuthUser.safeGet();
                if (authUser != null) {
                    model.addAttribute("authUser", authUser.getUser());
                }
            }
        }

        @Override
        public void afterCompletion(WebRequest request, Exception ex) {
        }

        @Override
        public void preHandle(WebRequest request) {
        }
    });

    @Bean
    LocaleResolver localeResolver() {
        var cookieLocaleResolver = new CookieLocaleResolver();
        cookieLocaleResolver.setDefaultLocale(Locale.of(defaultLocale));
        cookieLocaleResolver.setCookieMaxAge(Duration.ofDays(60));
        return cookieLocaleResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        var localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        return localeChangeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor()).excludePathPatterns("/js/**", "/webjars/**", "/css/**", "/images/**");
        registry.addInterceptor(authInterceptor).excludePathPatterns("/js/**", "/webjars/**", "/css/**", "/images/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/content/**").addResourceLocations("file:./content/");
        registry.setOrder(Integer.MAX_VALUE);
    }
}
