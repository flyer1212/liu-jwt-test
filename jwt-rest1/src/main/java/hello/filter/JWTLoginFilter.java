package hello.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * 这个类在使用security 默认页面的时候有用，
 *
 * 验证用户名密码正确后，生成一个token，并将token返回给客户端
 * attemptAuthentication ：接收并解析用户凭证。
 * successfulAuthentication ：用户成功登录后，这个方法会被调用，我们在这个方法里生成token。
 */
// 用户账号的验证
public class JWTLoginFilter extends UsernamePasswordAuthenticationFilter {

    Logger logger = LoggerFactory.getLogger(getClass());

    private AuthenticationManager authenticationManager;

    public JWTLoginFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    // 接受并解析用户登录凭证
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        try {
            User user = new ObjectMapper()
                    .readValue(request.getInputStream(), User.class);

            logger.info(user.getUsername() + "---------" + user.getPassword());
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            user.getPassword(),
                            new ArrayList<>())
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 用户登录成功后，这个方法会被调用，在这个里生成token
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        // super.successfulAuthentication(request, response, chain, authResult);

        Calendar calendar = Calendar.getInstance();
        // 设置签发时间
        Date now = calendar.getTime();

        // 设置过期时间
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 10); //10分钟
        Date expireTime = calendar.getTime();

        String token = Jwts.builder()
                .setSubject(((org.springframework.security.core.userdetails.User) authResult
                        .getPrincipal()).getUsername())
                .setIssuedAt(now)
                .setExpiration(expireTime)
                .signWith(SignatureAlgorithm.HS512, "MyJwtSecret")
                .compact();
        logger.info(token + " ---- token ");
        response.addHeader("Authorization", "Bearer " + token);
    }
}
