- [ ] # Di Coba NOTE

## Q :  dirimu adalah java developer yang sedang membangun sebuah sistem. dirimu akan menambahakan kemananan menggunakan jwt dan akan menggunakan @PreAuthorize hasAuthority yang akan di ambil dari database. tolong buatkan code nya
## A :
Untuk menambahkan keamanan menggunakan JWT (JSON Web Token) dan anotasi `@PreAuthorize` yang otoritasnya diambil dari database di aplikasi Java, Anda bisa mengikuti langkah-langkah berikut:

### 1. **Setup Dependencies**

Pastikan Anda memiliki dependensi yang diperlukan dalam file `pom.xml` jika menggunakan Maven:

```xml
<dependencies>
    <!-- Spring Security for JWT Authentication -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- JWT library -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt</artifactId>
        <version>0.11.5</version>
    </dependency>

    <!-- Spring Data JPA for database access -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Spring Web for REST API -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring AOP for @PreAuthorize annotation support -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>
</dependencies>
```

### 2. **JWT Utility Class**

Buat kelas utilitas untuk menangani pembuatan dan validasi JWT:

```java
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtil {
    private String secretKey = "secret"; // Gantilah dengan key yang lebih aman

    // Generate JWT token
    public String generateToken(String username, String[] authorities) {
        return Jwts.builder()
                .setSubject(username)
                .claim("authorities", String.join(",", authorities))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour expiration
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // Extract username from JWT token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract authorities (roles) from JWT token
    public String[] extractAuthorities(String token) {
        String authorities = extractClaim(token, claims -> claims.get("authorities", String.class));
        return authorities.split(",");
    }

    // Generic method to extract claims
    public <T> T extractClaim(String token, io.jsonwebtoken.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    // Validate JWT token
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Validate token
    public boolean validateToken(String token, String username) {
        return (username.equals(extractUsername(token)) && !isTokenExpired(token));
    }
}
```

### 3. **Custom JWT Filter for Authentication**

Buat filter untuk memvalidasi JWT dalam setiap request HTTP.

```java
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, javax.servlet.http.HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = getJwtFromRequest(request);
        if (token != null && jwtUtil.validateToken(token, jwtUtil.extractUsername(token))) {
            String username = jwtUtil.extractUsername(token);
            String[] authorities = jwtUtil.extractAuthorities(token);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, AuthorityUtils.createAuthorityList(authorities));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

### 4. **Service untuk Mengambil Otoritas dari Database**

Misalnya, Anda memiliki entitas `User` dengan relasi `Role`, dan Anda ingin mengambil otoritas (role) pengguna dari database.

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
    }
}
```

### 5. **Konfigurasi Security**

Konfigurasikan Spring Security agar menggunakan JWT dan otoritas yang diambil dari database.

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtUtil jwtUtil) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/login", "/register").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilter(new JwtAuthenticationFilter(jwtUtil, customUserDetailsService));

        http.authorizeRequests()
            .antMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
            .antMatchers("/user/**").hasAuthority("ROLE_USER");
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
```

### 6. **Menggunakan `@PreAuthorize` di Controller**

Anda bisa menggunakan anotasi `@PreAuthorize` untuk menentukan otorisasi berdasarkan peran yang diambil dari database.

```java
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin/dashboard")
    public String getAdminDashboard() {
        return "Admin Dashboard";
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/user/profile")
    public String getUserProfile() {
        return "User Profile";
    }
}
```

### Penjelasan:

- **JwtUtil**: Untuk menangani pembuatan dan validasi token JWT.
- **JwtAuthenticationFilter**: Untuk memfilter dan memvalidasi token JWT pada setiap request.
- **CustomUserDetailsService**: Mengambil data pengguna dan otoritas mereka dari database.
- **SecurityConfig**: Menambahkan filter JWT dan konfigurasi keamanan.
- **Controller**: Menggunakan anotasi `@PreAuthorize` untuk memverifikasi akses berdasarkan otorisasi (role).

Sekarang, sistem Anda dapat mengamankan endpoint menggunakan JWT dan otoritas yang dikelola dari database.

## Q : apa berdaan ke 2 nya ?
pada class JwtAuthenticationFilter terdapat code untuk authorities
```java
String[] authorities = jwtUtil.extractAuthorities(token);
UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, AuthorityUtils.createAuthorityList(authorities));
SecurityContextHolder.getContext().setAuthentication(authentication);
```

pada CustomUserDetailsService juga terdapat code untuk authorities
```java
List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
.map(role -> new SimpleGrantedAuthority(role.getName()))
.collect(Collectors.toList());
return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
```



## A:
Baik, mari kita ulas secara mendalam perbedaan antara dua potongan kode yang menangani **authorities** (otoritas/peran pengguna) di kelas `JwtAuthenticationFilter` dan `CustomUserDetailsService`. Meskipun keduanya menangani otoritas pengguna, cara mereka mengelola dan sumber data otoritas tersebut sangat berbeda.

### 1. **Code di `JwtAuthenticationFilter`**

```java
String[] authorities = jwtUtil.extractAuthorities(token);
UsernamePasswordAuthenticationToken authentication =
    new UsernamePasswordAuthenticationToken(username, null, AuthorityUtils.createAuthorityList(authorities));
