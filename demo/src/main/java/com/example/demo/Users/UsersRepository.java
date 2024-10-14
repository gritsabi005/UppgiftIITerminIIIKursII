package com.example.demo.Users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsersRepository extends JpaRepository<Users, Long> {
    //Optional<User> findByEPostaddressAndLosenord(String ePostaddress, String losenord);
    List<Users> findByEmailAddressAndPassword(String emailAddress, String password);
    List<Users> findByEmailAddress(String emailAddress);


}
