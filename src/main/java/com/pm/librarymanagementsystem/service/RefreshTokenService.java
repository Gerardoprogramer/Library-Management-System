package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.modal.User;

public interface RefreshTokenService {

    String createRefreshToken(User user);

    User validateAndConsume(String rawToken);

    void deleteByToken(String rawToken);
}
