package com.noentiendo.pokecenter.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.noentiendo.pokecenter.entity.User;
import com.noentiendo.pokecenter.repository.UserRepository;

/**
 * Test configuration que crea datos de prueba
 * Crea un usuario default: claudio / 1234
 */
@TestConfiguration
public class TestDataConfig {

    @Bean
    public TestDataInitializer testDataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return new TestDataInitializer(userRepository, passwordEncoder);
    }

    public static class TestDataInitializer {
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        public TestDataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
            initializeTestData();
        }

        public void initializeTestData() {
            // Crear usuario default si no existe
            if (userRepository.findByUsername("claudio").isEmpty()) {
                User defaultUser = User.builder()
                        .username("claudio")
                        .passwordHash(passwordEncoder.encode("1234"))
                        .build();
                userRepository.save(defaultUser);
            }
        }
    }
}
