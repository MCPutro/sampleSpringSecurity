package com.example.kemananan1;

import com.example.kemananan1.util.JwtUtil;
import io.jsonwebtoken.lang.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JwtUtilTest {

    @Test
    void generate() throws InterruptedException {
        JwtUtil jwtUtil = new JwtUtil();
        String adadasd = jwtUtil.generateToken("adadasd", Arrays.asList(new String[]{"makan", "minum"}));
        System.out.println(adadasd);

//        Thread.sleep(Duration.ofSeconds(3));

        boolean b = jwtUtil.validateToken(adadasd);
        System.out.printf("b=%b", b);
    }
}