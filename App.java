package com.example;

import com.example.config.DatabaseConfig;
import com.example.executor.ParallelQueryExecutor;
import com.example.model.ResultData;

import java.util.List;

/**
 * Main application entry point.
 * Run this class to test the parallel query executor.
 */
public class App {
    
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  Parallel Query Executor - Test Runner");
        System.out.println("===========================================\n");
        
        // Print configuration
        printConfiguration();
        
        try {
            // Test database connection first
            testConnection();
            
            // Run parallel queries
            ParallelQueryExecutor executor = new ParallelQueryExecutor();
            List<ResultData> results = executor.executeParallel();
            
            // Print sample results
            printSampleResults(results);
            
        } catch (Exception e) {
            System.err.println("\n*** ERROR ***");
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up connection pool
            DatabaseConfig.shutdown();
            System.out.println("\nConnection pool closed. Application finished.");
        }
    }
    
    private static void printConfiguration() {
        System.out.println("Configuration:");
        System.out.println("  DB URL: " + DatabaseConfig.getProperty("db.url", "not set"));
        System.out.println("  DB User: " + DatabaseConfig.getProperty("db.username", "not set"));
        System.out.println("  Thread Pool: " + DatabaseConfig.getIntProperty("executor.thread-pool-size", 4));
        System.out.println("  Fetch Size: " + DatabaseConfig.getIntProperty("executor.fetch-size", 1000));
        System.out.println("  Grades: " + DatabaseConfig.getProperty("query.grades", "4,5,7,11,12,13"));
        System.out.println();
    }
    
    private static void testConnection() throws Exception {
        System.out.println("Testing database connection...");
        try (var conn = DatabaseConfig.getDataSource().getConnection()) {
            var meta = conn.getMetaData();
            System.out.println("  Connected to: " + meta.getDatabaseProductName() + 
                             " " + meta.getDatabaseProductVersion());
            System.out.println("  Driver: " + meta.getDriverName() + 
                             " " + meta.getDriverVersion());
        }
        System.out.println("  Connection test: SUCCESS\n");
    }
    
    private static void printSampleResults(List<ResultData> results) {
        System.out.println("\n--- Sample Results (first 10) ---");
        int count = 0;
        for (ResultData result : results) {
            if (count++ >= 10) break;
            System.out.println("  " + result);
        }
        
        if (results.size() > 10) {
            System.out.println("  ... and " + (results.size() - 10) + " more records");
        }
    }
}
