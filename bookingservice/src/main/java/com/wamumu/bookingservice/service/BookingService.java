package com.wamumu.bookingservice.service;

import com.wamumu.bookingservice.client.InventoryServiceClient;
import com.wamumu.bookingservice.entity.Customer;
import com.wamumu.bookingservice.event.BookingEvent;
import com.wamumu.bookingservice.repository.CustomerRepository;
import com.wamumu.bookingservice.request.BookingRequest;
import com.wamumu.bookingservice.response.BookingResponse;
import com.wamumu.bookingservice.response.InventoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class BookingService {

    private final CustomerRepository customerRepository;
    private final InventoryServiceClient inventoryServiceClient;
    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;

    @Autowired
    public BookingService(final CustomerRepository customerRepostory,
                          final InventoryServiceClient inventoryServiceClient,
                          final KafkaTemplate<String, BookingEvent> kafkaTemplate) {
        this.customerRepository = customerRepostory;
        this.inventoryServiceClient = inventoryServiceClient;
        this.kafkaTemplate = kafkaTemplate;
    }


    public BookingResponse createBooking(final BookingRequest request) {
        //Check costumer is real
        final Customer customer = customerRepository.findById(request.getUserId()).orElse(null);
        if (customer == null) {
            throw new RuntimeException("Customer not found");
        }
        //Check available seats
        final InventoryResponse inventoryResponse = inventoryServiceClient.getInventory(request.getEventId());
        log.info("Inventory Response: {}",inventoryResponse);
        if (inventoryResponse.getCapacity() < request.getTicketAmmount()) {
            throw new RuntimeException("Not enough seats");
        }
        //create booking
        final BookingEvent bookingEvent = createBooking(request, customer, inventoryResponse);
        //send event to kafka
        kafkaTemplate.send("booking", bookingEvent);
        log.info("Booking sent to Kafka: {}",bookingEvent);
        return BookingResponse.builder()
                .userId(bookingEvent.getUserId())
                .eventId(bookingEvent.getEventId())
                .ticketCount(bookingEvent.getTicketAmmount())
                .totalPrice(bookingEvent.getTotalPrice())
                .build();
    }

    private BookingEvent createBooking(final BookingRequest request, final Customer customer, final InventoryResponse response) {
        return BookingEvent.builder()
                .userId(customer.getId())
                .eventId(request.getEventId())
                .ticketAmmount(request.getTicketAmmount())
                .totalPrice(response.getTicketPrice().multiply(BigDecimal.valueOf(request.getTicketAmmount())))
                .build();
    }
}
