package com.antonsteve96.ngrx.auth;

import com.antonsteve96.ngrx.email.EmailService;
import com.antonsteve96.ngrx.email.EmailTemplateName;
import com.antonsteve96.ngrx.role.RoleRepository;
import com.antonsteve96.ngrx.security.JwtService;
import com.antonsteve96.ngrx.user.Token;
import com.antonsteve96.ngrx.user.TokenRepository;
import com.antonsteve96.ngrx.user.User;
import com.antonsteve96.ngrx.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    public void register(RegistrationRequest request) throws MessagingException {
        log.info("Registering new user with email: {}", request.getEmail());
        var userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> {
                    log.error("ROLE USER was not initiated");
                    return new IllegalStateException("ROLE USER was not initiated");
                });
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .createdDate(LocalDateTime.now())
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .build();

        log.info("Registering user with id", user);
        userRepository.save(user);

        log.info("User registered successfully. Sending validation email to: {}", user.getEmail());
        sendValidationEmail(user);

    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("Authenticating user with email: {}", request.getEmail());
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var claims = new HashMap<String, Object>();
        var user = ((User) auth.getPrincipal());
        claims.put("fullName", user.getFullName());

        var jwtToken = jwtService.generateToken(claims, user);
        log.info("User authenticated successfully: {}", user.getEmail());
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    @Transactional
    public void activateAccount(String token) throws MessagingException {
        log.info("Activating account with token: {}", token);
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.error("Invalid token: {}", token);
                    return new RuntimeException("Invalid token");
                });
        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            log.warn("Activation token has expired for token: {}. Sending new token.", token);
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation token has expired. A new token has been send to the same email address");
        }

        var user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> {
                    log.error("User not found for token: {}", token);
                    return new UsernameNotFoundException("User not found");
                });
        user.setEnabled(true);
        userRepository.save(user);

        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
        log.info("Account activated successfully for user: {}", user.getEmail());
    }

    private String generateAndSaveActivationToken(User user) {
        log.info("Generating activation token for user: {}", user.getEmail());
        String generatedToken = generateActivationCode();
        var token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(60*24))
                .user(user)
                .build();
        tokenRepository.save(token);
        log.info("Activation token generated and saved for user: {}", user.getEmail());
        return generatedToken;
    }

    private void sendValidationEmail(User user) throws MessagingException {
        log.info("Sending validation email to user: {}", user.getEmail());
        var newToken = generateAndSaveActivationToken(user);
        String emailSubject = "Attivazione account";
        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken,
                emailSubject
        );
        log.info("Validation email sent to user: {}", user.getEmail());
    }

    private String generateActivationCode() {
        log.debug("Generating activation code of length: {}", 6);
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        int activationCodeLength = 6;
        for (int i = 0; i < activationCodeLength; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }
        log.debug("Activation code generated: {}", codeBuilder);
        return codeBuilder.toString();
    }
}
