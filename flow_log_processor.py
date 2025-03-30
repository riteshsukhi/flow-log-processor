#!/usr/bin/env python3

import sys
import csv
from collections import defaultdict
from typing import Dict, Tuple, List

def load_lookup_table(lookup_file: str) -> Dict[Tuple[str, str], str]:
    """
    Load the lookup table from CSV file and create a mapping of (port, protocol) to tag.
    """
    lookup = {}
    with open(lookup_file, 'r') as f:
        reader = csv.DictReader(f)
        for row in reader:
            port = row['dstport']
            protocol = row['protocol'].lower()  # Convert to lowercase for case-insensitive matching
            tag = row['tag']
            lookup[(port, protocol)] = tag
    return lookup

def process_flow_log(flow_file: str, lookup: Dict[Tuple[str, str], str]) -> Tuple[Dict[str, int], Dict[Tuple[str, str], int]]:
    """
    Process the flow log file and count tags and port/protocol combinations.
    """
    tag_counts = defaultdict(int)
    port_protocol_counts = defaultdict(int)
    
    # Protocol mapping from number to name
    protocol_map = {
        '6': 'tcp',
        '17': 'udp'
    }
    
    with open(flow_file, 'r') as f:
        for line in f:
            fields = line.strip().split()
            if len(fields) < 8:  # Skip invalid lines
                continue
                
            # Extract relevant fields (based on AWS VPC flow log format)
            dst_port = fields[6]
            protocol_num = fields[7]
            
            if protocol_num not in protocol_map:
                continue
                
            protocol = protocol_map[protocol_num]
            port_protocol = (dst_port, protocol)
            port_protocol_counts[port_protocol] += 1
            
            # Look up tag
            tag = lookup.get(port_protocol, "Untagged")
            tag_counts[tag] += 1
    
    return tag_counts, port_protocol_counts

def print_results(tag_counts: Dict[str, int], port_protocol_counts: Dict[Tuple[str, str], int]):
    """
    Print the results in the required format.
    """
    # Print tag counts
    print("\nTag Counts:")
    print("Tag             Count")
    print("-" * 20)
    for tag in sorted(tag_counts.keys()):
        print(f"{tag:<15} {tag_counts[tag]}")
    
    # Print port/protocol counts
    print("\nPort/Protocol Combination Counts:")
    print("Port    Protocol        Count")
    print("-" * 30)
    for (port, protocol), count in sorted(port_protocol_counts.items()):
        print(f"{port:<8} {protocol:<15} {count}")

def main():
    if len(sys.argv) != 3:
        print("Usage: python flow_log_processor.py <flow_log_file> <lookup_table_file>")
        sys.exit(1)
    
    flow_file = sys.argv[1]
    lookup_file = sys.argv[2]
    
    try:
        lookup = load_lookup_table(lookup_file)
        tag_counts, port_protocol_counts = process_flow_log(flow_file, lookup)
        print_results(tag_counts, port_protocol_counts)
    except Exception as e:
        print(f"Error: {str(e)}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main() 