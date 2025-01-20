package com.example.keamanan_tok.controller;


import com.example.keamanan_tok.model.User;
import com.example.keamanan_tok.repo.UserRepo;
import com.example.keamanan_tok.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api")
public class HelloController {

    private List<User> userList = new ArrayList<>();

    @Autowired
    private JwtUtils jwtUtil;

    @Autowired
    private UserRepo userRepo;

    @GetMapping("/public/hello")
    public String sayPublicHello() {
        return "Hello, Public World!";
    }

    @GetMapping("/public/login")
    public ResponseEntity<Object> login() {
        Optional<User> aaaa = this.userRepo.findUserByUsername("emchepe");
        if (aaaa.isPresent()) {

            String token = this.jwtUtil.generateToken(aaaa.get().getUsername(), aaaa.get().getAkses());

            Map<String, String> map = new HashMap<>();
            map.put("username", aaaa.get().getUsername());
            map.put("password", aaaa.get().getPassword());
            map.put("token", token);
            return new ResponseEntity<>(map, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("salah cuy", HttpStatus.UNAUTHORIZED);
        }

    }

    @GetMapping("/private/hello")
    public String sayPrivateHello() {
        return "Hello, Private World!";
    }

    @PreAuthorize("hasAuthority('main')")
    @GetMapping("/private/users1")
    public ResponseEntity<Object> getUserList1() {
        initList();
        List<User> tempList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            tempList.add(this.userList.get(i));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("list", tempList);
        map.put("size", tempList.size());
        return ResponseEntity.accepted().body(map);
    }

    @PreAuthorize("hasAuthority('mandi')")
    @GetMapping("/private/users2")
    public ResponseEntity<Object> getUserList2() {
        initList();
        List<User> tempList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            tempList.add(this.userList.get(i));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("list", tempList);
        map.put("size", tempList.size());
        return ResponseEntity.accepted().body(map);
    }

    @PreAuthorize("hasAuthority('tidur')")
    @GetMapping("/private/users3")
    public ResponseEntity<Object> getUserList3() {
        initList();

        Map<String, Object> map = new HashMap<>();
        map.put("list", this.userList);
        map.put("size", userList.size());
        return ResponseEntity.accepted().body(map);
    }

    private void initList() {
        if (this.userList.isEmpty()) {
            for (int i = 0; i < 10; i++) {
                this.userList.add(User.builder()
                        .id(i + 1)
                        .email("email" + (i + 1))
                        .username("username" + (i + 1))
                        .name("name" + (i + 1))
                        .akses(Arrays.asList("akses" + (i + 1)))
                        .build());
            }
        }
    }


    // Menangani AccessDeniedException ketika pengguna tidak memiliki izin
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        // Menambahkan pesan error ke model dan mengarahkannya ke halaman error
//        model.addAttribute("error", "You do not have permission to access this page.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("gak ada akses cuy");
    }
}
