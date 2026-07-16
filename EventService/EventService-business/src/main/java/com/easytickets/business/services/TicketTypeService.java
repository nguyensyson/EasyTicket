package com.easytickets.business.services;

import com.easytickets.business.dto.CreateTicketTypeRequest;
import com.easytickets.business.dto.TicketTypeDto;
import com.easytickets.business.dto.UpdateTicketTypeRequest;

import java.util.List;

public interface TicketTypeService {

    TicketTypeDto createTicketType(String eventId, CreateTicketTypeRequest request, String organizerId);

    TicketTypeDto updateTicketType(String eventId, String ticketTypeId, UpdateTicketTypeRequest request, String organizerId);

    List<TicketTypeDto> listTicketTypes(String eventId);
}
