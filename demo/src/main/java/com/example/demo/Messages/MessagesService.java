package com.example.demo.Messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessagesService {

    @Autowired
    private MessagesRepository messagesRepository;

    public List<Messages> getMessagesByUserId(Long userID) {
        return messagesRepository.findAllByUserId(userID);
    }
}
