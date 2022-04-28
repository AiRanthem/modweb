package cn.airanthem.modweb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PayloadCacheService {

    private static final Logger LOG = LoggerFactory.getLogger(PayloadCacheService.class);
    private final Map<String, ByteArrayOutputStream> cache = new ConcurrentHashMap<>();

    public void push(String remoteAddr, byte[] payloadPart) throws IOException {
        ByteArrayOutputStream stream = cache.computeIfAbsent(remoteAddr, (e) -> new ByteArrayOutputStream());
        try {
            stream.write(payloadPart);
        } catch (IOException e) {
            LOG.error("payload write error", e);
            throw e;
        }
    }

    public byte[] get(String remoteAddr) {
        ByteArrayOutputStream byteArrayOutputStream = cache.get(remoteAddr);
        cache.remove(remoteAddr);
        return byteArrayOutputStream.toByteArray();
    }

    public void drop(String remoteAddr) {
        cache.remove(remoteAddr);
    }

    public Integer getCachedRequestNum() {
        return cache.size();
    }
}
