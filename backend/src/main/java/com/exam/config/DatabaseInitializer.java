package com.exam.config;

import com.exam.entity.Role;
import com.exam.entity.User;
import com.exam.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.admin.name:System Administrator}")
    private String adminName;

    public DatabaseInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Enforce rule: No fake exams, questions, students or results.
        // Create initial admin ONLY if credentials provided in environment variables and no admin exists yet.
        if (StringUtils.hasText(adminEmail) && StringUtils.hasText(adminPassword)) {
            boolean adminExists = userRepository.existsByRole(Role.ADMIN);
            if (!adminExists) {
                if (!userRepository.existsByEmail(adminEmail)) {
                    User admin = new User(
                            adminEmail,
                            passwordEncoder.encode(adminPassword),
                            adminName,
                            Role.ADMIN
                    );
                    userRepository.save(admin);
                    logger.info("Initial bootstrap admin account created successfully for: {}", adminEmail);
                } else {
                    logger.info("Admin email {} already registered as another account.", adminEmail);
                }
            } else {
                logger.info("Admin account already exists in database. Skipping admin bootstrap.");
            }
        } else {
            logger.info("No ADMIN_EMAIL / ADMIN_PASSWORD configured in environment. Skipping bootstrap admin creation.");
        }
    }
}
