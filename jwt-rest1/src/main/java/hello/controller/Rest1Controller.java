package hello.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class Rest1Controller {

    @GetMapping("/hello")
    @PreAuthorize("hasRole('USER')")
    public String getHello() {
        return "hello";
    }
}
