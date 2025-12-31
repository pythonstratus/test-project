# Parallel Query Executor

A Java application for testing parallel Oracle database query execution.

## Requirements

- JDK 17+
- Maven 3.6+
- Oracle Database access

## Project Structure

```
parallel-query-app/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── App.java                    # Main entry point
│   │   │   ├── config/
│   │   │   │   └── DatabaseConfig.java     # HikariCP connection pool config
│   │   │   ├── executor/
│   │   │   │   └── ParallelQueryExecutor.java  # Parallel execution logic
│   │   │   └── model/
│   │   │       └── ResultData.java         # Data model
│   │   └── resources/
│   │       └── application.properties      # Configuration
│   └── test/java/com/example/
│       └── ParallelQueryExecutorTest.java
└── README.md
```

## Quick Start

### 1. Configure Database Connection

Edit `src/main/resources/application.properties`:

```properties
db.url=jdbc:oracle:thin:@//your-host:1521/YOUR_SERVICE
db.username=your_username
db.password=your_password
```

### 2. Modify the SQL Query

Edit `ParallelQueryExecutor.java` and update the SQL query in `executeForGrade()` method:

```java
String sql = """
    SELECT id, grade, name, description, created_date 
    FROM your_actual_table a 
    WHERE a.grade = ?
    """;
```

### 3. Update ResultData Model

Modify `ResultData.java` to match your table columns, and update the `mapResultData()` method in `ParallelQueryExecutor.java`.

### 4. Build and Run

```bash
# Navigate to project directory
cd parallel-query-app

# Compile
mvn clean compile

# Run the application
mvn exec:java

# Or run tests
mvn test

# Create executable JAR
mvn clean package

# Run the JAR
java -jar target/parallel-query-executor-1.0-SNAPSHOT.jar
```

## Configuration Options

| Property | Description | Default |
|----------|-------------|---------|
| `db.url` | Oracle JDBC URL | `jdbc:oracle:thin:@//localhost:1521/ORCL` |
| `db.username` | Database username | - |
| `db.password` | Database password | - |
| `db.pool.size` | Connection pool size | 10 |
| `executor.thread-pool-size` | Parallel threads | 4 |
| `executor.fetch-size` | JDBC fetch size | 1000 |
| `query.grades` | Grades to process | 4,5,7,11,12,13 |

## Performance Tips

1. **Thread Pool Size**: Set to match your Oracle connection pool limits
2. **Fetch Size**: Larger values reduce round trips but use more memory
3. **Connection Pool**: HikariCP handles connection reuse efficiently

## Troubleshooting

### ORA-12505: TNS listener does not currently know of SID
- Check your service name vs SID in the JDBC URL

### Connection timeout
- Verify network connectivity to Oracle server
- Check firewall rules for port 1521

### Out of memory
- Reduce fetch size
- Process results in batches instead of loading all to memory
