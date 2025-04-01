# Flow Log Processor

A Java program that processes AWS VPC flow logs and maps them to tags based on destination port and protocol combinations.

## Quick Start

1. Clone and build:
```bash
git clone https://github.com/riteshsukhi/flow-log-processor.git
cd flow-log-processor
mvn clean package
```

2. Run with sample data:
```bash
java -jar target/flow-log-processor-1.0-SNAPSHOT-jar-with-dependencies.jar sample_flow_logs.txt sample_lookup_table.csv output.txt
```

## Input Files

### Flow Log File
- Space-separated values in AWS VPC Flow Log format
- Fields: version, account-id, interface-id, srcaddr, dstaddr, srcport, dstport, protocol, packets, bytes, start, end, action, log-status
- See `sample_flow_logs.txt` for example

### Lookup Table
- CSV file with header: dstport,protocol,tag
- Example from `sample_lookup_table.csv`:
```
dstport,protocol,tag
25,tcp,sv_P1
68,udp,sv_P2
23,tcp,sv_P1
31,udp,SV_P3
443,tcp,sv_P2
```

## Output

The program generates an output file with tag counts and port/protocol combinations:

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

## Key Features

- Case-insensitive protocol matching
- Handles files up to 10MB
- Supports up to 10000 mappings
- Multiple port/protocol combinations per tag
- Protocol mapping: 6 → TCP, 17 → UDP
- Skips invalid entries gracefully
- Port range: 0-65535

## Development

### Requirements
- Java 11+
- Maven 3.6+

### Running Tests
```bash
mvn test
```

### Test Coverage
- Basic flow log processing
- Case sensitivity handling
- Input validation
- Edge cases
- Performance with large files

### Implementation Notes
- Uses buffered reading for large files
- HashMap for fast lookups
- Minimal memory usage
- No external dependencies (except JUnit)

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