package com.wamumu.orderservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class InventoryServiceClient {

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    public ResponseEntity<Void> updateInventory(final Long eventId, final Long ticketAmmount){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.put(inventoryServiceUrl + "/event/" + eventId + "/capacity/" + ticketAmmount,null);
        return ResponseEntity.ok().build();
    }
}
