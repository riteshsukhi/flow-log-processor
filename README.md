# Flow Log Processor

Processes VPC flow logs and maps entries to tags based on destination port and protocol combinations.

## Requirements

- Java 11 or higher
- Maven 3.6 or higher

## Building from Source

1. Clone the repository:
```bash
git clone https://github.com/riteshsukhi/flow-log-processor.git
cd flow-log-processor
```

2. Build the project:
```bash
mvn clean package
```

This will create two JAR files in the `target` directory:
- `flow-log-processor-1.0-SNAPSHOT.jar`: Contains only the project classes
- `flow-log-processor-1.0-SNAPSHOT-jar-with-dependencies.jar`: Contains all dependencies

## Running the Program

1. Using sample data:
```bash
java -jar target/flow-log-processor-1.0-SNAPSHOT-jar-with-dependencies.jar sample_flow_logs.txt sample_lookup_table.csv output.txt
```

2. Using your own data:
```bash
java -jar target/flow-log-processor-1.0-SNAPSHOT-jar-with-dependencies.jar <flow_log_file> <lookup_table_file> <output_file>
```

The program will generate an output file containing:
- Count of matches for each tag
- Count of matches for each port/protocol combination

## Input Files

1. Flow Log File:
   - Space-separated values following AWS VPC Flow Log format
   - Required fields: version, account-id, interface-id, srcaddr, dstaddr, srcport, dstport, protocol, packets, bytes, start, end, action, log-status
   - Sample file: `sample_flow_logs.txt`

2. Lookup Table:
   - CSV file with header: dstport,protocol,tag
   - Sample file: `sample_lookup_table.csv`
   - Example:
     ```
     dstport,protocol,tag
     25,tcp,sv_P1
     68,udp,sv_P2
     23,tcp,sv_P1
     31,udp,SV_P3
     443,tcp,sv_P2
     ```

## Output Format

The output file will contain:

```
Tag Counts:
Tag             Count
--------------------
SV_P3            1
Untagged         1
sv_P1            3
sv_P2            3

Port/Protocol Combination Counts:
Port    Protocol        Count
------------------------------
23       tcp             1
25       tcp             1
31       udp             1
53       udp             1
68       udp             1
80       tcp             1
443      tcp             1
8080     tcp             1
```

## Assumptions

- Protocol numbers are mapped as follows:
  - 6 → TCP
  - 17 → UDP
- Protocol matching is case-insensitive
- Invalid entries are skipped silently
- Port numbers must be between 0 and 65535
- One tag can map to multiple port/protocol combinations
- Flow log file size can be up to 10MB
- Lookup table can contain up to 10000 mappings
- Input files are plain text (ASCII) files

## Testing

Run the tests with:
```bash
mvn test
```

The test suite covers:
1. Basic Flow Log Processing:
   - Correct tag assignment
   - Proper counting of matches
   - Output format validation

2. Case Sensitivity:
   - Protocol matching is case-insensitive
   - Tag matching is case-sensitive
   - Port numbers are exact matches

3. Input Validation:
   - Invalid flow log entries are skipped
   - Invalid lookup table entries are skipped
   - Invalid port numbers are handled gracefully

4. Edge Cases:
   - Empty input files
   - Missing fields
   - Malformed data
   - Untagged entries
   - Multiple mappings for same tag

5. Performance:
   - Efficient processing of large files
   - Memory usage optimization
   - Proper resource cleanup

## Analysis

1. Performance Considerations:
   - Uses buffered reading for efficient file processing
   - HashMap for O(1) lookup table access
   - Minimal memory footprint
   - No external dependencies (except JUnit for testing)

2. Error Handling:
   - Graceful handling of invalid inputs
   - Clear error messages
   - No program crashes on bad data
   - Proper resource cleanup

3. Scalability:
   - Handles files up to 10MB
   - Supports up to 10000 mappings
   - Memory usage scales linearly with input size
   - Efficient data structures for lookups

4. Code Quality:
   - Clean, maintainable code
   - Comprehensive test coverage
   - Clear separation of concerns
   - Well-documented code
   - Follows Java best practices 