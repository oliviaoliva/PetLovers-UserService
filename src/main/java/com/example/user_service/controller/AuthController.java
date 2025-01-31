// package com.example.user_service.controller;

// import com.example.user_service.model.User;
// import com.example.user_service.repository.UserRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.web.bind.annotation.*;

// import java.util.Optional;

// @RestController
// @RequestMapping("/auth")
// public class AuthController {

//     @Autowired
//     private UserRepository userRepository;

//     @Autowired
//     private PasswordEncoder passwordEncoder;

//     // @Autowired
//     // private JwtUtil jwtUtil;

//     @PostMapping("/register")
//     public String register(@RequestBody User user) {
//         user.setPassword(passwordEncoder.encode(user.getPassword()));
//         userRepository.save(user);
//         return "Usuário registrado!";
//     }

//     @PostMapping("/login")
//     public ResponseEntity<String> login(@RequestBody User user) {
//         Optional<User> foundUser = userRepository.findByUsername(user.getUsername());

//         if (foundUser.isPresent() && passwordEncoder.matches(user.getPassword(), foundUser.get().getPassword())) {
//             return ResponseEntity.ok("Login realizado com sucesso!"); // 🔥 Agora só retorna uma mensagem
//         }
//         return ResponseEntity.status(401).body("Credenciais inválidas!");
//     }

// }
