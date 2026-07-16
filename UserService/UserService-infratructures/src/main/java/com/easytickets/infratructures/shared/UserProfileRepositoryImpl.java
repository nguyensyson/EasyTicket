package com.easytickets.infratructures.shared;

import com.easytickets.business.dto.UserProfileDto;
import com.easytickets.business.repo.UserProfileRepo;
import com.easytickets.infratructures.mapper.UserProfileMapper;
import com.easytickets.infratructures.model.UserProfile;
import com.easytickets.infratructures.repo.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserProfileRepositoryImpl implements UserProfileRepo {

    private final UserProfileRepository jpaRepository;
    private final UserProfileMapper mapper;

    @Override
    public UserProfileDto save(UserProfileDto dto) {
        UserProfile entity = mapper.toEntity(dto);
        return mapper.toDto(jpaRepository.save(entity));
    }

    @Override
    public List<UserProfileDto> findByIds(List<String> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(mapper::toDto)
                .toList();
    }
}
