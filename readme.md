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