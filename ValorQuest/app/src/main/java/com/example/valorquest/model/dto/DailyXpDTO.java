package com.example.valorquest.model.dto;

import java.time.LocalDate;
public class DailyXpDTO {
    private LocalDate date;
    private int xp;

    public DailyXpDTO(LocalDate date, int xp) {
        this.date = date;
        this.xp = xp;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getXp() {
        return xp;
    }
}