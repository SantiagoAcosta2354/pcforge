package com.pcforge.usuarios.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pcforge.usuarios.domain.Role;
import com.pcforge.usuarios.domain.User;

public interface UserRepository extends MongoRepository<User, String> {

    /** El correo se normaliza a minusculas antes de consultar. */
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByActiveTrueOrderByFullNameAsc();

    List<User> findByRolesContaining(Role role);
}
