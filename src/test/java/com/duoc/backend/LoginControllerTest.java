package com.duoc.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;


@WebMvcTest(LoginController.class)
public class LoginControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    JWTAuthenticationConfig jwtAuthtenticationConfig; 

    @MockBean
    MyUserDetailsService userDetailsService;

    @MockBean
    UserDetails userDetails;



    @BeforeEach
    public void setUp() throws Exception {

        String name = "user";

        when(userDetails.getPassword()).thenReturn("password");
        when(userDetailsService.loadUserByUsername(name)).thenReturn(userDetails);
        when(jwtAuthtenticationConfig.getJWTToken(name)).thenReturn("Bearer");
        when(jwtAuthtenticationConfig.getJWTToken("user")).thenReturn("Bearer");
   
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }


    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    public void testLogin() throws Exception {
        System.out.println("LoginControllerTest.testLogin()");
        mockMvc.perform(post("/login")
                .param("user", "user")
                .param("encryptedPass", "password"))
                .andExpect(status().isOk())
                .andExpect(content().string("Bearer"))
                .andReturn().getResponse().getContentAsString();

    }
}
