package org.backmeup.storage.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ClientTest {
    public static void main(String[] args) throws IOException {
        StorageClient client = new BackmeupStorageClient();

        String accessToken = "1";

        // upload -------------------------------------------------------------
        File inputFile = new File("C:\\data\\backmeup-storage\\client\\input\\document.txt");
        FileInputStream inputStream = new FileInputStream(inputFile);
        try {
            client.saveFile(accessToken, "/mydocuments/document.txt", true, inputFile.length(), inputStream);
        } finally {
            inputStream.close();
        }
        
        // download -----------------------------------------------------------
        
        FileOutputStream outputStream = new FileOutputStream("C:\\data\\backmeup-storage\\client\\output\\document.txt");
        try {
            client.getFile(accessToken, "/mydocuments/document.txt", outputStream);
        } finally {
            outputStream.close();
        }
    }
}
