package com.duoc.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.context.WebApplicationContext;
import com.duoc.backend.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserTest {

    //@MockBean
    //private UserDetailsService userDetailsService;

    @MockBean
    MyUserDetailsService userDetailsService;

    @Autowired
    private WebApplicationContext context;

    @Test
    public void testGetUserByUsername() {
        // Arrange
        String username = "testuser";
        User user = new User();
        user.setId(1);
        user.setName(username);
        user.setEmail("test@example.com");
        user.setPassword("testpassword");

        when(userDetailsService.loadUserByUsername(username)).thenReturn(user);

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Assert
        assert userDetails != null;
        assert userDetails.getUsername().equals(username);
        assert userDetails.getPassword().equals("testpassword");
        //assertEquals(user, userDetails);
        assert user.getId().equals(1);
        assert user.getName().equals(username);
        assert user.getEmail().equals("test@example.com");

        try {
            userDetails.getAuthorities();
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().equals("Unimplemented method 'getAuthorities'");
        }

        try {
            userDetails.isAccountNonExpired();
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().equals("Unimplemented method 'isAccountNonExpired'");
        }

        try {
            userDetails.isAccountNonLocked();
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().equals("Unimplemented method 'isAccountNonLocked'");
        }

        try {
            userDetails.isCredentialsNonExpired();
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().equals("Unimplemented method 'isCredentialsNonExpired'");
        }

        try {
            userDetails.isEnabled();
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().equals("Unimplemented method 'isEnabled'");
        }

    }
}