SecurityContextHolder.getContext().setAuthentication(authentication);
```

**Penjelasan**:
- **Sumber Otoritas**: Otoritas (`authorities`) diambil langsung dari token JWT. Informasi otoritas ini **sudah ada di dalam token JWT** yang dikirim oleh client (misalnya, aplikasi frontend atau aplikasi lain yang mengirimkan request). Token JWT ini berisi klaim yang menyimpan data seperti username dan otoritas.

- **Proses**:
    1. `jwtUtil.extractAuthorities(token)` digunakan untuk mengekstrak klaim yang berisi informasi otoritas dari token JWT. Biasanya, klaim ini berisi peran atau otoritas yang diberikan kepada pengguna, yang disimpan dalam bentuk string (misalnya: "ROLE_USER", "ROLE_ADMIN").
    2. `AuthorityUtils.createAuthorityList(authorities)` digunakan untuk mengonversi array string otoritas menjadi daftar `GrantedAuthority` yang dipahami oleh Spring Security.
    3. `UsernamePasswordAuthenticationToken` kemudian dibuat dengan menggunakan nama pengguna (`username`), otoritas yang baru diekstrak, dan nilai `null` untuk password (karena kita tidak lagi memerlukan password di sini—token JWT yang sudah diverifikasi menggantikan autentikasi berbasis password).
    4. Token autentikasi ini disetel dalam **SecurityContext** melalui `SecurityContextHolder.getContext().setAuthentication(authentication)`, sehingga Spring Security mengetahui bahwa pengguna ini sudah terautentikasi.

### 2. **Code di `CustomUserDetailsService`**

```java
List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
    .map(role -> new SimpleGrantedAuthority(role.getName()))
    .collect(Collectors.toList());
