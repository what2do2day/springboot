package com.couple.question_answer.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorChange {

    @Field("dimension")
    private String dimension;

    @Field("change")
    private Double change;
}