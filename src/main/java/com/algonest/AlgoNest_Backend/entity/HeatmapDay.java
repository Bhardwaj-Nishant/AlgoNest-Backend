package com.algonest.AlgoNest_Backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapDay {
    private String date;   // Format: "YYYY-MM-DD"
    private int count;     // Number of submissions on that day
}