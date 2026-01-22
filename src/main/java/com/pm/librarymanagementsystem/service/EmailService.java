package com.pm.librarymanagementsystem.service;

public interface EmailService {

    void sendEmail(String to, String subject, String body);
}
