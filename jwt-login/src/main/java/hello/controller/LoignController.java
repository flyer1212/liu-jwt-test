package hello.controller;


import hello.constrant.ConstantKey;
import hello.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/user")
public class LoignController {

    @GetMapping("/hello")
    public String getHello() {
        return "hello";
    }

    @GetMapping(value = "/login")
    public String login(  HttpServletResponse response) {
        User user = new User("12212","123");

        if (user != null) {
            System.out.println(user.toString() + "------login controller---");


            // 这里可以根据用户信息查询对应的角色信息，这里为了简单，我直接设置个空list
            List<String> roleInfoList = new ArrayList<>();
            roleInfoList.add("ROLE_USER");
            String subject = user.getUsername() + "-" + roleInfoList;
            System.out.println(new Date(System.currentTimeMillis() + 10) + "----- login date");


            Calendar calendar = Calendar.getInstance();
            // 设置签发时间
            Date nowTime = calendar.getTime();

            // 设置过期时间
            calendar.setTime(new Date());
            calendar.add(Calendar.MINUTE, 10); //10分钟
            Date expireTime = calendar.getTime();


            String token = Jwts.builder()
                    .setSubject(subject)
                    .setIssuedAt(nowTime)
                    .setExpiration(expireTime)
                    .signWith(SignatureAlgorithm.HS512, ConstantKey.SIGNING_KEY)
                    .compact();
            response.addHeader("Authorization", "Bearer " + token);
            return "ok";
        } else {
            return "no body";
        }
    }
}
