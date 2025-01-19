package com.example.kemananan1.service;

import com.example.kemananan1.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //find user in database
        UserDetails user = this.userRepo.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

//        // Ambil peran pengguna dan konversikan menjadi List<GrantedAuthority>
//        List<SimpleGrantedAuthority> authorities = user.getAkses().stream()
//                .map(role -> new SimpleGrantedAuthority(role)) // Misalnya, makan, mandi
//                .collect(Collectors.toList());
        System.out.println("----------------------------------------------------------------xxxx ");
        System.out.println(user.getUsername());
        System.out.println(user.getPassword());
        System.out.println(user.getAuthorities());
        System.out.println("----------------------------------------------------------------xxxx ");
        // Kembalikan objek UserDetails yang digunakan oleh Spring Security
//        return new User(user.getUsername(), user.getPassword(), authorities);

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), user.getAuthorities());

    }
}
