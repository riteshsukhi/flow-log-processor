package com.illumio;

import com.illumio.model.LookupEntry;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FlowLogProcessor {
    // TCP = 6, UDP = 17 in VPC Flow Logs
    private static final Map<String, String> PROTOCOL_MAP = Map.of(
        "6", "tcp",
        "17", "udp"
    );
    
    private final Map<LookupEntry, String> lookupTable;
    private final Map<String, Integer> tagCounts;
    private final Map<LookupEntry, Integer> portProtocolCounts;

    public FlowLogProcessor(String lookupFile) throws IOException {
        if (!Files.exists(Paths.get(lookupFile))) {
            throw new FileNotFoundException(lookupFile);
        }
        this.lookupTable = loadLookupTable(lookupFile);
        this.tagCounts = new HashMap<>();
        this.portProtocolCounts = new HashMap<>();
    }

    private Map<LookupEntry, String> loadLookupTable(String lookupFile) throws IOException {
        Map<LookupEntry, String> lookup = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(lookupFile))) {
            String header = reader.readLine();
            if (!header.equals("dstport,protocol,tag")) {
                throw new IllegalArgumentException("Invalid lookup table format");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length != 3) {
                    System.err.println("Warning: Skipping invalid line: " + line);
                    continue;
                }
                
                try {
                    int port = Integer.parseInt(parts[0].trim());
                    if (port >= 0 && port <= 65535) {
                        LookupEntry entry = new LookupEntry(
                            parts[0].trim(), 
                            parts[1].trim(), 
                            parts[2].trim()
                        );
                        lookup.put(entry, parts[2].trim());
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid port numbers
                }
            }
        }
        return lookup;
    }

    public void processFlowLog(String flowFile, String outputFile) throws IOException {
        if (!Files.exists(Paths.get(flowFile))) {
            throw new FileNotFoundException(flowFile);
        }

        // Validate output file path
        Path outputPath = Paths.get(outputFile);
        if (outputPath.getParent() != null && !Files.exists(outputPath.getParent())) {
            Files.createDirectories(outputPath.getParent());
        }
        if (Files.exists(outputPath) && !Files.isWritable(outputPath)) {
            throw new IOException("Output file exists and is not writable: " + outputFile);
        }

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(flowFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    processLine(line.trim());
                } catch (Exception e) {
                    // Skip invalid lines
                }
            }
        }

        // Write results to output file
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(outputFile)))) {
            // Write tag counts
            writer.println("\nTag Counts:\n");
            writer.println("Tag             Count");
            writer.println("--------------------");
            
            tagCounts.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> writer.printf("%-16s %d%n", entry.getKey(), entry.getValue()));

            // Write port/protocol combination counts
            writer.println("\nPort/Protocol Combination Counts:\n");
            writer.println("Port    Protocol        Count");
            writer.println("------------------------------");
            portProtocolCounts.entrySet().stream()
                .sorted((a, b) -> {
                    int portCompare = Integer.compare(
                        Integer.parseInt(a.getKey().getDstPort()), 
                        Integer.parseInt(b.getKey().getDstPort())
                    );
                    return portCompare != 0 ? portCompare : 
                           a.getKey().getProtocol().compareTo(b.getKey().getProtocol());
                })
                .forEach(entry -> {
                    writer.printf("%-8s %-15s %d%n", 
                        entry.getKey().getDstPort(), 
                        entry.getKey().getProtocol(), 
                        entry.getValue());
                });
        }
    }

    private void processLine(String line) {
        String[] fields = line.split("\\s+");
        // VPC Flow Log format needs at least these fields
        if (fields.length < 8) return;

        String dstPort = fields[6];
        String protocolNum = fields[7];
        
        try {
            int port = Integer.parseInt(dstPort);
            if (port < 0 || port > 65535) return;
            
            String protocol = PROTOCOL_MAP.get(protocolNum);
            if (protocol == null) return;

            LookupEntry entry = new LookupEntry(dstPort, protocol, null);
            portProtocolCounts.merge(entry, 1, Integer::sum);

            String tag = lookupTable.getOrDefault(entry, "Untagged");
            tagCounts.merge(tag, 1, Integer::sum);
        } catch (NumberFormatException e) {
            // Skip invalid port numbers
        }
    }

    // Getter methods for testing
    public Map<String, Integer> getTagCounts() {
        return Collections.unmodifiableMap(tagCounts);
    }

    public Map<LookupEntry, Integer> getPortProtocolCounts() {
        return Collections.unmodifiableMap(portProtocolCounts);
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java FlowLogProcessor <flow_log_file> <lookup_table_file> <output_file>");
            System.exit(1);
        }

        try {
            FlowLogProcessor processor = new FlowLogProcessor(args[1]);
            processor.processFlowLog(args[0], args[2]);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
} 