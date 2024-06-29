package com.duoc.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

//import com.duoc.backend.jwtutils.JwtAuthenticationEntryPoint;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;

@Configuration
@Service
public class MyUserDetailsService implements UserDetailsService {

        Logger logger 
        = LoggerFactory.getLogger(MyUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;
    

    @Override
    public UserDetails loadUserByUsername(String name) {
        User user = userRepository.findByName(name);
        System.out.println("MyUserDetailsService.loadUserByUsername() user = " + user);
        if (user == null) {
            throw new UsernameNotFoundException(name);
        }
        return user;
    }



}

