package com.example.demo.Messages;

import com.example.demo.Messages.Messages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessagesRepository extends JpaRepository<Messages, Long> {

    @Query("SELECT m FROM Messages m WHERE m.user.id = :userId")
    List<Messages> findAllByUserId(@Param("userId") Long userId);
}
