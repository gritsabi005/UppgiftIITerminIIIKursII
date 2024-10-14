package com.example.demo.Users;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class UsersController {
    private static final String SECRET = "MestinyaKauCariPenggantiDirikuSajaKarenaKitaSudahTakSalingBicara";
    @Autowired
    private UsersRepository usersRepository;
        @PostMapping("/register")
        @Transactional
        public Map<String, String> register(@RequestBody Users user) {
            System.out.println("starting to create now");
            /*if (usersRepository.findByEmailAddress(user.getEmailAddress()) != null) {
                return Map.of("error", "Email address already in use.");
            }*/
            String hashedPassword = hashPassword(user.getPassword());
            user.setPassword(hashedPassword);
            System.out.println("password is hashed and ready to be saved");
            usersRepository.save(user);
            System.out.println("success");
            return Map.of("message", "User registered successfully!");
        }
        public String hashPassword(String password) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(password.getBytes());
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    hexString.append(String.format("%02x", b));
                }
                return hexString.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }
        }
        @DeleteMapping("/delete")
        public Map<String, String> deleteAccount(@RequestBody Map<String, Object> credentials) {
            String username = credentials.get("username").toString();
            String password = credentials.get("password").toString();
            String hashedPassword = hashPassword(password);
            List<Users> userList = usersRepository.findByEmailAddress(username);
            if (userList != null && !userList.isEmpty()) {
                Users user = userList.get(0);
                if (user.getPassword().equals(hashedPassword)) {
                    usersRepository.delete(user);
                    return Map.of("message", "Account deleted successfully.");
                } else {
                    return Map.of("error", "Invalid password.");
                }
            } else {
                return Map.of("error", "User not found.");
            }
        }
    @PostMapping("/logInNow")
    public Map<String, String> logInNow(@RequestBody Map<String, Object> credentials) throws JOSEException {
        String username = credentials.get("username").toString();
        String password = credentials.get("password").toString();
        String hashedPassword = hashPassword(password);
        List<Users> userTryingToLogIn = usersRepository.findByEmailAddressAndPassword(username, hashedPassword);
        if (userTryingToLogIn != null && !userTryingToLogIn.isEmpty()) {
            Users user = userTryingToLogIn.get(0);
            JWSSigner signer = new MACSigner(SECRET);
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(String.valueOf(user.getID()))
                    .issuer("https://example.com")
                    .expirationTime(new Date(new Date().getTime() + 60 * 1000 * 60)) // 1 hour expiration
                    .build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);
            String jwt = signedJWT.serialize();
            return Map.of("token", jwt);
        } else {
            return Map.of("error", "Invalid credentials");
        }

        /*@PostMapping("/logInNow")
        public Map<String, String> logInNow(@RequestBody Map<String, Object> credentials) throws JOSEException {
        String username = credentials.get("username").toString();
        String password = credentials.get("password").toString();
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        String hashedPassword = hashPassword(password);
        List<Users> userTryingToLogIn = usersRepository.findByEmailAddressAndPassword(username, hashedPassword);
        System.out.println(userTryingToLogIn);

            if (userTryingToLogIn != null && !userTryingToLogIn.isEmpty()) {
                Users user = userTryingToLogIn.get(0);
                System.out.println("User is: " + user);
                JWSSigner signer = new MACSigner(SECRET);
                JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                        .subject(String.valueOf(user.getID()))
                        .issuer("https://example.com")
                        .expirationTime(new Date(new Date().getTime() + 60 * 1000 * 60))
                        .build();
                SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
                signedJWT.sign(signer);
                String jwt = signedJWT.serialize();
                return Map.of("token", jwt);
            } else {
                return Map.of("error", "invalid credentials");
            }*/
        /*if("admin".equals(username) && "password".equals(password)){
            JWSSigner signer = new MACSigner(SECRET);
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject("admin")
                    .issuer("https://example.com")
                    .expirationTime(new Date(new Date().getTime() + 60 * 1000 * 60))
                    .build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);
            String jwt = signedJWT.serialize();
            System.out.println(jwt);
            return Map.of("token", jwt);
        }else {
            return Map.of("error", "invalid credentials");
        }*/
    }
}
