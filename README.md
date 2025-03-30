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
java -jar target/flow-log-processor-1.0-SNAPSHOT-jar-with-dependencies.jar sample_flow_logs.txt sample_lookup_table.csv
```

2. Using your own data:
```bash
java -jar target/flow-log-processor-1.0-SNAPSHOT-jar-with-dependencies.jar <flow_log_file> <lookup_table_file>
```

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

## Assumptions

- Protocol numbers are mapped as follows:
  - 6 → TCP
  - 17 → UDP
- Protocol matching is case-insensitive
- Invalid entries are skipped silently
- Port numbers must be between 0 and 65535
- One tag can map to multiple port/protocol combinations

## Output Format

```
Tag Counts:
Tag             Count
--------------------
SV_P3           1
Untagged        2
sv_P1           2
sv_P2           2

Port/Protocol Combination Counts:
Port    Protocol        Count
------------------------------
23       tcp             1
25       tcp             1
31       udp             1
443      tcp             1
68       udp             1
80       tcp             1
```

## Testing

Run the tests with:
```bash
mvn test
```

The test suite covers:
- Basic flow log processing
- Case-insensitive protocol matching
- Invalid input handling
- Edge cases 