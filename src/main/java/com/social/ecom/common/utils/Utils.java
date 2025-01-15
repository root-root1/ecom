package com.social.ecom.common.utils;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Component
public class Utils {
    public <T> byte[] serializeOrder(T order) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(order);
            oos.flush();
            return bos.toByteArray();
        }
    }
}
