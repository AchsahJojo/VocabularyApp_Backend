package com.vocab.controller;

import com.vocab.model.User;
import com.vocab.model.VocabList;
import com.vocab.repository.UserRepository;
import com.vocab.repository.VocabListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VocabListRepository vocabListRepository;

    @InjectMocks
    private AuthController authController;

    private BCryptPasswordEncoder passwordEncoder;
    private User testUser;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        
        testUser = new User();
        testUser.setId("user123");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setSecurityQuestion("What is your pet's name?");
        testUser.setSecurityAnswer("Fluffy");
    }

    // ==================== REGISTER TESTS ====================

    @Test
    void testRegister_Success() {
        // Arrange
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("password123");
        newUser.setSecurityQuestion("What is your pet's name?");
        newUser.setSecurityAnswer("Fluffy");

        User savedUser = new User();
        savedUser.setId("newUserId");
        savedUser.setEmail(newUser.getEmail());
        savedUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(vocabListRepository.save(any(VocabList.class))).thenReturn(new VocabList());

        // Act
        ResponseEntity<?> response = authController.register(newUser);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Sign Up Successful", body.get("message"));
        assertEquals("newUserId", body.get("userId"));

        verify(userRepository).existsByEmail("newuser@example.com");
        verify(userRepository).save(any(User.class));
        verify(vocabListRepository).save(any(VocabList.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        // Arrange
        User newUser = new User();
        newUser.setEmail("existing@example.com");
        newUser.setPassword("password123");
        newUser.setSecurityQuestion("Question?");
        newUser.setSecurityAnswer("Answer");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act
        ResponseEntity<?> response = authController.register(newUser);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("This email is already registered", body.get("error"));

        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_MissingFields() {
        // Arrange
        User newUser = new User();
        newUser.setEmail("test@example.com");
        // Missing password, security question, and answer

        // Act
        ResponseEntity<?> response = authController.register(newUser);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("All fields are required", body.get("error"));

        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== LOGIN TESTS ====================

    @Test
    void testLogin_Success() {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "test@example.com");
        credentials.put("password", "password123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<?> response = authController.login(credentials);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Login successful", body.get("message"));
        assertEquals("user123", body.get("userId"));

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testLogin_UserNotFound() {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "nonexistent@example.com");
        credentials.put("password", "password123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = authController.login(credentials);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("User not found", body.get("error"));

        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void testLogin_IncorrectPassword() {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "test@example.com");
        credentials.put("password", "wrongpassword");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<?> response = authController.login(credentials);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Incorrect password", body.get("error"));
    }

    // ==================== FORGOT PASSWORD TESTS ====================

    @Test
    void testForgotPassword_Success() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<?> response = authController.forgotPassword(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("What is your pet's name?", body.get("securityQuestion"));

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testForgotPassword_UserNotFound() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("email", "nonexistent@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = authController.forgotPassword(request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("No account found with this email", body.get("error"));
    }

    // ==================== VERIFY SECURITY TESTS ====================

    @Test
    void testVerifySecurity_Success() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("securityAnswer", "Fluffy");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<?> response = authController.verifySecurity(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Security answer verified", body.get("message"));
    }

    @Test
    void testVerifySecurity_IncorrectAnswer() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("securityAnswer", "WrongAnswer");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<?> response = authController.verifySecurity(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Incorrect answer", body.get("error"));
    }

    // ==================== RESET PASSWORD TESTS ====================

    @Test
    void testResetPassword_Success() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("newPassword", "newPassword123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        ResponseEntity<?> response = authController.resetPassword(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Your password has been reset", body.get("message"));

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testResetPassword_EmptyPassword() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("newPassword", "");

        // Act
        ResponseEntity<?> response = authController.resetPassword(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Password cannot be empty", body.get("error"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testResetPassword_UserNotFound() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("email", "nonexistent@example.com");
        request.put("newPassword", "newPassword123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = authController.resetPassword(request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("User not found", body.get("error"));
    }
}