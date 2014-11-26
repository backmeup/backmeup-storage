package org.backmeup.storage.client;


import org.junit.Assert;
import org.junit.Test;

public class StorageConnectionStringBuilderTest {
    @Test
    public void testParseConnectionString() {
        String connectionString = "backmeup-storage;http://localhost:8080/backmeup-storage-service;Token=abc123";
        
        StorageConnectionStringBuilder builder = new StorageConnectionStringBuilder();
        builder.parse(connectionString);
        String actualCS = builder.toString();
        
        Assert.assertEquals(connectionString, actualCS);
    }
    
    @Test
    public void testParseConnectionStringMinimalHost() {
        String connectionString = "backmeup-storage;http://localhost";
        
        StorageConnectionStringBuilder builder = new StorageConnectionStringBuilder();
        builder.parse(connectionString);
        String actualCS = builder.toString();
        
        Assert.assertEquals(connectionString, actualCS);
    }
    
    @Test
    public void testParseConnectionStringMinimalHostWithProperties() {
        String connectionString = "backmeup-storage;http://localhost;Prop1=val1";
        
        StorageConnectionStringBuilder builder = new StorageConnectionStringBuilder();
        builder.parse(connectionString);
        String actualCS = builder.toString();
        
        Assert.assertEquals(connectionString, actualCS);
    }
    
    @Test
    public void testParseConnectionStringMinimalHostWithPort() {
        String connectionString = "backmeup-storage;http://localhost:8080";
        
        StorageConnectionStringBuilder builder = new StorageConnectionStringBuilder();
        builder.parse(connectionString);
        String actualCS = builder.toString();
        
        Assert.assertEquals(connectionString, actualCS);
    }
    
    @Test
    public void testBuildConnectionString() {
        String exprectedCS = "backmeup-storage;http://localhost:8080/backmeup-storage-service;Token=abc123";
        
        StorageConnectionStringBuilder builder = new StorageConnectionStringBuilder();
        builder.setProtocol("http");
        builder.setHost("localhost");
        builder.setPort(8080);
        builder.setPath("/backmeup-storage-service");
        builder.addProperty("Token", "abc123");
        
        String actualCS = builder.toString();
        
        Assert.assertEquals(exprectedCS, actualCS);
    }
}
