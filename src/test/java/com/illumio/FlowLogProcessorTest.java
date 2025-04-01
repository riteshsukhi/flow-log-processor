package com.illumio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class FlowLogProcessorTest {

    @Test
    void testBasicFlowLogProcessing(@TempDir Path tempDir) throws IOException {
        // Create test files
        Path lookupFile = tempDir.resolve("lookup.csv");
        Path flowFile = tempDir.resolve("flow.log");
        Path outputFile = tempDir.resolve("output.txt");
        
        Files.writeString(lookupFile, "dstport,protocol,tag\n" +
                "80,tcp,sv_P1\n" +
                "443,tcp,sv_P2\n");
        
        Files.writeString(flowFile, "2 1234567890 eni-abc123 10.0.0.1 10.0.0.2 12345 80 6 100 1000 1234567890 1234567890 ACCEPT OK\n" +
                "2 1234567890 eni-abc123 10.0.0.2 10.0.0.1 12345 443 6 100 1000 1234567890 1234567890 ACCEPT OK\n");

        FlowLogProcessor processor = new FlowLogProcessor(lookupFile.toString());
        processor.processFlowLog(flowFile.toString(), outputFile.toString());

        String output = Files.readString(outputFile);
        assertTrue(output.contains("sv_P1"));
        assertTrue(output.contains("sv_P2"));
    }

    @Test
    void testCaseInsensitiveProtocol(@TempDir Path tempDir) throws IOException {
        Path lookupFile = tempDir.resolve("lookup.csv");
        Path flowFile = tempDir.resolve("flow.log");
        Path outputFile = tempDir.resolve("output.txt");
        
        Files.writeString(lookupFile, "dstport,protocol,tag\n" +
                "80,TCP,sv_P1\n");
        
        Files.writeString(flowFile, "2 1234567890 eni-abc123 10.0.0.1 10.0.0.2 12345 80 6 100 1000 1234567890 1234567890 ACCEPT OK\n");

        FlowLogProcessor processor = new FlowLogProcessor(lookupFile.toString());
        processor.processFlowLog(flowFile.toString(), outputFile.toString());

        String output = Files.readString(outputFile);
        assertTrue(output.contains("sv_P1"));
    }

    @Test
    void testUntaggedEntries(@TempDir Path tempDir) throws IOException {
        Path lookupFile = tempDir.resolve("lookup.csv");
        Path flowFile = tempDir.resolve("flow.log");
        Path outputFile = tempDir.resolve("output.txt");
        
        Files.writeString(lookupFile, "dstport,protocol,tag\n" +
                "80,tcp,sv_P1\n");
        
        Files.writeString(flowFile, "2 1234567890 eni-abc123 10.0.0.1 10.0.0.2 12345 80 6 100 1000 1234567890 1234567890 ACCEPT OK\n" +
                "2 1234567890 eni-abc123 10.0.0.2 10.0.0.1 12345 443 6 100 1000 1234567890 1234567890 ACCEPT OK\n");

        FlowLogProcessor processor = new FlowLogProcessor(lookupFile.toString());
        processor.processFlowLog(flowFile.toString(), outputFile.toString());

        String output = Files.readString(outputFile);
        assertTrue(output.contains("Untagged"));
    }

    @Test
    void testInvalidFlowLogFormat(@TempDir Path tempDir) throws IOException {
        Path lookupFile = tempDir.resolve("lookup.csv");
        Path flowFile = tempDir.resolve("flow.log");
        Path outputFile = tempDir.resolve("output.txt");
        
        Files.writeString(lookupFile, "dstport,protocol,tag\n" +
                "80,tcp,sv_P1\n");
        
        Files.writeString(flowFile, "invalid format\n");

        FlowLogProcessor processor = new FlowLogProcessor(lookupFile.toString());
        processor.processFlowLog(flowFile.toString(), outputFile.toString());

        String output = Files.readString(outputFile);
        assertTrue(output.contains("Tag Counts"));
    }

    @Test
    void testInvalidLookupTable(@TempDir Path tempDir) {
        Path lookupFile = tempDir.resolve("lookup.csv");
        
        assertThrows(IllegalArgumentException.class, () -> {
            Files.writeString(lookupFile, "invalid header\n");
            new FlowLogProcessor(lookupFile.toString());
        });
    }
} 