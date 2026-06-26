package com.prodman.gateway.repository;

import com.prodman.gateway.model.LogEntry;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends ReactiveMongoRepository<LogEntry, String> {
}