package cn.airanthem.modweb.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class ClientUtilsTest {

    @Test
    void splitPayload() {
        byte[] bytes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31};
        List<byte[]> splitPayload = ClientUtils.splitPayload(bytes, 5);
        for (byte[] bts : splitPayload) {
            System.out.println(Arrays.toString(bts));
        }
    }
}