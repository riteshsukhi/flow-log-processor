# Flow Log Processor

A Java application that processes flow log entries and maps them to tags based on a lookup table. The program analyzes network traffic patterns by counting occurrences of specific port/protocol combinations and their associated tags.

## Features

- Processes flow log entries in a space-efficient manner
- Maps entries to tags based on destination port and protocol
- Case-insensitive protocol matching
- Provides statistics on tag counts and port/protocol combinations
- Thread-safe implementation
- Comprehensive test coverage

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

## Assumptions

1. **Flow Log Format**:
   - Each line contains space-separated fields
   - Fields are in the order: version, account-id, interface-id, srcaddr, dstaddr, srcport, dstport, protocol, packets, bytes, start, end, action, log-status
   - Protocol numbers are mapped as follows:
     - 6 → TCP
     - 17 → UDP

2. **Lookup Table Format**:
   - CSV file with header row: "dstport,protocol,tag"
   - Each subsequent row contains: port number, protocol name, and tag
   - Protocol names are case-insensitive
   - No duplicate port/protocol combinations

3. **Memory Efficiency**:
   - Program processes one line at a time
   - Uses efficient data structures for lookups and counting
   - Maintains minimal state for statistics

4. **Error Handling**:
   - Invalid flow log entries are skipped
   - Invalid lookup table format raises an exception
   - File I/O errors are properly handled

## Building the Project

```bash
mvn clean package
```

This will create two JAR files in the `target` directory:
- `flow-log-processor-1.0-SNAPSHOT.jar`: Contains only the project classes
- `flow-log-processor-1.0-SNAPSHOT-jar-with-dependencies.jar`: Contains all dependencies

## Running the Program

```bash
java -jar target/flow-log-processor-1.0-SNAPSHOT-jar-with-dependencies.jar <flow_log_file> <lookup_table_file>
```

Example:
```bash
java -jar target/flow-log-processor-1.0-SNAPSHOT-jar-with-dependencies.jar flow.log lookup.csv
```

## Test Coverage

The project includes comprehensive test cases:

### LookupEntry Tests
- Equality comparison
- Hash code consistency
- Case-insensitive protocol matching
- Getter methods

### FlowLogProcessor Tests
- Basic flow log processing
- Case-insensitive protocol handling
- Untagged entries handling
- Invalid flow log format handling
- Invalid lookup table format handling

To run the tests:
```bash
mvn test
```

## Performance Analysis

1. **Time Complexity**:
   - Flow log processing: O(n) where n is the number of lines
   - Lookup operations: O(1) using HashMap
   - Statistics generation: O(m log m) where m is the number of unique entries

2. **Space Complexity**:
   - Lookup table: O(k) where k is the number of lookup entries
   - Statistics: O(m) where m is the number of unique port/protocol combinations

3. **Memory Usage**:
   - Processes one line at a time
   - Maintains minimal state for counting
   - Uses efficient data structures for lookups

## Design Decisions

1. **Immutable Data Classes**:
   - `LookupEntry` is immutable for thread safety
   - Prevents accidental modifications during processing

2. **Thread Safety**:
   - Uses thread-safe collections
   - Returns unmodifiable views of statistics

3. **Error Handling**:
   - Graceful handling of invalid input
   - Clear error messages for configuration issues

4. **Code Organization**:
   - Clear separation of concerns
   - Modular design for easy maintenance
   - Well-documented public API

## Future Improvements

1. **Performance Optimizations**:
   - Parallel processing for large files
   - Batch processing for better throughput
   - Memory-mapped file support

2. **Feature Additions**:
   - Support for more protocol types
   - Custom protocol mappings
   - Additional statistics and analytics
   - Export results in various formats

3. **Monitoring and Logging**:
   - Add detailed logging
   - Performance metrics collection
   - Progress reporting for large files 