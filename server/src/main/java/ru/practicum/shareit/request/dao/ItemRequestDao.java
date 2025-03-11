package ru.practicum.shareit.request.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Collection;

public interface ItemRequestDao extends JpaRepository<ItemRequest, Long> {

    @Query("SELECT r FROM ItemRequest r WHERE r.requestor.id = :ownerId ORDER BY r.created DESC")
    Collection<ItemRequest> findAllOwnerRequest(Long ownerId);

    @Query("SELECT r FROM ItemRequest r WHERE r.requestor.id  != :userId ORDER BY r.created DESC")
    Collection<ItemRequest> findAllByNotRequesterId(Long userId);

}
