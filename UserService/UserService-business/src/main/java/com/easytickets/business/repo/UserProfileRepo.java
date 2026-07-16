package com.easytickets.business.repo;

import com.easytickets.business.dto.UserProfileDto;

import java.util.List;

public interface UserProfileRepo {
    UserProfileDto save(UserProfileDto profile);

    List<UserProfileDto> findByIds(List<String> ids);
}
