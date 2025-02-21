package com.example.user_service;

import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        // Configuração inicial
        user = new User();
        user.setId("123");
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setPassword("secret");
        user.setName("Test Name");
        user.setAddress("123 Test St");
        user.setPhone("555-1234");
    }

    @Test
    void testCreateUser() {
        // Dado que o repositório retorna o mesmo usuário ao salvar
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Execução
        User created = userService.createUser(user);

        // testando 
        verify(userRepository, times(1)).save(user);  // verifica se userRepository.save foi chamado
        assertNotNull(created);
        assertEquals("testUser", created.getUsername());
        assertEquals("test@example.com", created.getEmail());
    }

    @Test
    void testGetUserById_UserExists() {
        // Dado que o usuário existe no repositório
        when(userRepository.findById("123")).thenReturn(Optional.of(user));

        // Executando
        Optional<User> foundUser = userService.getUserById("123");

        // testando
        verify(userRepository, times(1)).findById("123");
        assertTrue(foundUser.isPresent());
        assertEquals("123", foundUser.get().getId());
        assertEquals("testUser", foundUser.get().getUsername());
    }

    @Test
    void testGetUserById_UserNotExists() {
        // Dado que o repositório não encontra o usuário
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        // Executando
        Optional<User> notFound = userService.getUserById("999");

        // Testando
        verify(userRepository, times(1)).findById("999");
        assertFalse(notFound.isPresent());
    }

    @Test
    void testGetAllUsers() {
        // Dado que temos uma lista de usuários
        List<User> userList = new ArrayList<>();
        userList.add(user);
        userList.add(new User()); // outro user qualquer

        when(userRepository.findAll()).thenReturn(userList);

        // Executando
        List<User> result = userService.getAllUsers();

        // Testando
        verify(userRepository, times(1)).findAll();
        assertEquals(2, result.size());
    }

    @Test
    void testUpdateUser_UserExists() {
        // Configuração
        when(userRepository.findById("123")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedInfo = new User();
        updatedInfo.setUsername("newUsername");
        updatedInfo.setEmail("new-email@example.com");
        updatedInfo.setPassword("newSecret");
        updatedInfo.setName("New Name");
        updatedInfo.setAddress("New Address");
        updatedInfo.setPhone("999-8888");

        // Execução
        User updatedUser = userService.updateUser("123", updatedInfo);

        // Testes
        verify(userRepository).findById("123");
        verify(userRepository).save(any(User.class));
        assertEquals("newUsername", updatedUser.getUsername());
        assertEquals("new-email@example.com", updatedUser.getEmail());
        assertEquals("newSecret", updatedUser.getPassword());
        assertEquals("New Name", updatedUser.getName());
        assertEquals("New Address", updatedUser.getAddress());
        assertEquals("999-8888", updatedUser.getPhone());
    }

    @Test
    void testUpdateUser_UserNotExists() {
        // Dado que o usuário não existe
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        // Execução/Teste
        assertThrows(RuntimeException.class, () -> {
            userService.updateUser("999", user);
        });

        verify(userRepository).findById("999");
        verify(userRepository, never()).save(any(User.class)); 
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userRepository).deleteById("123");

        userService.deleteUser("123");

        verify(userRepository, times(1)).deleteById("123");
    }
}
