package com.magicauth.service;

import com.magicauth.model.MagicToken;
import com.magicauth.model.User;
import com.magicauth.repository.MagicTokenRepository;
import com.magicauth.repository.UserRepository;
import com.magicauth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final MagicTokenRepository magicTokenRepository;
    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sends a magic login link to the user's email.
     * Creates the user if they don't exist yet.
     */
        public void sendMagicLink(String email) {
        // Find or create user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(new User(email)));

        magicTokenRepository.markAllTokensAsUsed(user.getId());

        // Generate a secure token (UUID)
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        //hash the token
        String hashedToken = DigestUtils.sha256Hex(rawToken);

        // Token expires in 15 minutes
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
        MagicToken magicToken = new MagicToken(hashedToken, user, expiresAt);
        magicTokenRepository.save(magicToken);

        // Build the magic link pointing to the React frontend
        String magicLink = frontendUrl + "/verify?token=" + rawToken;

        // Send email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Your Magic Login Link");
        message.setText(
            "Hello!\n\n" +
            "Click the link below to sign in. This link will expire in 15 minutes.\n\n" +
            magicLink + "\n\n" +
            "If you didn't request this, you can safely ignore this email.\n\n" +
            "— The Team"
        );

        mailSender.send(message);
    }

    /**
     * Verifies the magic token and returns a JWT if valid.
     */
    @Transactional
    public String verifyMagicToken(String rawToken) {
        String hashedToken = DigestUtils.sha256Hex(rawToken);
        MagicToken magicToken = magicTokenRepository.findByToken(hashedToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token."));

        if (magicToken.isUsed()) {
            throw new IllegalArgumentException("This link has already been used.");
        }
        if (magicToken.isExpired()) {
            throw new IllegalArgumentException("This link has expired. Please request a new one.");
        }

        // Mark token as used
        magicToken.setUsed(true);
        magicTokenRepository.save(magicToken);

        // Issue JWT for the user
        return jwtUtil.generateToken(magicToken.getUser().getEmail());
    }

    /**
     * Returns user info from a valid JWT.
     */
    public String getEmailFromJwt(String jwt) {
        return jwtUtil.extractEmail(jwt);
    }
}
