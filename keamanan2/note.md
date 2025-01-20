HTTP Request --> 
SecurityFilterChain --> 
AuthenticationManager --> 
AuthenticationProvider --> 
Success/Failure



```
http://localhost:8080/auth/login POST

curl --location 'http://localhost:8080/auth/register' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'username=user' \
--data-urlencode 'password=password' \
--data-urlencode 'role=USER'

```

```
http://localhost:8080/auth/login POST

curl --location 'http://localhost:8080/auth/login' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'username=user' \
--data-urlencode 'password=password'
```