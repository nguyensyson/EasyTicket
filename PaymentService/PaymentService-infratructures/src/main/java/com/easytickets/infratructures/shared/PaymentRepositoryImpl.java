package com.easytickets.infratructures.shared;

import com.easytickets.business.dto.PaymentDto;
import com.easytickets.business.dto.PaymentStatus;
import com.easytickets.business.repo.PaymentRepo;
import com.easytickets.infratructures.mapper.PaymentMapper;
import com.easytickets.infratructures.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepo {

    private final PaymentRepository jpaRepository;
    private final PaymentMapper mapper;

    @Override
    public PaymentDto save(PaymentDto payment) {
        return mapper.toDto(jpaRepository.save(mapper.toEntity(payment)));
    }

    @Override
    public Optional<PaymentDto> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDto);
    }

    @Override
    public Optional<PaymentDto> findByOrderId(String orderId) {
        return jpaRepository.findByOrderId(orderId).map(mapper::toDto);
    }

    @Override
    public List<PaymentDto> findPendingExpiredBefore(LocalDateTime cutoff) {
        return jpaRepository.findByStatusAndExpiresAtBefore(PaymentStatus.PENDING, cutoff).stream()
                .map(mapper::toDto)
                .toList();
    }
}