return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
```

**Penjelasan**:
- **Sumber Otoritas**: Otoritas di sini diambil dari **database**. Saat Spring Security memverifikasi kredensial pengguna (biasanya ketika pengguna login dengan username dan password), ia memanggil `CustomUserDetailsService` untuk mengambil detail pengguna. Dalam hal ini, otoritas berasal dari **relasi antara pengguna dan peran (role)** yang ada di database.

- **Proses**:
    1. `user.getRoles()` mengakses peran-peran yang dimiliki oleh pengguna dari **database** (biasanya melalui relasi antara entitas `User` dan `Role` di database).
    2. `SimpleGrantedAuthority(role.getName())` digunakan untuk mengonversi nama peran (misalnya "ROLE_USER", "ROLE_ADMIN") menjadi objek `GrantedAuthority`, yang dipahami oleh Spring Security.
    3. `collect(Collectors.toList())` mengumpulkan semua otoritas tersebut menjadi daftar otoritas.
    4. Akhirnya, `new org.springframework.security.core.userdetails.User(...)` membuat objek `UserDetails` yang menyertakan username, password, dan otoritas (roles) pengguna. Objek ini digunakan oleh Spring Security untuk proses autentikasi dan otorisasi.

### Perbedaan Utama:

1. **Sumber Otoritas**:
    - **Di `JwtAuthenticationFilter`**: Otoritas diambil dari **token JWT** yang sudah ada. Artinya, pengguna sudah memiliki token yang mengandung informasi otoritas, dan otoritas tersebut tidak perlu diambil lagi dari database.
    - **Di `CustomUserDetailsService`**: Otoritas diambil dari **database**. Saat pengguna mencoba login, aplikasi mengakses database untuk mendapatkan informasi peran yang dimiliki pengguna.

2. **Waktu Pengambilan Otoritas**:
    - **Di `JwtAuthenticationFilter`**: Otoritas diekstrak **setelah** pengguna berhasil mengautentikasi menggunakan JWT. Artinya, filter ini digunakan pada saat request yang sudah terautentikasi (menggunakan token JWT) diterima, dan otoritasnya sudah ada di dalam token.
    - **Di `CustomUserDetailsService`**: Otoritas diambil **selama proses login**. Ketika pengguna mencoba untuk login, otoritas diambil dari database dan digunakan untuk autentikasi pengguna.

3. **Cara Otoritas Ditangani**:
    - **Di `JwtAuthenticationFilter`**: Otoritas yang ada di dalam JWT digunakan untuk membuat objek `UsernamePasswordAuthenticationToken` dan disetel ke dalam **SecurityContext**. Ini memberi tahu Spring Security bahwa pengguna sudah terautentikasi dan sudah memiliki otoritas yang sah.
    - **Di `CustomUserDetailsService`**: Otoritas yang diambil dari database digunakan untuk membuat objek `UserDetails` yang digunakan oleh Spring Security selama proses login atau autentikasi berbasis username dan password.

4. **Ketergantungan pada Database**:
    - **Di `JwtAuthenticationFilter`**: Tidak ada ketergantungan pada database. Otoritas sudah disertakan dalam token JWT dan langsung digunakan untuk autentikasi.
    - **Di `CustomUserDetailsService`**: Ada ketergantungan pada database. Otoritas diambil dari tabel `Role` yang terkait dengan pengguna di database.

### Kesimpulan:
- **`JwtAuthenticationFilter`** berfungsi untuk mengekstrak otoritas pengguna dari **token JWT** yang sudah ada dan mengautentikasi pengguna berdasarkan token tersebut.
- **`CustomUserDetailsService`** berfungsi untuk memuat otoritas pengguna dari **database** saat pengguna login dengan username dan password.

Keduanya berfungsi untuk menangani autentikasi dan otorisasi pengguna, tetapi mereka melakukannya dalam konteks yang berbeda dan menggunakan sumber data yang berbeda: **JWT** di satu sisi dan **database** di sisi lain.


## update filter

Tentu! Berikut adalah kode **`JwtAuthenticationFilter`** yang telah diperbaiki dan dioptimalkan, dengan memanfaatkan **`UserDetailsService`** untuk memverifikasi pengguna berdasarkan username yang diekstrak dari token JWT, jika diperlukan:

### Kode `JwtAuthenticationFilter` yang Diperbaiki

```java
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@WebFilter("/api/*")  // Filter ini bekerja untuk endpoint tertentu, sesuaikan dengan kebutuhan aplikasi
public class JwtAuthenticationFilter implements Filter {

    private JwtUtil jwtUtil; // Utility untuk mengolah JWT
    private UserDetailsService userDetailsService; // UserDetailsService untuk memuat UserDetails
    private AuthenticationManager authenticationManager; // Untuk menangani autentikasi

