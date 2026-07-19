package com.algonest.AlgoNest_Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapDayDTO {
    private String date;
    private int count;
}