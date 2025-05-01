package com.wamumu.inventoryservice.repository;

import com.wamumu.inventoryservice.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
