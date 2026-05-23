package com.cosmoscan.fileanalysis.enums;

/**
 * Enumeration representing the possible statuses of an analysis report.
 * Each status has a corresponding Russian-language display name.
 */
public enum ReportStatus {
    
    /** Work has been accepted and meets all requirements */
    ACCEPTED("Принято"),
    
    /** Work requires revisions or improvements */
    NEEDS_REWORK("Требуется доработка"),
    
    /** An error occurred during the analysis process */
    ERROR("Ошибка анализа"),
    
    /** Work is awaiting analysis processing */
    PENDING("Ожидает анализа");

    private final String russianName;
    
    /**
     * @param russianName human-readable status description in Russian
     */
    ReportStatus(String russianName) { 
        this.russianName = russianName; 
    }
    
    /**
     * @return the Russian-language display name for this status
     */
    public String getRussianName() { 
        return russianName; 
    }
}