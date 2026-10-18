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
 * permisos de extremo a extremo.
 *
 * La comprobacion es por correo y no por "la base esta vacia": asi, cuando se
 * agrega un rol nuevo al sistema, la cuenta correspondiente aparece en el
 * siguiente arranque aunque las demas ya existan. Antes el cargador se
 * detenia si habia cualquier cuenta y los roles agregados despues se quedaban
 * sin usuario con el que probarlos.
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
            int created = 0;
            created += createIfMissing("Administrador PCForge", adminEmail, adminPassword, Set.of(Role.ADMIN));
            created += createIfMissing("Asesor comercial", "asesor@pcforge.co", demoPassword, Set.of(Role.ASESOR));
            created += createIfMissing("Tecnico de ensamblaje", "tecnico@pcforge.co", demoPassword, Set.of(Role.TECNICO));
            created += createIfMissing("Cliente de prueba", "cliente@pcforge.co", demoPassword, Set.of(Role.CLIENTE));
            created += createIfMissing("Curador de datos tecnicos", "curador@pcforge.co", demoPassword, Set.of(Role.CURADOR));
            created += createIfMissing("Analista de precios y margenes", "analista@pcforge.co", demoPassword, Set.of(Role.ANALISTA));
            if (created == 0) {
                log.info("Las cuentas de demostracion ya existen; no se creo ninguna");
            } else {
                log.info("Cuentas de demostracion creadas en este arranque: {}", created);
            }
        };
    }

    /**
     * @return 1 si la cuenta se creo, 0 si ya existia.
     */
    private int createIfMissing(String fullName, String email, String rawPassword, Set<Role> roles) {
        String normalized = email.toLowerCase();
        if (userRepository.existsByEmail(normalized)) {
            return 0;
        }
        userRepository.save(User.builder().fullName(fullName).email(normalized)
                .passwordHash(passwordEncoder.encode(rawPassword)).roles(roles).active(true).build());
        return 1;
    }

    public UserSeeder(final UserRepository userRepository, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
}
