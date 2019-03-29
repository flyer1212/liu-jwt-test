package hello.filter;

import hello.constrant.ConstantKey;
import hello.entity.GrantedAuthorityImpl;
import hello.exception.TokenException;
import io.jsonwebtoken.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

// 进行鉴权
public class JWTAuthenticationFilter extends BasicAuthenticationFilter {
    public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        //super.doFilterInternal(request, response, chain);
        String header = request.getHeader("Authorization");
        // 这里的每次请求都会执行，除了在WebSecurity 里面配置的放行的url
        // 放行是因为WebSecurity 在拦截
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            System.out.println("Token为空");
        } else {
            System.out.println(token + "=------");
        }

        try {
            if (token != null) {
                String user = Jwts.parser()
                        .setSigningKey(ConstantKey.SIGNING_KEY)
                        .parseClaimsJws(token.replace("Bearer ", ""))
                        .getBody()
                        .getSubject();

                System.out.println(user.toString() + "---------------");
                if (user != null) {
                    String[] split = user.split("-")[1].split(",");
                    ArrayList<GrantedAuthority> authorities = new ArrayList<>();

                    for (int i = 0; i < split.length; i++) {
                        authorities.add(new GrantedAuthorityImpl(split[i]));
                    }
                    // 只要告诉spring-security该用户是否已登录，是什么角色，拥有什么权限就可以
                    //  这里从token中获取用户信息并新建一个token

                    System.out.println(authorities.get(0).getAuthority() +" ----authorities");
                    return new UsernamePasswordAuthenticationToken(user, null, authorities);
                }
                return null;
            }
        } catch (ExpiredJwtException e) {
            logger.error("Token已过期: {} " + e);
            throw new TokenException("Token已过期");
        } catch (UnsupportedJwtException e) {
            logger.error("Token格式错误: {} " + e);
            throw new TokenException("Token格式错误");
        } catch (MalformedJwtException e) {
            logger.error("Token没有被正确构造: {} " + e);
            throw new TokenException("Token没有被正确构造");
        } catch (SignatureException e) {
            logger.error("签名失败: {} " + e);
            throw new TokenException("签名失败");
        } catch (IllegalArgumentException e) {
            logger.error("非法参数异常: {} " + e);
            throw new TokenException("非法参数异常");
        }
        return null;
    }
}
