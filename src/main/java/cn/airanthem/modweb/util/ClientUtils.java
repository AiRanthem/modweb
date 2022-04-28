package cn.airanthem.modweb.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientUtils {
    public static List<byte[]> splitPayload(byte[] payload, int size) {
        ArrayList<byte[]> splited = new ArrayList<>();
        int parts = payload.length / size + 1;
        int left = payload.length;
        for (int i = 0; i < parts; i++) {
            int start = i * size;
            splited.add(Arrays.copyOfRange(payload, start, start + Math.min(left, size)));
            left -= size;
        }
        return splited;
    }
}
