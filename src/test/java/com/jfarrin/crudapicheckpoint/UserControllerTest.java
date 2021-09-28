package com.jfarrin.crudapicheckpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfarrin.crudapicheckpoint.model.AuthenticationDetails;
import com.jfarrin.crudapicheckpoint.model.User;
import com.jfarrin.crudapicheckpoint.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.HashMap;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository repository;

    @Transactional
    @Rollback
    @Test
    void getUsers() throws Exception {
        User user1 = new User("john@example.com", "");
        User user2 = new User("eliza@example.com", "");
        User[] users = {this.repository.save(user1), this.repository.save(user2)};
        String expected = mapper.writeValueAsString(users);

        this.mvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expected));
    }

    @Transactional
    @Rollback
    @Test
    void postNewUser() throws Exception {
        User user = new User("john@example.com", "abc123");

        this.mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", equalTo("john@example.com")));
    }

    @Transactional
    @Rollback
    @Test
    void getUserById() throws Exception {
        User user = this.repository.save(new User("john@example.com", "abc123"));

        this.mvc.perform(get("/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(user)));
    }

    @Transactional
    @Rollback
    @Test
    void patchUserEmail() throws Exception {
        String patchJson = "{\"email\": \"joe@example.com\"}";
        User user = this.repository.save(new User("john@example.com", "abc123"));
        user.setEmail("joe@example.com");
        this.mvc.perform(patch("/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(user)));
    }

    @Transactional
    @Rollback
    @Test
    void patchUserEmailAndPassword() throws Exception {
        String patchJson = "{\"email\": \"joe@example.com\",\"password\": \"1234\"}";
        User user = this.repository.save(new User("john@example.com", "abc123"));
        user.setEmail("joe@example.com");
        user.setPassword("1234");
        this.mvc.perform(patch("/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(user)));
    }

    @Transactional
    @Rollback
    @Test
    void deleteUser() throws Exception {
        User user = this.repository.save(new User("john@example.com", "abc123"));
        assertEquals(1,((Collection)this.repository.findAll()).size());

        this.mvc.perform(delete("/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{count: 0}"))
                .andExpect(jsonPath("$.count",equalTo(0)));
    }

    @Transactional
    @Rollback
    @Test
    void postValidAuthentication() throws Exception {

        String email = "angelica@example.com";
        String password = "1234";

        User user = this.repository.save(new User(email,password));
        HashMap<String, Object> expected = new HashMap<>(){{
            put("authenticated", true);
            put("user", user);
        }};

        AuthenticationDetails authenticationDetails = new AuthenticationDetails(email,password);

        this.mvc.perform(post("/users/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(authenticationDetails)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected)));
    }

    @Transactional
    @Rollback
    @Test
    void postInvalidAuthentication() throws Exception {

        String email = "angelica@example.com";
        String password = "1234";

        User user = this.repository.save(new User(email,password));
        HashMap<String, Object> expected = new HashMap<>(){{
            put("authenticated", false);
        }};

        AuthenticationDetails authenticationDetails = new AuthenticationDetails(email,"12345");

        this.mvc.perform(post("/users/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(authenticationDetails)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected)));
    }
}