    // Konstruktor untuk dependency injection
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService, AuthenticationManager authenticationManager) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void doFilter(javax.servlet.ServletRequest request, javax.servlet.ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = httpRequest.getHeader("Authorization"); // Ambil token dari header Authorization

        if (token != null && token.startsWith("Bearer ")) {
            // Ekstrak token tanpa "Bearer "
            token = token.substring(7);
            
            // Ekstrak username dari token
            String username = jwtUtil.extractUsername(token);
            
            // Cek apakah username sudah ada di dalam context keamanan
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Memuat UserDetails dari database menggunakan UserDetailsService
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Validasi token JWT (misalnya cek apakah token belum expired)
                if (jwtUtil.validateToken(token, username)) {
                    // Membuat objek Authentication dengan otoritas yang sesuai dari UserDetails
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, 
                            userDetails.getAuthorities()
                        );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
                    
                    // Set Authentication ke SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        
        // Lanjutkan ke filter selanjutnya
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Inisialisasi filter (bisa kosong jika tidak diperlukan)
    }

    @Override
    public void destroy() {
        // Bersihkan resource jika diperlukan
    }
}
```

### Penjelasan Pembaruan:

1. **Penggunaan `UserDetailsService`**:
    - Saya menambahkan penggunaan **`UserDetailsService`** di dalam filter untuk memuat detail pengguna berdasarkan username yang diekstrak dari JWT. Hal ini dapat berguna jika Anda ingin memverifikasi status pengguna atau melakukan operasi lain yang melibatkan database.
    - `userDetailsService.loadUserByUsername(username)` digunakan untuk mendapatkan detail pengguna dari database, seperti peran dan informasi terkait lainnya.

2. **Token JWT dan Validasi**:
    - Token JWT diparsing dari header `Authorization`. Jika token ditemukan dan valid, filter akan memastikan bahwa pengguna yang sesuai ada di dalam konteks keamanan Spring.
    - Saya menggunakan `jwtUtil.extractUsername(token)` untuk mengekstrak username dari token dan kemudian memverifikasi token menggunakan `jwtUtil.validateToken(token, username)`.

3. **Menyetel Autentikasi ke dalam `SecurityContext`**:
    - Setelah memverifikasi token dan memuat `UserDetails`, saya membuat objek **`UsernamePasswordAuthenticationToken`** dengan menggunakan informasi dari `UserDetails`, termasuk otoritas (`getAuthorities()`), dan menyetelnya ke dalam **SecurityContext** menggunakan `SecurityContextHolder.getContext().setAuthentication(authentication)`.
    - Ini memberi tahu Spring Security bahwa pengguna sudah terautentikasi dan memiliki otoritas yang sah.

### Komponen yang Dibutuhkan:
1. **`JwtUtil`**: Ini adalah utility yang Anda buat untuk menangani pembuatan dan verifikasi token JWT (seperti mengekstrak username, otoritas, dan validasi token).
2. **`UserDetailsService`**: Digunakan untuk memuat informasi pengguna dari database berdasarkan `username` yang diekstrak dari JWT.
3. **`AuthenticationManager`**: Biasanya digunakan untuk mengelola proses autentikasi di Spring Security, meskipun dalam kode ini, `authenticationManager` tidak digunakan langsung, tetapi jika perlu untuk kebutuhan lebih lanjut, Anda bisa menggunakannya.

### Apa yang Berubah:
- **`UserDetailsService`** sekarang digunakan untuk memuat detail pengguna dari database, yang mungkin diperlukan jika Anda perlu memverifikasi status pengguna atau memperbarui informasi terkait pengguna yang terdapat dalam JWT.
- Filter ini memastikan bahwa setiap request dengan token yang valid akan diterima dan diproses oleh Spring Security, dengan memastikan bahwa informasi autentikasi dan otoritas pengguna dimuat dan valid.

Jika Anda tidak memerlukan validasi database lebih lanjut atau tidak memerlukan informasi lebih lanjut tentang pengguna selain yang ada di dalam JWT, Anda bisa menghapus penggunaan `UserDetailsService` dan langsung mengonfigurasi otoritas berdasarkan klaim yang ada di dalam JWT.

Semoga penjelasan dan kode yang diperbarui ini membantu! Jika ada pertanyaan lebih lanjut atau jika ada yang perlu dijelaskan lebih dalam, silakan beri tahu saya!

## Q : pada class JwtAuthenticationFilter ada variable UserDetailsService tapi tidak digunakan.
## A : 
Jika di kelas `JwtAuthenticationFilter` ada variabel `UserDetailsService` namun tidak digunakan, itu mungkin disebabkan oleh kesalahan dalam implementasi atau desain kode. Secara umum, **`UserDetailsService`** digunakan dalam Spring Security untuk memuat informasi pengguna berdasarkan kredensial seperti username dan password dari database. Namun, dalam konteks `JwtAuthenticationFilter`, penggunaan `UserDetailsService` tidak selalu diperlukan, karena autentikasi sudah dilakukan dengan token JWT, bukan dengan kredensial berbasis username dan password.

Mari kita lihat dua kemungkinan alasan kenapa `UserDetailsService` mungkin ada, tetapi tidak digunakan dalam `JwtAuthenticationFilter`:

### 1. **Penggunaan `UserDetailsService` untuk Otorisasi (Jika Diperlukan)**
Biasanya, `UserDetailsService` digunakan untuk memuat detail pengguna (seperti roles/authorities) dari database berdasarkan **username**. Namun, dalam `JwtAuthenticationFilter`, proses autentikasi dilakukan berdasarkan **token JWT**, yang sudah berisi informasi tentang otoritas pengguna. Oleh karena itu, `UserDetailsService` mungkin **tidak diperlukan** dalam filter ini.

Namun, jika tujuannya adalah untuk **memverifikasi** atau **memuat lebih lanjut** data pengguna yang ada di dalam JWT (misalnya, melakukan query database untuk memastikan bahwa pengguna masih aktif), maka `UserDetailsService` bisa digunakan. Namun, ini bukan langkah yang umum karena token JWT biasanya sudah cukup untuk autentikasi.

### 2. **Jika Tujuan Anda Menggunakan `UserDetailsService` untuk Memverifikasi Pengguna**
Jika Anda memang ingin memverifikasi pengguna berdasarkan data dari database (misalnya untuk memastikan status pengguna atau apakah pengguna tersebut valid dalam aplikasi), Anda bisa menggunakan `UserDetailsService` di dalam filter. Tetapi, biasanya data yang ada dalam JWT sudah cukup untuk melakukan hal ini. Jika Anda merasa perlu melakukan query database, maka Anda bisa menggunakan `UserDetailsService` untuk mengambil **`UserDetails`** dari database berdasarkan **username**.

### Implementasi Kode yang Memanfaatkan `UserDetailsService`

Jika Anda memang ingin menggunakan `UserDetailsService` di dalam `JwtAuthenticationFilter`, berikut adalah contoh implementasi bagaimana Anda bisa menggunakan **`UserDetailsService`** untuk memverifikasi pengguna berdasarkan username yang ada dalam JWT:

### Contoh Implementasi Menggunakan `UserDetailsService` di `JwtAuthenticationFilter`

Misalnya, kita ingin memverifikasi pengguna berdasarkan username dari token JWT dan memuat detail pengguna menggunakan `UserDetailsService`. Anda bisa melakukannya seperti ini:

```java
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@WebFilter("/api/*")  // Filter ini bekerja untuk endpoint tertentu, sesuaikan dengan kebutuhan aplikasi
public class JwtAuthenticationFilter implements Filter {

