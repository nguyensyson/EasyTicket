package com.easytickets.business.services;

import com.easytickets.business.dto.RegisterRequest;
import com.easytickets.business.dto.RegisterResponse;
import com.easytickets.business.dto.UserRole;

public interface UserService {
    RegisterResponse register(RegisterRequest request, UserRole role);
}
