package com.easytickets.infratructures.repo;

import com.easytickets.infratructures.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, String> {
}