    private JwtUtil jwtUtil; // Utility untuk mengolah JWT
    private UserDetailsService userDetailsService; // UserDetailsService untuk memuat UserDetails
    private AuthenticationManager authenticationManager; // Untuk menangani autentikasi

    // Konstruktor untuk dependency injection
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService, AuthenticationManager authenticationManager) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void doFilter(javax.servlet.ServletRequest request, javax.servlet.ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = httpRequest.getHeader("Authorization"); // Ambil token dari header Authorization

        if (token != null && token.startsWith("Bearer ")) {
            // Ekstrak token tanpa "Bearer "
            token = token.substring(7);
            
            // Ekstrak username dari token
            String username = jwtUtil.extractUsername(token);
            
            // Cek apakah username sudah ada di dalam context keamanan
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Memuat UserDetails dari database menggunakan UserDetailsService
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Validasi token JWT (misalnya cek apakah token belum expired)
                if (jwtUtil.validateToken(token, username)) {
                    // Membuat objek Authentication dengan otoritas yang sesuai dari UserDetails
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, 
                            userDetails.getAuthorities()
                        );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
                    
                    // Set Authentication ke SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        
        // Lanjutkan ke filter selanjutnya
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Inisialisasi filter (bisa kosong jika tidak diperlukan)
    }

    @Override
    public void destroy() {
        // Bersihkan resource jika diperlukan
    }
}
```

### Penjelasan:
- **`UserDetailsService` digunakan untuk memuat detail pengguna** dari database berdasarkan username yang ada di dalam token JWT.
- **`jwtUtil.validateToken(token, username)`**: Validasi token JWT untuk memastikan bahwa token yang diterima valid dan sesuai dengan username pengguna.
- **`UsernamePasswordAuthenticationToken`**: Setelah memuat detail pengguna, kita membuat token autentikasi dan menambahkannya ke `SecurityContext` agar Spring Security tahu bahwa pengguna tersebut sudah terautentikasi.
- **Penyaringan Otentikasi**: Filter ini akan dijalankan pada setiap request yang membutuhkan autentikasi berdasarkan token JWT.

### Kapan Anda Memerlukan `UserDetailsService` di `JwtAuthenticationFilter`?
1. **Untuk Memverifikasi Pengguna**: Jika Anda ingin memverifikasi apakah pengguna yang ada di dalam JWT benar-benar ada di database dan masih aktif, Anda dapat memanfaatkan `UserDetailsService` untuk memuat data pengguna dari database.

2. **Menggunakan Detail Pengguna**: Jika Anda perlu informasi lebih lanjut tentang pengguna selain yang ada di dalam JWT (misalnya, status pengguna atau informasi yang disimpan di database), Anda bisa menggunakan `UserDetailsService` untuk memuatnya.

### Kesimpulan:
- **Jika JWT sudah berisi semua informasi yang diperlukan** (termasuk otoritas), Anda **tidak memerlukan `UserDetailsService`** di dalam `JwtAuthenticationFilter`.
- **Namun, jika Anda ingin melakukan pengecekan lebih lanjut tentang pengguna di database**, seperti memverifikasi status pengguna atau memperbarui informasi pengguna, Anda bisa menggunakan `UserDetailsService` untuk mengambil detail pengguna dari database.
    
## Q : tolong perbaiki class JwtAuthenticationFilter dengan mengimplementasikan OncePerRequestFilter
## A : 
Tentu! **`OncePerRequestFilter`** adalah kelas abstrak di Spring Security yang digunakan untuk memastikan bahwa filter Anda hanya dijalankan **sekali** per request. Ini sangat berguna untuk situasi di mana Anda ingin memproses request hanya satu kali dalam siklus hidupnya, yang menghindari eksekusi berulang pada filter yang mungkin di-chain bersama filter lainnya.

Jika Anda ingin memperbaiki dan mengimplementasikan filter Anda menggunakan `OncePerRequestFilter`, maka kita akan mengganti `Filter` dengan `OncePerRequestFilter`. Berikut adalah contoh implementasi **`JwtAuthenticationFilter`** yang menggunakan `OncePerRequestFilter` untuk mengautentikasi pengguna berdasarkan token JWT.

### Kode `JwtAuthenticationFilter` dengan Implementasi `OncePerRequestFilter`

```java
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil; // Util untuk mengolah JWT

    @Autowired
    private UserDetailsService userDetailsService; // UserDetailsService untuk memuat UserDetails

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Ambil token JWT dari header "Authorization"
        String token = request.getHeader("Authorization");

        // Jika token ada dan diawali dengan "Bearer "
        if (token != null && token.startsWith("Bearer ")) {
            // Hilangkan "Bearer " dan ambil token
            token = token.substring(7);
            
            // Ambil username dari token JWT
            String username = jwtUtil.extractUsername(token);

            // Cek apakah pengguna sudah ada di dalam context (sudah terautentikasi)
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Memuat UserDetails dari database menggunakan UserDetailsService
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Validasi token JWT (misalnya cek apakah token masih berlaku)
                if (jwtUtil.validateToken(token, username)) {
                    // Membuat objek Authentication dengan otoritas yang sesuai dari UserDetails
                    List<SimpleGrantedAuthority> authorities = userDetails.getAuthorities().stream()
                            .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, 
                            authorities
                        );
                    // Menambahkan detail request ke objek Authentication
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set Authentication ke SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        // Lanjutkan ke filter berikutnya dalam chain
        filterChain.doFilter(request, response);
    }
}
```

### Penjelasan Perubahan:

1. **`OncePerRequestFilter`**:
    - **`OncePerRequestFilter`** adalah subclass dari **`javax.servlet.Filter`** yang memastikan bahwa filter ini hanya dipanggil sekali per request. Anda harus mengimplementasikan metode **`doFilterInternal`**, yang menggantikan metode **`doFilter`** pada `Filter` biasa.
    - Dengan mengganti implementasi filter dari `Filter` ke `OncePerRequestFilter`, kita bisa menghindari eksekusi berulang, yang sangat membantu dalam memastikan filter hanya dipanggil sekali per request.

2. **`doFilterInternal`**:
    - Metode ini menggantikan **`doFilter`** dari `Filter` biasa. Di dalamnya, Anda akan melakukan logika filter, seperti ekstraksi token, validasi, dan set autentikasi ke dalam `SecurityContext`.

3. **Pengecekan Token**:
    - Token JWT diekstraksi dari header `Authorization`. Kami memeriksa apakah token dimulai dengan `"Bearer "`, kemudian mengambil bagian token yang sesungguhnya setelah `"Bearer "` tersebut.

4. **Memverifikasi Token dan Membuat `Authentication`**:
    - Setelah mengekstrak username dari token, kami memverifikasi token dengan metode `jwtUtil.validateToken(token, username)`. Jika valid, kita memuat detail pengguna menggunakan **`UserDetailsService`** untuk mengambil otoritas dan data pengguna lainnya.
    - Kemudian, objek **`UsernamePasswordAuthenticationToken`** dibuat dengan **userDetails** dan **otoritas** yang dimiliki pengguna. Otoritas ini didapatkan dari `UserDetails`, yang biasanya sudah dikelola oleh **`UserDetailsService`**.

5. **Menyetujui Autentikasi ke dalam `SecurityContext`**:
    - Setelah objek **`UsernamePasswordAuthenticationToken`** selesai dibuat, autentikasi disetel ke dalam **`SecurityContextHolder`**, sehingga Spring Security tahu bahwa pengguna telah terautentikasi.

6. **`filterChain.doFilter`**:
    - Setelah proses autentikasi selesai, filter akan melanjutkan ke filter berikutnya di dalam **filter chain** dengan memanggil `filterChain.doFilter(request, response)`.

### 2. **Pentingnya `OncePerRequestFilter`**:

- **Eksekusi Filter Secara Terpusat**: Menggunakan `OncePerRequestFilter` memastikan bahwa filter ini hanya dijalankan sekali per request, meskipun ada beberapa filter dalam chain. Ini menghindari eksekusi berulang yang tidak diperlukan.
- **Filter Berbasis HTTP Request**: `OncePerRequestFilter` sangat cocok digunakan dalam konteks Spring Security ketika Anda perlu memproses permintaan HTTP secara spesifik, seperti memeriksa dan memvalidasi JWT dalam header request.

### 3. **Penggunaan Filter dalam Spring Security Configuration**

Untuk memastikan filter ini dijalankan dalam aplikasi Anda, Anda perlu menambahkan **`JwtAuthenticationFilter`** ke dalam konfigurasi Spring Security Anda.

Contoh konfigurasi Spring Security untuk menggunakan `JwtAuthenticationFilter`:

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)  // Menambahkan filter sebelum filter autentikasi berbasis username/password
            .authorizeRequests()
                .antMatchers("/api/public/**").permitAll()  // Endpoint publik tanpa otentikasi
                .anyRequest().authenticated();            // Endpoint lainnya memerlukan autentikasi
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
```

