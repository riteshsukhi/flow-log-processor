package com.illumio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class FlowLogProcessorTest {

    @TempDir
    Path tempDir;

    @Test
    void testBasicFlowLogProcessing() throws IOException {
        // Create test lookup table
        Path lookupFile = tempDir.resolve("lookup.csv");
        Files.writeString(lookupFile, "dstport,protocol,tag\n" +
                "80,tcp,web\n" +
                "443,tcp,secure\n" +
                "53,udp,dns");

        // Create test flow log
        Path flowFile = tempDir.resolve("flow.log");
        Files.writeString(flowFile, "2 1234567890 eni-abc123 10.0.0.1 10.0.0.2 12345 80 6 100 1000 1234567890 1234567890 ACCEPT OK\n" +
                "2 1234567890 eni-abc123 10.0.0.2 10.0.0.1 80 443 6 100 1000 1234567890 1234567890 ACCEPT OK\n" +
                "2 1234567890 eni-abc123 10.0.0.3 10.0.0.4 12345 53 17 100 1000 1234567890 1234567890 ACCEPT OK");

        FlowLogProcessor processor = new FlowLogProcessor(lookupFile.toString());
        processor.processFlowLog(flowFile.toString());

        Map<String, Integer> tagCounts = processor.getTagCounts();
        assertEquals(1, tagCounts.get("web"));
        assertEquals(1, tagCounts.get("secure"));
        assertEquals(1, tagCounts.get("dns"));
    }

    @Test
    void testCaseInsensitiveProtocol() throws IOException {
        // Create test lookup table with uppercase protocol
        Path lookupFile = tempDir.resolve("lookup.csv");
        Files.writeString(lookupFile, "dstport,protocol,tag\n" +
                "80,TCP,web");

        // Create test flow log
        Path flowFile = tempDir.resolve("flow.log");
        Files.writeString(flowFile, "2 1234567890 eni-abc123 10.0.0.1 10.0.0.2 12345 80 6 100 1000 1234567890 1234567890 ACCEPT OK");

        FlowLogProcessor processor = new FlowLogProcessor(lookupFile.toString());
        processor.processFlowLog(flowFile.toString());

        Map<String, Integer> tagCounts = processor.getTagCounts();
        assertEquals(1, tagCounts.get("web"));
    }

    @Test
    void testUntaggedEntries() throws IOException {
        // Create test lookup table
        Path lookupFile = tempDir.resolve("lookup.csv");
        Files.writeString(lookupFile, "dstport,protocol,tag\n" +
                "80,tcp,web");

        // Create test flow log with untagged entries
        Path flowFile = tempDir.resolve("flow.log");
        Files.writeString(flowFile, "2 1234567890 eni-abc123 10.0.0.1 10.0.0.2 12345 80 6 100 1000 1234567890 1234567890 ACCEPT OK\n" +
                "2 1234567890 eni-abc123 10.0.0.2 10.0.0.1 80 443 6 100 1000 1234567890 1234567890 ACCEPT OK\n" +
                "2 1234567890 eni-abc123 10.0.0.3 10.0.0.4 12345 53 17 100 1000 1234567890 1234567890 ACCEPT OK");

        FlowLogProcessor processor = new FlowLogProcessor(lookupFile.toString());
        processor.processFlowLog(flowFile.toString());

        Map<String, Integer> tagCounts = processor.getTagCounts();
        assertEquals(1, tagCounts.get("web"));
        assertEquals(2, tagCounts.get("Untagged"));
    }

    @Test
    void testInvalidFlowLogFormat() throws IOException {
        // Create test lookup table
        Path lookupFile = tempDir.resolve("lookup.csv");
        Files.writeString(lookupFile, "dstport,protocol,tag\n" +
                "80,tcp,web");

        // Create test flow log with invalid format
        Path flowFile = tempDir.resolve("flow.log");
        Files.writeString(flowFile, "invalid format\n" +
                "2 1234567890 eni-abc123 10.0.0.1 10.0.0.2 12345 80 6 100 1000 1234567890 1234567890 ACCEPT OK");

        FlowLogProcessor processor = new FlowLogProcessor(lookupFile.toString());
        processor.processFlowLog(flowFile.toString());

        Map<String, Integer> tagCounts = processor.getTagCounts();
        assertEquals(1, tagCounts.get("web"));
    }

    @Test
    void testInvalidLookupTable() throws IOException {
        // Create invalid lookup table (missing header)
        Path lookupFile = tempDir.resolve("lookup.csv");
        Files.writeString(lookupFile, "80,tcp,web");

        // Create test flow log
        Path flowFile = tempDir.resolve("flow.log");
        Files.writeString(flowFile, "2 1234567890 eni-abc123 10.0.0.1 10.0.0.2 12345 80 6 100 1000 1234567890 1234567890 ACCEPT OK");

        assertThrows(IllegalArgumentException.class, () -> {
            new FlowLogProcessor(lookupFile.toString());
        });
    }
} 