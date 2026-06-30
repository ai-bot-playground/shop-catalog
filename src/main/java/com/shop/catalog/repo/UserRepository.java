package com.shop.catalog.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shop.catalog.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsername(String username);
}
