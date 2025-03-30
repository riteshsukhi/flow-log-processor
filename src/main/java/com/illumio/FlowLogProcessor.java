package com.illumio;

import com.illumio.model.LookupEntry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class FlowLogProcessor {
    private static final Map<String, String> PROTOCOL_MAP = Map.of(
            "6", "tcp",
            "17", "udp"
    );

    private final Map<LookupEntry, String> lookupTable;
    private final Map<String, Integer> tagCounts;
    private final Map<LookupEntry, Integer> portProtocolCounts;

    public FlowLogProcessor(String lookupFile) throws IOException {
        this.lookupTable = loadLookupTable(lookupFile);
        this.tagCounts = new HashMap<>();
        this.portProtocolCounts = new HashMap<>();
    }

    private Map<LookupEntry, String> loadLookupTable(String lookupFile) throws IOException {
        Map<LookupEntry, String> lookup = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(lookupFile))) {
            String header = reader.readLine();
            if (header == null || !header.equals("dstport,protocol,tag")) {
                throw new IllegalArgumentException("Invalid lookup table format");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 3) continue;
                
                LookupEntry entry = new LookupEntry(parts[0], parts[1], parts[2]);
                lookup.put(entry, parts[2]);
            }
        }
        return lookup;
    }

    public void processFlowLog(String flowFile) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(flowFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line);
            }
        }
    }

    private void processLine(String line) {
        String[] fields = line.strip().split("\\s+");
        if (fields.length < 8) return;

        String dstPort = fields[6];
        String protocolNum = fields[7];
        String protocol = PROTOCOL_MAP.get(protocolNum);
        
        if (protocol == null) return;

        LookupEntry entry = new LookupEntry(dstPort, protocol, null);
        portProtocolCounts.merge(entry, 1, Integer::sum);

        String tag = lookupTable.getOrDefault(entry, "Untagged");
        tagCounts.merge(tag, 1, Integer::sum);
    }

    public void printResults() {
        // Print tag counts
        System.out.println("\nTag Counts:");
        System.out.println("Tag             Count");
        System.out.println("-".repeat(20));
        
        tagCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> System.out.printf("%-15s %d%n", entry.getKey(), entry.getValue()));

        // Print port/protocol counts
        System.out.println("\nPort/Protocol Combination Counts:");
        System.out.println("Port    Protocol        Count");
        System.out.println("-".repeat(30));
        
        portProtocolCounts.entrySet().stream()
                .sorted((a, b) -> {
                    int portCompare = a.getKey().getDstPort().compareTo(b.getKey().getDstPort());
                    return portCompare != 0 ? portCompare : 
                           a.getKey().getProtocol().compareTo(b.getKey().getProtocol());
                })
                .forEach(entry -> System.out.printf("%-8s %-15s %d%n",
                        entry.getKey().getDstPort(),
                        entry.getKey().getProtocol(),
                        entry.getValue()));
    }

    // Getter methods for testing
    public Map<String, Integer> getTagCounts() {
        return Collections.unmodifiableMap(tagCounts);
    }

    public Map<LookupEntry, Integer> getPortProtocolCounts() {
        return Collections.unmodifiableMap(portProtocolCounts);
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java FlowLogProcessor <flow_log_file> <lookup_table_file>");
            System.exit(1);
        }

        try {
            FlowLogProcessor processor = new FlowLogProcessor(args[1]);
            processor.processFlowLog(args[0]);
            processor.printResults();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
} 