package com.hexaware.main.careassist.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hexaware.main.careassist.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Integer> {
    Optional<Admin> findTopByAppUserUserIdOrderByAdminIdDesc(Integer userId);
}
