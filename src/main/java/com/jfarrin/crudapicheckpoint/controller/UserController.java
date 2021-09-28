package com.jfarrin.crudapicheckpoint.controller;

import com.jfarrin.crudapicheckpoint.model.AuthenticationDetails;
import com.jfarrin.crudapicheckpoint.model.User;
import com.jfarrin.crudapicheckpoint.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository repository;

    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<Iterable<User>> getUsers(){
        return new ResponseEntity<>(this.repository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        return new ResponseEntity<>(this.repository.findById(id).orElse(null), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user){
        return new ResponseEntity<>(this.repository.save(user), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id, @RequestBody User newUser){
        Optional<User> user = this.repository.findById(id);
        if (user.isPresent()){
            user.get().setEmail(newUser.getEmail());
            user.get().setPassword(newUser.getPassword());
            return new ResponseEntity<>(this.repository.save(user.get()), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Integer>> deleteUserById(@PathVariable Long id){
        this.repository.deleteById(id);
        return new ResponseEntity<>(new HashMap<String,Integer>(){{put("count",((Collection)repository.findAll()).size());}}, HttpStatus.OK);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<HashMap<String,Object>> postAuthenticateUser(@RequestBody AuthenticationDetails authenticationDetails){
        User dbUser = this.repository.findUserByEmail(authenticationDetails.getEmail());
        return dbUser != null && authenticationDetails.getPassword().equals(dbUser.getPassword())
                ? new ResponseEntity<>(new HashMap<>(){{put("authenticated",true); put("user",dbUser);}}, HttpStatus.OK)
                : new ResponseEntity<>(new HashMap<>(){{put("authenticated",false);}}, HttpStatus.OK);
    }
}
