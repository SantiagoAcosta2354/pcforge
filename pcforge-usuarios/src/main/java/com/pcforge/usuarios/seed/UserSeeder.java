package com.pcforge.usuarios.seed;

import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.pcforge.usuarios.domain.Role;
import com.pcforge.usuarios.domain.User;
import com.pcforge.usuarios.repository.UserRepository;

/**
 * Crea las cuentas de demostracion, una por rol, para poder probar los
 * permisos de extremo a extremo. No se ejecuta si ya hay cuentas registradas.
 */
@Configuration
@ConditionalOnProperty(name = "pcforge.seed.enabled", havingValue = "true")
public class UserSeeder {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserSeeder.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${pcforge.seed.admin-email}")
    private String adminEmail;
    @Value("${pcforge.seed.admin-password}")
    private String adminPassword;
    @Value("${pcforge.seed.demo-password}")
    private String demoPassword;

    // despues de crear el indice unico del correo
    @Bean
    @Order(2)
    ApplicationRunner seedUsers() {
        return args -> {
            if (userRepository.count() > 0) {
                log.info("Ya existen cuentas registradas; no se cargan los datos de demostracion");
                return;
            }
            create("Administrador PCForge", adminEmail, adminPassword, Set.of(Role.ADMIN));
            create("Asesor comercial", "asesor@pcforge.co", demoPassword, Set.of(Role.ASESOR));
            create("Tecnico de ensamblaje", "tecnico@pcforge.co", demoPassword, Set.of(Role.TECNICO));
            create("Cliente de prueba", "cliente@pcforge.co", demoPassword, Set.of(Role.CLIENTE));
            log.info("Cuentas de demostracion creadas: admin, asesor, tecnico y cliente");
        };
    }

    private void create(String fullName, String email, String rawPassword, Set<Role> roles) {
        userRepository.save(User.builder().fullName(fullName).email(email.toLowerCase()).passwordHash(passwordEncoder.encode(rawPassword)).roles(roles).active(true).build());
    }

    public UserSeeder(final UserRepository userRepository, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
}
