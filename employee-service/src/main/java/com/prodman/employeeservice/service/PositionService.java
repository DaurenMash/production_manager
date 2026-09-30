package com.prodman.employeeservice.service;

import com.prodman.employeeservice.dto.request.CreatePositionRequest;
import com.prodman.employeeservice.dto.request.UpdatePositionRequest;
import com.prodman.employeeservice.dto.response.PositionResponse;
import com.prodman.employeeservice.model.Position;
import com.prodman.employeeservice.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;

    @Transactional
    public PositionResponse createPosition(CreatePositionRequest request) {
        if (positionRepository.existsByName(request.getName())) {
            throw new RuntimeException("Position with name '" + request.getName() + "' already exists");
        }

        Position position = Position.builder()
            .name(request.getName())
            .color(request.getColor() != null ? request.getColor() : "#3498db")
            .build();

        return toResponse(positionRepository.save(position));
    }

    @Transactional
    public PositionResponse updatePosition(String id, UpdatePositionRequest request) {
        Position position = positionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Position not found: " + id));

        if (request.getName() != null && !request.getName().equals(position.getName())) {
            if (positionRepository.existsByName(request.getName())) {
                throw new RuntimeException("Position with name '" + request.getName() + "' already exists");
            }
            position.setName(request.getName());
        }

        if (request.getColor() != null) {
            position.setColor(request.getColor());
        }

        return toResponse(positionRepository.save(position));
    }

    @Transactional
    public void deletePosition(String id) {
        if (!positionRepository.existsById(id)) {
            throw new RuntimeException("Position not found: " + id);
        }
        positionRepository.deleteById(id);
    }

    public List<PositionResponse> getAllPositions() {
        return positionRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public PositionResponse getPositionById(String id) {
        Position position = positionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Position not found: " + id));
        return toResponse(position);
    }

    private PositionResponse toResponse(Position position) {
        return PositionResponse.builder()
            .id(position.getId())
            .name(position.getName())
            .color(position.getColor())
            .createdAt(position.getCreatedAt())
            .updatedAt(position.getUpdatedAt())
            .build();
    }
}