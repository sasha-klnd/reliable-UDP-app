package udpdemo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileStreamTest {
    
    public static void main(String[] args) {
        final short MAX_SEG_SIZE = 512;
        final short HEADER_SIZE = 8;
        
        FileInputStream f;
        byte[] dataBytes = new byte[MAX_SEG_SIZE - HEADER_SIZE];

        try {
            f = getInputStream("ulysses.txt");
            f.read(dataBytes);

            System.out.println(new String(dataBytes, StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        // SO WE CAN READ FILES AND GET THE RIGHT NUMBER OF BYTES
        // NEXT: SERVER PARSES RESOURCE STRING AND TRIES TO FIND FILE
        // THEN EITHER SENDS A DATA PACKET (+ OFFSET INCREMENTATION) OR 
        // SENDS AN ERR PACKET IF NOT FOUND
    }

    private static FileInputStream getInputStream(String resourceName) throws FileNotFoundException {
        Path pathToResource = Paths.get("")
            .toAbsolutePath()
            .resolve("resources")
            .resolve(resourceName);

        if (!Files.exists(pathToResource)) {
            throw new FileNotFoundException();
        }

        return new FileInputStream(pathToResource.toString());
    }

}
