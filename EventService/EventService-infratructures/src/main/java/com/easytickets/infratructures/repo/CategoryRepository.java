package com.easytickets.infratructures.repo;

import com.easytickets.infratructures.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, String> {
}
