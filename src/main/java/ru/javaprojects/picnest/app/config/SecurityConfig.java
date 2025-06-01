package ru.javaprojects.picnest.app.config;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import ru.javaprojects.picnest.app.AuthUser;
import ru.javaprojects.picnest.app.sociallogin.AppOAuth2UserService;
import ru.javaprojects.picnest.app.sociallogin.OAuth2AuthenticationSuccessHandler;
import ru.javaprojects.picnest.common.error.NotFoundException;
import ru.javaprojects.picnest.users.model.Role;
import ru.javaprojects.picnest.users.model.User;
import ru.javaprojects.picnest.users.service.UserService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    public static final PasswordEncoder PASSWORD_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final UserService userService;
    private final SessionRegistry sessionRegistry;
    private final UserMdcLoggingFilter userMdcLoggingFilter;
    private final AppOAuth2UserService appOAuth2UserService;

    @Value("${remember-me.key}")
    private String rememberMeKey;

    @Value("${remember-me.cookie-name}")
    private String rememberMeCookieName;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PASSWORD_ENCODER;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            try {
                User user = userService.getByEmail(email);
                return new AuthUser(user);
            } catch (NotFoundException ex) {
                throw new UsernameNotFoundException(ex.getMessage());
            }
        };
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            String error = (exception instanceof DisabledException) ? "disabled-credentials" : "bad-credentials";
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.sendRedirect("/login?error=" + error);
        };
    }

    @Bean
    public OAuth2AuthenticationSuccessHandler oAuth2AuthSuccessHandler() {
        return new OAuth2AuthenticationSuccessHandler("/", rememberMeKey, rememberMeCookieName);
    }



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .addFilterAfter(userMdcLoggingFilter, AuthorizationFilter.class)
                .authorizeHttpRequests((authorizeHttpRequests) ->
                        authorizeHttpRequests
                                .requestMatchers("/management/**").hasRole(Role.ADMIN.name())
                                .requestMatchers("/register/**", "/profile/forgot-password", "/profile/reset-password",
                                        "/login").anonymous()
                                .requestMatchers(HttpMethod.GET, "/", "/search/**", "/about", "/contact",
                                        "/profile/*/view", "/profile/by-keyword",
                                        "/webjars/**", "/css/**", "/images/**", "/js/**", "/picnest/content/**").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin((formLogin) ->
                        formLogin
                                .permitAll()
                                .loginPage("/login")
                                .defaultSuccessUrl("/", true)
                                .failureHandler(authenticationFailureHandler())
                )
                .oauth2Login((oauth2Login) ->
                        oauth2Login
                                .loginPage("/login")
                                .successHandler(oAuth2AuthSuccessHandler())
                                .tokenEndpoint((tokenEndpoint) ->
                                        tokenEndpoint.accessTokenResponseClient(accessTokenResponseClient()))
                                .userInfoEndpoint((userInfoEndpoint) ->
                                        userInfoEndpoint.userService(appOAuth2UserService))
                                .failureHandler(authenticationFailureHandler())
                )
                .logout((logout) ->
                        logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/")
                                .invalidateHttpSession(true)
                                .clearAuthentication(true)
                                .deleteCookies("JSESSIONID")
                )
                .rememberMe((rememberMe) ->
                        rememberMe
                                .key(rememberMeKey)
                                .rememberMeCookieName(rememberMeCookieName))
                .sessionManagement((sessionManagement) ->
                        sessionManagement
                                .maximumSessions(5)
                                .sessionRegistry(sessionRegistry));
        return http.build();
    }

    // Need only for VK provider tokens
    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        var accessTokenResponseClient = new RestClientAuthorizationCodeTokenResponseClient();
        var tokenResponseHttpMessageConverter = new OAuth2AccessTokenResponseHttpMessageConverter();
        tokenResponseHttpMessageConverter.setAccessTokenResponseConverter(new TokenResponseConverter());
        var restTemplate = new RestTemplate(Arrays.asList(new FormHttpMessageConverter(),
                tokenResponseHttpMessageConverter));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        accessTokenResponseClient.setRestClient(RestClient.create(restTemplate));
        return accessTokenResponseClient;
    }

    private static class TokenResponseConverter implements Converter<Map<String, Object>, OAuth2AccessTokenResponse> {
        @Override
        public OAuth2AccessTokenResponse convert(Map<String, Object> tokenResponseParameters) {
            String accessToken = (String) tokenResponseParameters.get(OAuth2ParameterNames.ACCESS_TOKEN);
            var accessTokenType = OAuth2AccessToken.TokenType.BEARER;
            Map<String, Object> additionalParameters = new HashMap<>(tokenResponseParameters);
            return OAuth2AccessTokenResponse.withToken(accessToken)
                    .tokenType(accessTokenType)
                    .additionalParameters(additionalParameters)
                    .build();
        }
    }
}
