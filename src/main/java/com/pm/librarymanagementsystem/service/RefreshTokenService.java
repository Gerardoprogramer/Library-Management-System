package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.modal.RefreshToken;
import com.pm.librarymanagementsystem.modal.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyExpiration(RefreshToken token);

    void delete(RefreshToken token);

    void deleteByToken(String token);
}
