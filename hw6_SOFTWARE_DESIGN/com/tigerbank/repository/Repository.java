package com.tigerbank.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Repository<T> {
    T save(T entity);

    Optional<T> findById(UUID id);

    List<T> findAll();

    boolean delete(UUID id);
}