### Penjelasan Konfigurasi:

- **`addFilterBefore`**: `JwtAuthenticationFilter` ditambahkan ke filter chain sebelum **`UsernamePasswordAuthenticationFilter`**. Ini penting karena kita ingin memverifikasi token JWT sebelum Spring Security melakukan autentikasi berbasis username dan password.
- **`antMatchers("/api/public/**").permitAll()`**: Endpoint `/api/public/**` akan diakses tanpa autentikasi.
- **`anyRequest().authenticated()`**: Semua request lainnya membutuhkan autentikasi.

Dengan konfigurasi ini, **`JwtAuthenticationFilter`** akan memverifikasi token JWT untuk semua request yang memerlukannya, dan hanya endpoint yang secara eksplisit diizinkan yang bisa diakses tanpa autentikasi.

### Kesimpulan

- **`OncePerRequestFilter`** memastikan bahwa filter hanya dijalankan satu kali per request.
- Kami menggunakan **`JwtAuthenticationFilter`** untuk mengautentikasi pengguna berdasarkan token JWT yang ada di header `Authorization`.
- Filter ditambahkan ke dalam konfigurasi Spring Security menggunakan **`addFilterBefore`**, sebelum autentikasi berbasis username dan password dilakukan.

Semoga ini membantu! Jika ada pertanyaan lebih lanjut, silakan beri tahu saya!

