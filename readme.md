# login 
## 登陆api后提供token
http://localhost:8090/user/login


# rest1
## 访问rest1的api 必须提供 token 才能访问
http://localhost:8091/hello

---
首先，某 client 使用自己的账号密码发送 post 请求 login，由于这是首次接触，服务器会校验账号与密码是否合法，如果一致，则根据密钥生成一个 token 并返回，client 收到这个 token 并保存在本地。在这之后，需要访问一个受保护的路由或资源时，只要附加上 token（通常使用 Header 的 Authorization 属性）发送到服务器，服务器就会检查这个 token 是否有效，并做出响应。

![avatar](/img/jwt-origin.jpg)

--- 
### JWT 的组成

    // Header
    {
    "alg": "HS256",
    "typ": "JWT"
    }

    // Payload
    {
    // reserved claims
    "iss": "a.com",
    "exp": "1d",
    // public claims
    "http://a.com": true,
    // private claims
    "company": "A",
    "awesome": true
    }

    // $Signature
    HS256(Base64(Header) + "." + Base64(Payload), secretKey)

JWT

    JWT = Base64(Header) + "." + Base64(Payload) + "." + $Signature

    HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)

---

第一部分是经过 Base64 编码的 Header。Header 是一个 JSON 对象，对象里有一个值为 “JWT” 的 typ 属性，以及 alg 属性，值为 HS256，表明最终使用的加密算法是 HS256

---

第二部分是经过 Base64 编码的 Payload。Payload 被定义为实体的状态，就像 token 自身附加元数据一样，claim 包含我们想要传输的信息，以及用于服务器验证的信息，一般有 reserved/public/private 三类。

---

第三部分是 $Signature。它由 Header 指定的算法 HS256 加密产生。该算法有两个参数，第一个参数是经过 Base64 分别编码的 Header 及 Payload 通过 . 连接组成的字符串，第二个参数是生成的密钥，由服务器保存

####
---
注意：从这里我们可以看出，JWT 仅仅是对 payload 做了简单的 sign 和 encode 处理，并未被加密，并不能保证数据的安全性，所以建议只在其中保存非敏感的用于身份验证的数据。


### 服务端验证

服务端接收到 token 之后，会逆向构造过程，decode 出 JWT 的三个部分，这一步可以得到 sign 的算法及 payload，结合服务端配置的 secretKey，可以再次进行 $Signature 的生成得到新的 $Signature，与原有的 $Signature 比对以验证 token 是否有效，完成用户身份的认证，验证通过才会使用 payload 的数据。 （过程详见node-jsonwebtoken、node-jws、node-jwa）


如你所见，`服务端最终只是为了验证 $Signature 是否仍是自己当时下发给 client 的那个`，如果验证通过，则说明该 JWT 有效并且来自可靠来源，否则说明可能是对应用程序的潜在攻击，以此完成认证。


### JWT 优势和问题

JWT 拥有基于 Token 的会话管理方式所拥有的一切优势，不依赖 Cookie，使得其可以防止 CSRF 攻击，也能在禁用 Cookie 的浏览器环境中正常运行。

 ---
而 JWT 的最大优势是服务端不再需要存储 Session，使得服务端认证鉴权业务可以方便扩展，避免存储 Session 所需要引入的 Redis 等组件，降低了系统架构复杂度。



但这也是 JWT 最大的劣势，由于有效期存储在 Token 中，`JWT Token 一旦签发，就会在有效期内一直可用，无法在服务端废止，当用户进行登出操作，只能依赖客户端删除掉本地存储的 JWT Token`，如果需要禁用用户，单纯使用 JWT 就无法做到了。



### refresh Token

Refresh Token 的有效期会比较长，而 Access Token 的有效期比较短，当 Access Token 由于过期而失效时，使用 Refresh Token 就可以获取到新的 Access Token，如果 Refresh Token 也失效了，用户就只能重新登录了。

在 JWT 的实践中，引入 Refresh Token，将会话管理流程改进如下。
---

- 客户端使用用户名密码进行认证
- 服务端生成有效时间较短的 Access Token（例如 10 分钟），和有效时间较长的 Refresh Token（例如 7 天）
- 客户端访问需要认证的接口时，携带 Access Token
- 如果 Access Token 没有过期，服务端鉴权后返回给客户端需要的数据
- 如果携带 Access Token 访问需要认证的接口时鉴权失败（例如返回 401 错误），则客户端使用 Refresh Token 向刷新接口申请新的 Access Token
- 如果 Refresh Token 没有过期，服务端向客户端下发新的 Access Token
- 客户端使用新的 Access Token 访问需要认证的接口

---
![avatar](/img/refresh-token.jpg)


将生成的 Refresh Token 以及过期时间存储在服务端的数据库中，由于 Refresh Token 不会在客户端请求业务接口时验证，只有在申请新的 Access Token 时才会验证，所以将 Refresh Token 存储在数据库中，不会对业务接口的响应时间造成影响，也不需要像 Session 一样一直保持在内存中以应对大量的请求


---
上述的架构，提供了服务端禁用用户 Token 的方式，当用户需要登出或禁用用户时，只需要将服务端的 Refresh Token 禁用或删除，用户就会在 Access Token 过期后，由于无法获取到新的 Access Token 而再也无法访问需要认证的接口。这样的方式虽然会有一定的窗口期（取决于 Access Token 的失效时间），但是结合用户登出时客户端删除 Access Token 的操作，基本上可以适应常规情况下对用户认证鉴权的精度要求。

### 总结

JWT 的使用，提高了开发者开发用户认证鉴权功能的效率，降低了系统架构复杂度，避免了大量的数据库和缓存查询，降低了业务接口的响应延迟。然而 JWT 的这些优点也增加了 Token 管理上的难度，通过引入 Refresh Token，既能继续使用 JWT 所带来的优势，又能使得 Token 管理的精度符合业务的需求。
