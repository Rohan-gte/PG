package com.example.demo.repository;

import com.example.demo.entity.User;
import com.example.demo.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    Page<User> findByRole(Role role, Pageable pageable);
    List<User> findByRole(Role role);
    long countByRole(Role role);

    @Query("select u from User u where u.role = :role and (lower(u.fullName) like lower(concat('%', :q, '%')) or lower(u.email) like lower(concat('%', :q, '%')))")
    Page<User> searchByRole(Role role, String q, Pageable pageable);
}
