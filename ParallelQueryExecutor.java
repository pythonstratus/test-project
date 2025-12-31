package com.example.executor;

import com.example.config.DatabaseConfig;
import com.example.model.ResultData;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Parallel query executor using HikariCP connection pool.
 * Executes queries for different grade values in parallel threads.
 */
public class ParallelQueryExecutor {
    
    private final int threadPoolSize;
    private final int fetchSize;
    private final DataSource dataSource;
    
    public ParallelQueryExecutor() {
        this.threadPoolSize = DatabaseConfig.getIntProperty("executor.thread-pool-size", 4);
        this.fetchSize = DatabaseConfig.getIntProperty("executor.fetch-size", 1000);
        this.dataSource = DatabaseConfig.getDataSource();
    }
    
    /**
     * Execute queries in parallel for all configured grades.
     */
    public List<ResultData> executeParallel() throws Exception {
        int[] grades = DatabaseConfig.getGrades();
        
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        List<Future<QueryResult>> futures = new ArrayList<>();
        
        System.out.println("Starting parallel execution with " + threadPoolSize + " threads...");
        System.out.println("Processing grades: " + java.util.Arrays.toString(grades));
        
        long startTime = System.currentTimeMillis();
        
        for (int grade : grades) {
            futures.add(executor.submit(() -> executeForGrade(grade)));
        }
        
        // Collect results
        List<ResultData> allResults = new ArrayList<>();
        int totalRecords = 0;
        
        for (Future<QueryResult> future : futures) {
            try {
                QueryResult result = future.get(5, TimeUnit.MINUTES);
                allResults.addAll(result.data);
                totalRecords += result.recordCount;
                System.out.printf("  Grade %d: %d records in %d ms%n", 
                    result.grade, result.recordCount, result.executionTimeMs);
            } catch (TimeoutException e) {
                System.err.println("Query timed out: " + e.getMessage());
            } catch (ExecutionException e) {
                System.err.println("Query failed: " + e.getCause().getMessage());
                e.getCause().printStackTrace();
            }
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        System.out.printf("%nTotal: %d records fetched in %d ms%n", totalRecords, totalTime);
        
        executor.shutdown();
        if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
            executor.shutdownNow();
        }
        
        return allResults;
    }
    
    /**
     * Execute query for a specific grade value.
     */
    private QueryResult executeForGrade(int grade) throws SQLException {
        List<ResultData> results = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        // TODO: Replace with your actual SQL query
        String sql = """
            SELECT id, grade, name, description, created_date 
            FROM main_data a 
            WHERE a.grade = ?
            """;
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setFetchSize(fetchSize);
            stmt.setInt(1, grade);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultData(rs));
                }
            }
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        return new QueryResult(grade, results, results.size(), executionTime);
    }
    
    /**
     * Map ResultSet row to ResultData object.
     * TODO: Modify this method based on your actual table columns.
     */
    private ResultData mapResultData(ResultSet rs) throws SQLException {
        ResultData data = new ResultData();
        data.setId(rs.getLong("id"));
        data.setGrade(rs.getInt("grade"));
        data.setName(rs.getString("name"));
        data.setDescription(rs.getString("description"));
        data.setCreatedDate(rs.getTimestamp("created_date"));
        return data;
    }
    
    /**
     * Internal class to hold query result with metadata.
     */
    private static class QueryResult {
        final int grade;
        final List<ResultData> data;
        final int recordCount;
        final long executionTimeMs;
        
        QueryResult(int grade, List<ResultData> data, int recordCount, long executionTimeMs) {
            this.grade = grade;
            this.data = data;
            this.recordCount = recordCount;
            this.executionTimeMs = executionTimeMs;
        }
    }
}
