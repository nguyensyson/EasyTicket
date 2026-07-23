package com.easytickets.infratructures.shared;

import com.easytickets.business.dto.CategoryDto;
import com.easytickets.business.repo.CategoryRepo;
import com.easytickets.infratructures.mapper.CategoryMapper;
import com.easytickets.infratructures.repo.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepo {

    private final CategoryRepository jpaRepository;
    private final CategoryMapper mapper;

    @Override
    public List<CategoryDto> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public Optional<CategoryDto> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDto);
    }
}
