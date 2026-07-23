package com.easytickets.business.services.impl;

import com.easytickets.business.dto.CategoryDto;
import com.easytickets.business.repo.CategoryRepo;
import com.easytickets.business.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepo categoryRepo;

    @Override
    @Cacheable(cacheNames = "categories")
    public List<CategoryDto> listCategories() {
        return categoryRepo.findAll();
    }
}
