package com.easytickets.infratructures.shared;

import com.easytickets.business.dto.OrderDto;
import com.easytickets.business.repo.OrderRepo;
import com.easytickets.infratructures.mapper.OrderMapper;
import com.easytickets.infratructures.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepo {

    private final OrderRepository jpaRepository;
    private final OrderMapper mapper;

    @Override
    public OrderDto save(OrderDto order) {
        return mapper.toDto(jpaRepository.save(mapper.toEntity(order)));
    }

    @Override
    public Optional<OrderDto> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDto);
    }

    @Override
    public Optional<OrderDto> findByReservationId(String reservationId) {
        return jpaRepository.findByReservationId(reservationId).map(mapper::toDto);
    }
}
