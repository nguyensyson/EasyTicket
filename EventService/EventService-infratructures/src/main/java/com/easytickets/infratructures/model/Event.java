package com.easytickets.infratructures.model;

import com.easytickets.business.dto.EventCategory;
import com.easytickets.business.dto.EventStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@SQLRestriction("delete_flag = 'ACTIVE'")
@Data
@EqualsAndHashCode(callSuper = true)
@EntityListeners(AuditingEntityListener.class)
public class Event extends BaseEntity {

    @Column(name = "organizer_id", length = 255, nullable = false)
    private String organizerId;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50, nullable = false)
    private EventCategory category;

    @Column(name = "location_id", length = 36, nullable = false)
    private String locationId;

    @Column(name = "location", length = 255, nullable = false)
    private String location;

    @Column(name = "banner_url", length = 255)
    private String bannerUrl;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventStatus status;
}
