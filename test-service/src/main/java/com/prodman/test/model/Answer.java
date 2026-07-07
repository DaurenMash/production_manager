package com.prodman.test.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "question_id", nullable = false)
    private String questionId;

    @ElementCollection
    @CollectionTable(name = "answer_selected_options", joinColumns = @JoinColumn(name = "answer_id"))
    @Column(name = "selected_index")
    @Builder.Default
    private List<Integer> selectedOptions = new ArrayList<>();
}