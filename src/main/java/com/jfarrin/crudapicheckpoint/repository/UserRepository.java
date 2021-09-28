package com.jfarrin.crudapicheckpoint.repository;

import com.jfarrin.crudapicheckpoint.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    User findUserByEmail(String email);
}
