package com.cosmoscan.fileanalysis.enums;

public enum ReportStatus {
    ACCEPTED("Принято"),
    NEEDS_REWORK("Требуется доработка"),
    ERROR("Ошибка анализа"),
    PENDING("Ожидает анализа");

    private final String russianName;
    ReportStatus(String russianName) { this.russianName = russianName; }
    public String getRussianName() { return russianName; }
}
