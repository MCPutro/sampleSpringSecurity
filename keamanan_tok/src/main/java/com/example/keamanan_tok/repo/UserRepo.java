package com.example.keamanan_tok.repo;



import com.example.keamanan_tok.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class UserRepo {
    private List<User> users = new ArrayList<>();

    public List<User> getUsers() {
        return users;
    }

    public Optional<User> findUserByUsername(String username) {

        if (!username.equals("emchepe")) {
            return Optional.empty();
        }

        this.users.clear();
        this.users.add(User.builder()
                .id(1)
                .email("email@email.com")
                .name("name")
                .password("password")
                .username(username)
                .akses(Arrays.asList("mains", "mandi", "tidur"))
                .build());
        return Optional.ofNullable(users.getFirst());
    }

}
