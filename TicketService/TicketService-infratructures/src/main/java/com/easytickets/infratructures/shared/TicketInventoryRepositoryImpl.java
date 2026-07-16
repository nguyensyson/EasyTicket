package com.easytickets.infratructures.shared;

import com.easytickets.business.dto.ReservationResult;
import com.easytickets.business.dto.ReservationStatus;
import com.easytickets.business.dto.TicketMetaDto;
import com.easytickets.business.dto.TicketReservationDto;
import com.easytickets.business.dto.TicketTypeDto;
import com.easytickets.business.dto.event.TicketReservedEvent;
import com.easytickets.business.repo.TicketInventoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Redis-backed adapter for {@link TicketInventoryRepo} – Redis is the single source of
 * truth for ticket stock. Reservation/decrement goes through a Lua script (see
 * {@code RedisTicketConfig}) to keep CHECK & DECREMENT atomic under concurrent flash-sale load.
 */
@Repository
@RequiredArgsConstructor
public class TicketInventoryRepositoryImpl implements TicketInventoryRepo {

    private static final String INVENTORY_KEY = "ticket:inventory:%s:%s";
    private static final String META_KEY = "ticket:meta:%s:%s";
    private static final String TICKET_TYPES_KEY = "ticket:event:%s:ticket-types";
    private static final String LOADED_KEY = "ticket:event:%s:loaded";
    private static final String RESERVATION_KEY = "ticket:reservation:%s";

    private static final long NOT_FOUND_CODE = -1L;
    private static final long SOLD_OUT_CODE = -2L;

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> checkAndDecrementScript;

    @Override
    public ReservationResult reserve(String eventId, String ticketTypeId, int quantity) {
        Long outcome = redisTemplate.execute(checkAndDecrementScript,
                List.of(inventoryKey(eventId, ticketTypeId)), String.valueOf(quantity));

        if (outcome == null || outcome == NOT_FOUND_CODE) {
            return ReservationResult.builder().status(ReservationStatus.NOT_FOUND).remaining(0).build();
        }
        if (outcome == SOLD_OUT_CODE) {
            return ReservationResult.builder().status(ReservationStatus.SOLD_OUT).remaining(0).build();
        }
        return ReservationResult.builder().status(ReservationStatus.RESERVED).remaining(outcome.intValue()).build();
    }

    @Override
    public void release(String eventId, String ticketTypeId, int quantity) {
        redisTemplate.opsForValue().increment(inventoryKey(eventId, ticketTypeId), quantity);
    }

    @Override
    public Optional<TicketMetaDto> getMeta(String eventId, String ticketTypeId) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(metaKey(eventId, ticketTypeId));
        if (entries.isEmpty()) {
            return Optional.empty();
        }
        Object name = entries.get("name");
        Object price = entries.get("price");
        return Optional.of(TicketMetaDto.builder()
                .name(name != null ? name.toString() : null)
                .price(price != null ? new BigDecimal(price.toString()) : null)
                .build());
    }

    @Override
    public List<String> getTicketTypeIds(String eventId) {
        Set<String> members = redisTemplate.opsForSet().members(ticketTypesKey(eventId));
        return members != null ? new ArrayList<>(members) : List.of();
    }

    @Override
    public Optional<Integer> getAvailableQuantity(String eventId, String ticketTypeId) {
        String value = redisTemplate.opsForValue().get(inventoryKey(eventId, ticketTypeId));
        return value != null ? Optional.of(Integer.valueOf(value)) : Optional.empty();
    }

    @Override
    public boolean isInventoryLoaded(String eventId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(loadedKey(eventId)));
    }

    @Override
    public void loadInventory(String eventId, List<TicketTypeDto> ticketTypes) {
        for (TicketTypeDto ticketType : ticketTypes) {
            redisTemplate.opsForValue().setIfAbsent(
                    inventoryKey(eventId, ticketType.getId()), String.valueOf(ticketType.getTotalQuantity()));

            Map<String, String> meta = new HashMap<>();
            meta.put("name", ticketType.getName());
            meta.put("price", ticketType.getPrice().toPlainString());
            redisTemplate.opsForHash().putAll(metaKey(eventId, ticketType.getId()), meta);

            redisTemplate.opsForSet().add(ticketTypesKey(eventId), ticketType.getId());
        }
        redisTemplate.opsForValue().set(loadedKey(eventId), "true");
    }

    @Override
    public void saveReservation(TicketReservedEvent event, long ttlSeconds) {
        Map<String, String> fields = new HashMap<>();
        fields.put("userId", event.getUserId());
        fields.put("eventId", event.getEventId());
        fields.put("ticketTypeId", event.getTicketTypeId());
        fields.put("quantity", String.valueOf(event.getQuantity()));
        fields.put("unitPrice", event.getUnitPrice().toPlainString());
        fields.put("reservedAt", Instant.now().toString());

        String key = reservationKey(event.getReservationId());
        redisTemplate.opsForHash().putAll(key, fields);
        redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public Optional<TicketReservationDto> getReservation(String reservationId) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(reservationKey(reservationId));
        if (entries.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(TicketReservationDto.builder()
                .userId((String) entries.get("userId"))
                .eventId((String) entries.get("eventId"))
                .ticketTypeId((String) entries.get("ticketTypeId"))
                .quantity(Integer.parseInt((String) entries.get("quantity")))
                .unitPrice(new BigDecimal((String) entries.get("unitPrice")))
                .build());
    }

    @Override
    public void deleteReservation(String reservationId) {
        redisTemplate.delete(reservationKey(reservationId));
    }

    private String inventoryKey(String eventId, String ticketTypeId) {
        return INVENTORY_KEY.formatted(eventId, ticketTypeId);
    }

    private String metaKey(String eventId, String ticketTypeId) {
        return META_KEY.formatted(eventId, ticketTypeId);
    }

    private String ticketTypesKey(String eventId) {
        return TICKET_TYPES_KEY.formatted(eventId);
    }

    private String loadedKey(String eventId) {
        return LOADED_KEY.formatted(eventId);
    }

    private String reservationKey(String reservationId) {
        return RESERVATION_KEY.formatted(reservationId);
    }
}
