package com.example.user_service.rabbitmq;

import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import jakarta.transaction.Transactional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class PetCreatedListener {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PetCreatedListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    @RabbitListener(queues = "pet_created", ackMode = "MANUAL")
    public void onPetCreated(String message, Channel channel) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String petId = jsonNode.get("petId").asText();
            String userId = jsonNode.get("ownerId").asText();


            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.getPetIds().add(petId);
                userRepository.save(user);
                System.out.println("✅ Pet " + petId + " vinculado ao usuário " + userId);
            } else {
                System.out.println("⚠ Usuário " + userId + " não encontrado!");
            }

            channel.basicAck(1, false); 

        } catch (Exception e) {
            System.err.println("❌ Erro ao processar mensagem: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
