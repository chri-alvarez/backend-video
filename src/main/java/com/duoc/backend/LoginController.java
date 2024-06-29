package com.duoc.backend; 

 

import com.duoc.backend.User; 
import com.duoc.backend.JWTAuthenticationConfig; 


import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.security.core.userdetails.UserDetails; 
import org.springframework.web.bind.annotation.PostMapping; 
import org.springframework.web.bind.annotation.RequestParam; 
import org.springframework.web.bind.annotation.RestController; 
 

@RestController 
public class LoginController { 

    @Autowired 
    JWTAuthenticationConfig jwtAuthtenticationConfig; 

    @Autowired 
    private MyUserDetailsService userDetailsService; 

    @PostMapping("login") 
    public String login( 
            @RequestParam("user") String username, 
            @RequestParam("encryptedPass") String encryptedPass) { 

        System.out.println("LoginController.login()");

        final UserDetails userDetails = userDetailsService.loadUserByUsername(username); 

        System.out.println("LoginController.login() userDetails = " + userDetails);

        if (!userDetails.getPassword().equals(encryptedPass)) { 
            System.out.println("Invalid login " + userDetails.getPassword() + " " + encryptedPass);
            throw new RuntimeException("Invalid login"); 
        } 

        String token = jwtAuthtenticationConfig.getJWTToken(username); 

        return token; 
    } 

} 

 