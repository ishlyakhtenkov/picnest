package ru.javaprojects.picnest.app.sociallogin;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import ru.javaprojects.picnest.app.AuthUser;
import ru.javaprojects.picnest.users.model.User;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices.RememberMeTokenAlgorithm.SHA256;

public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final String rememberMeKey;
    private final String rememberMeCookieName;

    public OAuth2AuthenticationSuccessHandler(String successUrl, String rememberMeKey, String rememberMeCookieName) {
        super(successUrl);
        this.rememberMeKey = rememberMeKey;
        this.rememberMeCookieName = rememberMeCookieName;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String token = makeToken(authentication);
        Cookie rememberMe = new Cookie(rememberMeCookieName, Base64.getEncoder().encodeToString(token.getBytes()));
        rememberMe.setPath(getDefaultTargetUrl());
        rememberMe.setMaxAge((int) Duration.ofDays(14).toSeconds());
        response.addCookie(rememberMe);
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private String makeToken(Authentication authentication) {
        User user = ((AuthUser) authentication.getPrincipal()).getUser();
        String username = user.getEmail();
        String password = user.getPassword();
        long tokenExpirationTime = LocalDateTime.now().plusDays(14).toInstant(ZoneOffset.UTC).toEpochMilli();
        String tokenSignature = makeTokenSignature(tokenExpirationTime, username, password);
        return username + ":" + tokenExpirationTime + ":" + SHA256 + ":" + tokenSignature;
    }

    private String makeTokenSignature(long tokenExpiryTime, String username, String password) {
        String data = username + ":" + tokenExpiryTime + ":" + password + ":" + rememberMeKey;
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA256.getDigestAlgorithm());
            return new String(Hex.encode(digest.digest(data.getBytes())));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("No " + SHA256.getDigestAlgorithm() + " algorithm available!");
        }
    }
}
