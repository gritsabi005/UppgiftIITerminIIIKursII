package com.example.demo.Messages;

import com.example.demo.Users.Users;
import com.example.demo.Users.UsersRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class MessagesController {
    @Autowired
    private MessagesRepository messagesRepository;
    @Autowired
    private UsersRepository usersRepository;
    private final String secretKeyString = "MestinyaKauCariPenggantiDirikuSajaKarenaKitaSudahTakSalingBicara";
    private final SecretKey secretKey;
    public MessagesController() {
        byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
        this.secretKey = new SecretKeySpec(keyBytes, 0, 16, "AES");
    }
    @PostMapping("/writeMessages")
    public Map<String, String> writeMessages(@RequestBody Map<String, Object> incomingMessages, @RequestHeader("Authorization") String token) throws Exception {
        if (!validateJWT(token)){
            return Map.of("error", "Invalid credentials");
        }
        Long usersId = users_idFromToken(token);
        Users user = usersRepository.findById(usersId)
                .orElseThrow(() -> new Exception("User Not Found"));
        String content = incomingMessages.get("content").toString();
        String encryptedMessage = AESKryptering(content, secretKey);
        Messages newMessage = new Messages(encryptedMessage, user);
        messagesRepository.save(newMessage);
        return Map.of("status", "Message saved");
    }
    @GetMapping("/readMessages")
    public List<String> readMessages(@RequestHeader("Authorization") String token) throws Exception {
        if (!validateJWT(token)){
            throw new Exception("Invalid credentials");
        }
        Long usersId = users_idFromToken(token);
        System.out.println(usersId);
        List<Messages> messagesList = messagesRepository.findAllByUserId(usersId);
        return messagesList.stream()
                .map(msg -> {
                    try {
                        return AESDekryptering(msg.getMessages(), secretKey);
                    } catch (Exception e) {
                        System.err.println("Error decrypting message: " + e.getMessage());
                        return "Error decrypting message";
                    }
                })
                .collect(Collectors.toList());
    }
    private Long users_idFromToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token format");
        }
        String payload = parts[1]; //payload is the second part, the first is header and the third is signature I think
        byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
        String decodedString = new String(decodedBytes);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(decodedString);
            return jsonNode.get("sub").asLong();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JWT payload", e);
        }
    }
    private boolean validateJWT(String token) {
        return true;
    }
    private SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        return keyGen.generateKey();
    }
    public String AESKryptering(String data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    public String AESDekryptering(String encryptedData, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }
}
