package uni.fmi.mjt.project.spotify.utility;

import uni.fmi.mjt.project.spotify.exception.ServerSideException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectByteConvertor {
    public static byte[] convertObjectToByteArray(Object object) {
        try (var byteArrayStream = new ByteArrayOutputStream();
             var objectOutStream = new ObjectOutputStream(byteArrayStream)) {

            objectOutStream.writeObject(object);
            objectOutStream.flush();

            return byteArrayStream.toByteArray();
        } catch (IOException e) {
            throw new ServerSideException("There was a problem converting the format to a byte array", e);
        }
    }

    public static Object convertByteArrayToObject(byte[] reply) {
        try (var objectStream = new ObjectInputStream(new ByteArrayInputStream(reply))) {
            return objectStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("There was a problem converting the byte array to object", e);
        }
    }
}
