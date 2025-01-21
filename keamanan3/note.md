Proses Autentikasi:
```
HTTP Request --> SecurityFilterChain --> AuthenticationManager --> AuthenticationProvider --> Success/Failure
```

Delegasi Autentikasi:
```
AuthenticationManager --> [AuthenticationProvider1, AuthenticationProvider2, ...]

```
Req Register & Login
```
curl --location 'http://localhost:8080/auth/register' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'username=newuser' \
--data-urlencode 'password=newpassword' \
--data-urlencode 'role=USER'

or 


curl --location 'http://localhost:8080/auth/login' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--header 'Cookie: flash-session=MTczNjc0MjUzNHxEWDhFQVFMX2dBQUJFQUVRQUFBRV80QUFBQT09fMs28Y-mtaogVc9gYgHpGMFKTbU1gwAILzu6CmAjJ3lr; user-session=MTczNjc0MjU4OHxEWDhFQVFMX2dBQUJFQUVRQUFCQV80QUFBUVp6ZEhKcGJtY01CQUFDYVdRR2MzUnlhVzVuRENZQUpHSmtNekEyTnpnNUxUa3lOMkV0TkdFeFl5MWlOVEl3TFdZeU1HUmtaR0l4WVRBMk5BPT18zASNf2RRi42NyJuFPuwSiKqUn_K65X6i2-WGTVkHW-Q=' \
--data-urlencode 'username=newuser' \
--data-urlencode 'password=newpassword'

```