## Q: apakah class UserDetailsService di implementasikan olah suatu class ?
## A: 
Ya, **`UserDetailsService`** adalah antarmuka (interface) yang harus diimplementasikan oleh suatu kelas untuk mengambil detail pengguna (seperti username, password, dan otoritas/roles) dari suatu sumber, misalnya dari database.

Dalam aplikasi berbasis Spring Security, **`UserDetailsService`** umumnya digunakan untuk memuat detail pengguna berdasarkan kredensial (misalnya username) yang diminta. Setelah data pengguna ditemukan, Spring Security dapat memverifikasi kredensial dan memberikan akses yang sesuai berdasarkan peran atau otoritas yang diberikan.

### 1. **Contoh Implementasi `UserDetailsService`**

Sebuah kelas yang mengimplementasikan **`UserDetailsService`** harus mengimplementasikan metode **`loadUserByUsername(String username)`**, yang mengembalikan objek **`UserDetails`** (biasanya objek yang diimplementasikan oleh **`org.springframework.security.core.userdetails.User`**).

Berikut adalah contoh implementasi `UserDetailsService` menggunakan Spring Security, di mana informasi pengguna diambil dari database:

### Contoh Kelas Implementasi `UserDetailsService`

Misalkan kita memiliki model `User` yang berhubungan dengan entitas pengguna di database, dan model `Role` yang berisi informasi peran-peran pengguna.

