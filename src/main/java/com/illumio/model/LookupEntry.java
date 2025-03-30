package com.illumio.model;

public class LookupEntry {
    private final String dstPort;
    private final String protocol;
    private final String tag;

    public LookupEntry(String dstPort, String protocol, String tag) {
        this.dstPort = dstPort;
        this.protocol = protocol.toLowerCase(); // Convert to lowercase for case-insensitive matching
        this.tag = tag;
    }

    public String getDstPort() {
        return dstPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LookupEntry that = (LookupEntry) o;
        return dstPort.equals(that.dstPort) && protocol.equals(that.protocol);
    }

    @Override
    public int hashCode() {
        return 31 * dstPort.hashCode() + protocol.hashCode();
    }
} 