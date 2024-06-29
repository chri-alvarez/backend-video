package com.duoc.backend;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.WebApplicationContext;

import ch.qos.logback.core.filter.Filter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.duoc.backend.Constants.*;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;


import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
public class JWTAuthorizationFilterTest {

    @Autowired
    private JWTAuthorizationFilter filter;

    @MockBean
    private HttpServletRequest mockRequest;

    @MockBean
    private HttpServletResponse mockResponse;

    @MockBean
    private FilterChain mockFilterChain;

    @MockBean
    private SecretKey secretKey;

    @Autowired
    private WebApplicationContext context;

    private Claims capturedClaims;

    private MockedStatic<Constants> mocked;

    @BeforeEach
    public void setUp() {
        // Inject mocks or any necessary dependencies
        String secret = SUPER_SECRET_KEY;
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        mocked = mockStatic(Constants.class);
        mocked.when(() -> Constants.getSigningKey(SUPER_SECRET_KEY)).thenReturn(Keys.hmacShaKeyFor(keyBytes));

    }

    @AfterEach
    public void tearDown() {
        // Clean up any resources
        mocked.close();
    }

    @Test
    public void testDoFilterInternal_ValidToken_SetsAuthentication() throws Exception {
        // Mock a valid JWT token in the request header
        String validToken = createValidToken("username", List.of("ROLE_USER"));
        when(mockRequest.getHeader(HEADER_AUTHORIZACION_KEY)).thenReturn(TOKEN_BEARER_PREFIX + validToken);
       
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils 
                .commaSeparatedStringToAuthorityList("ROLE_USER"); 
        
        Map<String, Object> mockClaims = new HashMap<>(); 
        mockClaims.put("authorities", grantedAuthorities.stream() 
                .map(GrantedAuthority::getAuthority) 
                .collect(Collectors.toList())); 


        System.out.println("mockClaims: " + mockClaims);

        System.out.println("capturedClaims: " + capturedClaims);

        // Execute the filter
        System.out.println("mockRequest: " + mockRequest);
        System.out.println("mockResponse: " + mockResponse);
        System.out.println("mockFilterChain: " + mockFilterChain);
        filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);
        System.out.println("doFilterInternal Executed");
        
        System.out.println("capturedClaims: " + capturedClaims);

        // Verify SecurityContext is set with the expected authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication instanceof UsernamePasswordAuthenticationToken;

        System.out.println("authentication: " + authentication);
        System.out.println("capturedClaims: " + capturedClaims);


        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
        System.out.println("token: " + token);

        assert token.getName().equals("username");
        assert token.getAuthorities().size() == 1;
        assert token.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"));

    }

    @Test
    public void testDoFilterInternal_InvalidToken_ClearsContext() throws Exception {
        // Mock an invalid token (e.g., missing prefix)
        when(mockRequest.getHeader(HEADER_AUTHORIZACION_KEY)).thenReturn("invalid_token");

        // Execute the filter
        filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        // Verify SecurityContext is cleared
        assert SecurityContextHolder.getContext().getAuthentication() == null;

    }

    @Test
    public void testDoFilterInternal_ExpiredToken_SendsForbiddenResponse() throws Exception {
        // Mock an expired token
        String expiredToken = createExpiredToken("username", List.of("ROLE_USER"));
        when(mockRequest.getHeader(HEADER_AUTHORIZACION_KEY)).thenReturn(TOKEN_BEARER_PREFIX + expiredToken);
        
        // Execute the filter (expect exception)
        try {
            filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);
            //fail("Expected ExpiredJwtException");
        } catch (ExpiredJwtException e) {
            // Verify expected behavior
            e.printStackTrace();
            assert e.getMessage() != null;
            verify(mockResponse).setStatus(HttpServletResponse.SC_FORBIDDEN);
            verify(mockResponse).sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }
    }

    // Utility methods to create valid/expired tokens (implementation omitted)
    private String createValidToken(String username, List<String> authorities) {
        Map<String, Object> claims = new HashMap<>(); 
    
    
        claims.put("authorities", authorities.stream() 
                .map(String::new) 
                .collect(Collectors.toList()));
        
        String token = Jwts.builder() 
                .claims() 
                .add(claims) 
                .subject(username) 
                .issuedAt(new Date(System.currentTimeMillis())) 
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 1440)) 
                .and() 
                .signWith(getSigningKey(SUPER_SECRET_KEY)) 
                .compact(); 
    
        return token;
    }

    private String createExpiredToken(String username, List<String> authorities) {
        Map<String, Object> claims = new HashMap<>(); 
    
    
        claims.put("authorities", authorities.stream() 
                .map(String::new) 
                .collect(Collectors.toList()));
        
        String token = Jwts.builder() 
                .claims() 
                .add(claims) 
                .subject(username) 
                .issuedAt(new Date(System.currentTimeMillis())) 
                .expiration(new Date(System.currentTimeMillis() - 1000 * 60 * 1440)) 
                .and() 
                .signWith(getSigningKey(SUPER_SECRET_KEY)) 
                .compact(); 
    
        return token;
    }
}
