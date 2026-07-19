package com.algonest.AlgoNest_Backend.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CalendarInfoDTO {
    private int totalActiveDays;
    private List<Map<String, String>> badges;
}