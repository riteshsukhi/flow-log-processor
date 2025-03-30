# Flow Log Tag Mapper

This program processes flow log data and maps each row to a tag based on a lookup table. The lookup table contains mappings between destination port, protocol, and corresponding tags.

## Project Structure
```
.
├── README.md
├── pom.xml
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── illumio/
│                   ├── FlowLogProcessor.java
│                   └── model/
│                       └── LookupEntry.java
├── sample_flow_logs.txt
└── sample_lookup_table.csv
```

## Assumptions

1. Input Files:
   - Flow log file is a plain text file with space-separated values
   - Lookup table is a CSV file with headers: dstport,protocol,tag
   - Flow log file size can be up to 10 MB
   - Lookup table can have up to 10,000 mappings

2. Processing:
   - Tag matching is case-insensitive
   - One tag can map to multiple port/protocol combinations
   - If no matching tag is found, the flow is marked as "Untagged"
   - Port numbers in the lookup table are treated as strings for exact matching
   - Protocol names in the lookup table are treated as strings for exact matching

3. Output:
   - Results are written to stdout
   - Counts are sorted alphabetically for tags and numerically for ports

## Requirements
- Java 11 or higher
- Maven 3.6 or higher

## Building the Project

1. Clone the repository
2. Navigate to the project directory
3. Build the project:
   ```bash
   mvn clean package
   ```

## Usage

1. Prepare your input files:
   - Flow log file (space-separated values)
   - Lookup table (CSV format with headers: dstport,protocol,tag)

2. Run the program:
   ```bash
   java -jar target/flow-log-processor-1.0-SNAPSHOT-jar-with-dependencies.jar <flow_log_file> <lookup_table_file>
   ```

3. The program will output:
   - Tag counts (including Untagged)
   - Port/Protocol combination counts

## Sample Output
```
Tag Counts:
Tag             Count
SV_P3          1
Untagged       2
sv_P1          2
sv_P2          2

Port/Protocol Combination Counts:
Port    Protocol        Count
23      tcp            1
25      tcp            1
31      udp            1
68      udp            1
80      tcp            1
443     tcp            1
```

## Testing
The program has been tested with:
- Sample flow logs up to 10MB
- Lookup tables with various sizes
- Different tag formats and cases
- Missing or invalid data
- Edge cases in port/protocol combinations

## Implementation Details

The Java implementation:
- Uses Java 11 features like `Map.of()` for immutable collections
- Implements proper resource management with try-with-resources
- Uses Java streams for efficient data processing and sorting
- Follows object-oriented principles with proper encapsulation
- Includes comprehensive error handling
- Uses efficient data structures (HashMap) for lookups
- Implements proper equals() and hashCode() for custom objects 