package com.medicore.api.util;

import com.medicore.api.model.User;
import com.medicore.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PasswordMigrationTask implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("[PasswordMigration] Checking for plain-text passwords...");
        List<User> users = userRepository.findAll();
        int count = 0;

        for (User user : users) {
            String password = user.getPassword();
            // BCrypt hashes usually start with $2a$, $2b$, or $2y$
            if (password != null && !password.startsWith("$2a$") && !password.startsWith("$2b$") && !password.startsWith("$2y$")) {
                System.out.println("[PasswordMigration] Hashing password for user: " + user.getUsername());
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
                count++;
            }
        }

        if (count > 0) {
            System.out.println("[PasswordMigration] Successfully migrated " + count + " passwords to BCrypt.");
        } else {
            System.out.println("[PasswordMigration] All passwords are already hashed.");
        }
    }
}
