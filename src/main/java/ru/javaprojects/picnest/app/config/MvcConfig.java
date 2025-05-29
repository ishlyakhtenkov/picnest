package ru.javaprojects.picnest.app.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.DispatcherServletWebRequest;
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

    @Value("${content-path.photos-uri}")
    private String photosUri;

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

    private final HandlerInterceptor photoInterceptor = new WebRequestHandlerInterceptorAdapter(new WebRequestInterceptor() {
        @Override
        public void preHandle(WebRequest request) {
            String requestURI = ((DispatcherServletWebRequest) request).getRequest().getRequestURI();
            AuthUser authUser = AuthUser.safeGet();
            if (authUser == null || !requestURI.startsWith(photosUri + authUser.id() + "/")) {
                throw new AccessDeniedException("Access for request=" + requestURI + " denied for user=" + authUser);
            }
        }

        @Override
        public void postHandle(WebRequest request, ModelMap model) {
        }

        @Override
        public void afterCompletion(WebRequest request, Exception ex) {
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
        registry.addInterceptor(photoInterceptor).addPathPatterns(photosUri +  "**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/picnest/content/**").addResourceLocations("file:./picnest/content/");
        registry.setOrder(Integer.MAX_VALUE);
    }
}
