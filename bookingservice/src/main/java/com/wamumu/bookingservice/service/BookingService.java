package com.wamumu.bookingservice.service;

import com.wamumu.bookingservice.client.InventoryServiceClient;
import com.wamumu.bookingservice.entity.Customer;
import com.wamumu.bookingservice.repository.CustomerRepository;
import com.wamumu.bookingservice.request.BookingRequest;
import com.wamumu.bookingservice.response.BookingResponse;
import com.wamumu.bookingservice.response.InventoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    private final CustomerRepository customerRepository;
    private final InventoryServiceClient inventoryServiceClient;

    @Autowired
    public BookingService(final CustomerRepository customerRepostory,
                          final InventoryServiceClient inventoryServiceClient) {
        this.customerRepository = customerRepostory;
        this.inventoryServiceClient = inventoryServiceClient;
    }


    public BookingResponse createBooking(final BookingRequest request) {
        //Check costumer is real
        final Customer customer = customerRepository.findById(request.getUserId()).orElse(null);
        if (customer == null) {
            throw new RuntimeException("Customer not found");
        }
        //Check available seats
        final InventoryResponse inventoryResponse = inventoryServiceClient.getInventory(request.getEventId());

        return BookingResponse.builder().build();
    }
}
