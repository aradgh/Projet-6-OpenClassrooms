package com.paymybuddy.service;

import com.paymybuddy.model.User;
import com.paymybuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Crée un utilisateur test
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        // Pour les tests, la valeur du mot de passe est moins importante
        testUser.setPassword("password");
        testUser.setConnections(new ArrayList<>());
    }

    @Test
    void testGetUserByEmail_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        User result = userService.getUserByEmail("test@example.com");
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testGetUserByEmail_NotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            userService.getUserByEmail("notfound@example.com"));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void testRegisterUser_Success() {
        String username = "newuser";
        String email = "new@example.com";
        String password = "newpass";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        // Simuler l'enregistrement initial et la ré-enregistrement
        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername(username);
        savedUser.setEmail(email);
        // On simule que le mot de passe est haché (avec BCrypt, la longueur est généralement 60)
        savedUser.setPassword(new BCryptPasswordEncoder().encode(password));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerUser(username, email, password);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        // Vérifier que la méthode createAccount a été appelée pour le nouvel utilisateur
        verify(accountService, times(1)).createAccount(savedUser);
    }

    @Test
    void testRegisterUser_EmailAlreadyUsed() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> userService.registerUser("anyuser", "test@example.com", "pass"));
        assertEquals("Email already in use.", ex.getMessage());
    }

    @Test
    void testGetUserById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        Optional<User> result = userService.getUserById(1L);
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<User> result = userService.getUserById(99L);
        assertFalse(result.isPresent());
    }

    @Test
    void testGetAllUsers() {
        List<User> users = new ArrayList<>();
        users.add(testUser);
        when(userRepository.findAll()).thenReturn(users);
        List<User> result = userService.getAllUsers();
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetConnections_Success() {
        List<User> connections = new ArrayList<>();
        User friend = new User();
        friend.setId(2L);
        friend.setUsername("friend");
        connections.add(friend);
        testUser.setConnections(connections);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        List<User> result = userService.getConnections(1L);
        assertEquals(1, result.size());
    }

    @Test
    void testGetConnections_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> userService.getConnections(1L));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void testAddConnection_Success() {
        User newConnection = new User();
        newConnection.setId(2L);
        newConnection.setUsername("friend");
        testUser.setConnections(new ArrayList<>());

        when(userRepository.save(testUser)).thenReturn(testUser);
        userService.addConnection(testUser, newConnection);
        assertEquals(1, testUser.getConnections().size());
        assertTrue(testUser.getConnections().contains(newConnection));
    }

    @Test
    void testAddConnection_AlreadyExists() {
        User newConnection = new User();
        newConnection.setId(2L);
        newConnection.setUsername("friend");
        List<User> connections = new ArrayList<>();
        connections.add(newConnection);
        testUser.setConnections(connections);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> userService.addConnection(testUser, newConnection));
        assertEquals("Cette relation existe déjà.", ex.getMessage());
    }

    @Test
    void testLoadUserByUsername_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        UserDetails userDetails = userService.loadUserByUsername("test@example.com");
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
    }

    @Test
    void testLoadUserByUsername_NotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () ->
            userService.loadUserByUsername("notfound@example.com"));
    }

    @Test
    void testUpdateProfile_UpdatePassword() {
        testUser.setUsername("olduser");
        testUser.setEmail("old@example.com");
        testUser.setPassword("oldpass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateProfile(1L, "newuser", "new@example.com", "newpass");
        assertEquals("newuser", updated.getUsername());
        assertEquals("new@example.com", updated.getEmail());
        // Le mot de passe ne doit pas être "newpass" en clair
        assertNotEquals("newpass", updated.getPassword());
    }

    @Test
    void testUpdateProfile_NoPasswordChange() {
        testUser.setUsername("olduser");
        testUser.setEmail("old@example.com");
        testUser.setPassword("oldpass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateProfile(1L, "newuser", "new@example.com", "");
        assertEquals("newuser", updated.getUsername());
        assertEquals("new@example.com", updated.getEmail());
        // Si aucun nouveau mot de passe n'est fourni, l'ancien doit rester inchangé
        assertEquals("oldpass", updated.getPassword());
    }
}
