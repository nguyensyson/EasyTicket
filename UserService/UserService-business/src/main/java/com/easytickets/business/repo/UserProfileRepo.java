package com.easytickets.business.repo;

import com.easytickets.business.dto.UserProfileDto;

public interface UserProfileRepo {
    UserProfileDto save(UserProfileDto profile);
}
