# 카카오페이 - 뿌리기 기능 구현
 
## 개발환경
 - kotlin 1.3.70
 - Spring Boot 2.2.4.RELEASE
 - JPA
 - QueryDSL
 - Redis
 - H2
 - Gradle
 
## 실행환경
localhost:6379 로 Redis 접속이 가능해야 한다.
멀티 인스턴스 환경으로 실행 시 DB 와 Redis 의 [접속 정보](./src/main/resources/application.yml)를 변경해야 한다.

## API

### 뿌리기 API
Request
```
POST /sprinkles
X-USER-ID: ${userId}
X-ROOM-ID: ${roomId}

{
    "amount": 100,
    "divide": 3
}
```

Success
```
HTTP 201 CREATED
{
    "token": ${token}
}
```

Fail
```
HTTP 500 Internal Server Error
{
    "code": "SERVER_ERROR",
    "message": "시스템 오류가 발생하였습니다.",
    "timestamp": "2020-06-27T06:03:06.556993"
}
```

### 받기 API
Request
```
PUT /sprinkles
X-USER-ID: ${userId}
X-ROOM-ID: ${roomId}

{
    "token": ${token}
}
```

Success
```
HTTP 200 OK
{
    "amount": 18
}
```

Fail
```
HTTP 400 Bad Request
{
    "code": "SAME_SPRINKLE_USER",
    "message": "자신이 뿌리기 한 건은 자신이 받을 수 없습니다.",
    "timestamp": "2020-06-27T05:58:52.671740"
}
```
```
HTTP 400 Bad Request
{
    "code": "NOT_SAME_ROOM",
    "message": "대화방이 동일하지 않습니다.",
    "timestamp": "2020-06-27T06:00:42.506162"
}
```
```
HTTP 400 Bad Request
{
    "code": "SPRINKLE_REQUEST_TIMEOUT",
    "message": "유효기간이 지난 요청입니다.",
    "timestamp": "2020-06-27T06:00:42.506162"
}
```
```
HTTP 400 Bad Request
{
    "code": "ALREADY_RECEIVED",
    "message": "이미 받은 이력이 있습니다.",
    "timestamp": "2020-06-27T06:02:25.213066"
}
```
```
HTTP 400 Bad Request
{
    "code": "ALREADY_FINISHED",
    "message": "이미 종료되었습니다.",
    "timestamp": "2020-06-27T06:03:06.556993"
}
```
```
HTTP 400 Bad Request
{
    "code": "BAD_REQUEST",
    "message": "잘못된 요청입니다.",
    "timestamp": "2020-06-27T06:03:06.556993"
}
```
```
HTTP 500 Internal Server Error
{
    "code": "SERVER_ERROR",
    "message": "시스템 오류가 발생하였습니다.",
    "timestamp": "2020-06-27T06:03:06.556993"
}
```

### 조회 API
Request
```
GET /sprinkles/${tokenId}
X-USER-ID: ${userId}
```

Success
```
HTTP 200 OK
{
    "sprinkledAt": "2020-06-27T05:57:15.842702",
    "amount": 100,
    "totalReceivedAmount": 100,
    "receivedList": [
        {
            "receivedAmount": 18,
            "receivedUserId": 1004
        },
        {
            "receivedAmount": 1,
            "receivedUserId": 1006
        },
        {
            "receivedAmount": 81,
            "receivedUserId": 1007
        }
    ]
}
```

Fail
```
HTTP 404 Not Found
{
    "code": "NOT_FOUND",
    "message": "요청하신 내용을 찾을 수 없습니다.",
    "timestamp": "2020-06-27T06:07:39.290946"
}
```
```
HTTP 500 Internal Server Error
{
    "code": "SERVER_ERROR",
    "message": "시스템 오류가 발생하였습니다.",
    "timestamp": "2020-06-27T06:03:06.556993"
}
```

## 문제해결전략

### 3자리 고유 토큰 생성
`System.nanoTime()` 을 64 진수로 변환하여 사용하였다.
3자리를 보장하도록 64^2 ~ 64^3 사이의 값으로 조정하여 사용하였고, 진수 변환을 위한 digits 은 예측 불가능하도록 매번 섞어서 사용하였다.
별도의 중복 체크는 멀티 인스턴스 환경에서 오버엔지니어링이 될 수 있다고 판단, 성능을 고려하여 별도의 중복 체크는 진행하지 않았다.

### 다수의 서버, 다수의 인스턴스
멀티 인스턴스 환경에서 문제가 발생할 수 있는 부분은 `받기 API` 이다.
아직 할당되지 않은 분배건을 요청자에게 할당하는 작업에 대해 동시실행 방지가 필요하다.
이를 위해 redis 의 [distributed locks](https://redis.io/topics/distlock) 을 사용하였고, [Spring Integration](https://docs.spring.io/spring-integration/reference/html/redis.html#redis-lock-registry) 의 구현을 이용하였다.