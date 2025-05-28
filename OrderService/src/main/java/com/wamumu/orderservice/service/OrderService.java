package com.wamumu.orderservice.service;

import com.wamumu.bookingservice.event.BookingEvent;
import com.wamumu.orderservice.client.InventoryServiceClient;
import com.wamumu.orderservice.entity.Order;
import com.wamumu.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryServiceClient inventoryServiceClient;

    @Autowired
    public OrderService(final OrderRepository orderRepository,
                        final InventoryServiceClient inventoryServiceClient) {
        this.orderRepository = orderRepository;
        this.inventoryServiceClient=inventoryServiceClient;
    }

    @KafkaListener(topics = "booking", groupId = "order-service")
    public void orderEvent(BookingEvent bookingEvent){
        log.info("Received order event: {}", bookingEvent);

        //create in db
        Order order = createOrder(bookingEvent);
        orderRepository.saveAndFlush(order);
        //update inventory
        inventoryServiceClient.updateInventory(order.getEventId(),order.getTicketAmmount());
        log.info("Updated order event: {}, less [{}] tickets", order.getEventId(), order.getTicketAmmount());
    }

    private Order createOrder(BookingEvent bookingEvent){
        return Order.builder()
                .customerId(bookingEvent.getUserId())
                .eventId(bookingEvent.getEventId())
                .ticketAmmount(bookingEvent.getTicketAmmount())
                .totalPrice(bookingEvent.getTotalPrice())
                .build();
    }

}
