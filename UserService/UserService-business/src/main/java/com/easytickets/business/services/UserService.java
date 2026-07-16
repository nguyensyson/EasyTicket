package com.easytickets.business.services;

import com.easytickets.business.dto.AccountStatus;
import com.easytickets.business.dto.LoginRequest;
import com.easytickets.business.dto.LoginResponse;
import com.easytickets.business.dto.OrganizerHistoryDto;
import com.easytickets.business.dto.RegisterRequest;
import com.easytickets.business.dto.RegisterResponse;
import com.easytickets.business.dto.TicketHistoryDto;
import com.easytickets.business.dto.UserRole;
import com.easytickets.business.dto.UserSummaryDto;

import java.util.List;

public interface UserService {
    RegisterResponse register(RegisterRequest request, UserRole role);

    LoginResponse login(LoginRequest request);

    /**
     * Buyer's ticket purchase history, aggregated from Order Service.
     */
    List<TicketHistoryDto> getTicketHistory();

    /**
     * Organizer's event/revenue statistics, aggregated from Event Service.
     */
    OrganizerHistoryDto getOrganizerHistory();

    /**
     * Admin: list accounts, optionally filtered by role and/or status.
     */
    List<UserSummaryDto> getUsers(UserRole role, AccountStatus status);

    /**
     * Admin: lock/unlock an account, synced to Keycloak's enabled flag.
     */
    void updateUserStatus(String userId, AccountStatus status);
}
