package com.easytickets.infratructures.repo;

import java.math.BigDecimal;

public interface EventOrderStatsProjection {
    String getEventId();

    Long getTicketsSold();

    BigDecimal getRevenue();
}
