package cn.airanthem.modweb;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;

public class DemoTest {
    @Test
    public void payloadNull() {
        ByteString bytes = byteString(new byte[0]);
    }

    private ByteString byteString(byte[] bytes) {
        return ByteString.copyFrom(bytes);
    }
}
