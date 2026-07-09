package com.hexaware.main.careassist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hexaware.main.careassist.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Integer> {

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    List<AppUser> findByRole(String role);

    List<AppUser> findByRoleNot(String role);
}