```java
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository; // Repository untuk mengambil data pengguna dari database

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Ambil pengguna berdasarkan username dari database
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        // Ambil peran pengguna dan konversikan menjadi List<GrantedAuthority>
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName())) // Misalnya, ROLE_USER, ROLE_ADMIN
                .collect(Collectors.toList());
        
        // Kembalikan objek UserDetails yang digunakan oleh Spring Security
        return new User(user.getUsername(), user.getPassword(), authorities);
    }
}
```

### Penjelasan:

1. **`UserRepository`**: Ini adalah repositori yang digunakan untuk mengakses data pengguna dari database (misalnya menggunakan JPA atau Spring Data).
    - `userRepository.findByUsername(username)` akan mencari pengguna berdasarkan nama pengguna yang diberikan.

2. **`SimpleGrantedAuthority`**: Digunakan untuk mengonversi nama peran (misalnya, `ROLE_USER`, `ROLE_ADMIN`) menjadi objek yang dipahami oleh Spring Security sebagai `GrantedAuthority`.

3. **`User` (dari `org.springframework.security.core.userdetails.User`)**: Ini adalah implementasi standar dari objek `UserDetails` yang digunakan oleh Spring Security. Objek ini menyimpan informasi tentang pengguna, termasuk nama pengguna, password, dan otoritas (roles).

4. **`UsernameNotFoundException`**: Jika pengguna tidak ditemukan berdasarkan username yang diberikan, Spring Security akan melemparkan exception ini.

### 2. **Penggunaan `UserDetailsService` dalam Konfigurasi Spring Security**

Setelah Anda mengimplementasikan **`UserDetailsService`**, Anda dapat menggunakannya dalam konfigurasi Spring Security untuk mengautentikasi pengguna berdasarkan username dan password. Berikut adalah contoh cara mengonfigurasi Spring Security untuk menggunakan `CustomUserDetailsService`:

### Contoh Konfigurasi Spring Security untuk Menggunakan `UserDetailsService`

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // Mengonfigurasi Spring Security untuk menggunakan CustomUserDetailsService dan enkripsi password (misalnya BCrypt)
        auth.userDetailsService(customUserDetailsService)
            .passwordEncoder(new BCryptPasswordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/api/public/**").permitAll()  // Endpoint publik tanpa otentikasi
                .anyRequest().authenticated()             // Semua endpoint lain harus terautentikasi
            .and()
            .formLogin();  // Menggunakan login berbasis form (default)
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
```

### Penjelasan:
- **`AuthenticationManagerBuilder`** digunakan untuk mengonfigurasi `UserDetailsService` dan **password encoder** (misalnya `BCryptPasswordEncoder`).
- **`http.authorizeRequests()`** menentukan akses ke endpoint tertentu. Dalam contoh di atas, semua endpoint `/api/public/**` dapat diakses tanpa autentikasi, sedangkan endpoint lainnya memerlukan autentikasi.
- **`authenticationManagerBean()`** digunakan untuk menyediakan **`AuthenticationManager`** yang dapat digunakan oleh aplikasi, misalnya dalam filter JWT atau otentikasi berbasis form.

### 3. **Kenapa `UserDetailsService` Diperlukan?**

- **Autentikasi Berbasis Username dan Password**: `UserDetailsService` mengelola proses pemuatan detail pengguna berdasarkan **username** yang diberikan selama proses login, misalnya melalui form login.

- **Pengelolaan Otoritas Pengguna**: `UserDetailsService` memastikan bahwa otoritas atau peran yang diberikan kepada pengguna dimuat dengan benar (misalnya, `ROLE_USER`, `ROLE_ADMIN`), yang digunakan untuk menentukan apakah pengguna memiliki hak akses yang cukup untuk mengakses resource tertentu dalam aplikasi.

- **Penting dalam Konteks Database**: Jika data pengguna disimpan di database, `UserDetailsService` bertanggung jawab untuk mengambil data pengguna dari database dan mengembalikannya dalam bentuk yang dapat dipahami oleh Spring Security.

### Kesimpulan

- **`UserDetailsService`** adalah antarmuka yang harus diimplementasikan oleh kelas khusus yang bertugas memuat detail pengguna (seperti username, password, otoritas) dari sumber data, umumnya **database**.
- Anda perlu mengonfigurasi Spring Security untuk menggunakan implementasi dari `UserDetailsService` agar proses autentikasi dan otorisasi dapat berjalan dengan baik.
- `UserDetailsService` digunakan dalam kasus **autentikasi berbasis username dan password**, di mana kredensial pengguna diperiksa dan kemudian otoritas yang dimilikinya (seperti peran) diberikan.