package cn.airanthem.modweb.client;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;

@SpringBootTest
@Disabled
public class RealModbusReadTest {
    @Resource
    ModWebClient client;

    @Test
    public void testRead502() {
        client.putPeer(1, "127.0.0.1", 502);
        Map<Integer, ModWebClient.Result> resultMap = client.all().readInputRegister(0, 16, 0);
        System.out.println(Arrays.toString(resultMap.get(1).getBody()));
        Map<Integer, ModWebClient.Result> resultMap1 = client.all().readHoldingRegisters(0, 21, 0);
        System.out.println(Arrays.toString(resultMap1.get(1).getBody()));
    }

    @Test
    public void testReadTwice() {
        client.putPeer(1, "127.0.0.1", 502);
        client.putPeer(2, "127.0.0.1", 502);
        Map<Integer, ModWebClient.Result> resultMap = client.all().readInputRegister(0, 16, 0);
        System.out.println(resultMap);
    }

    @Test
    public void testWrite502() {
        client.putPeer(1, "127.0.0.1", 502);
        byte[] data = client.all().readHoldingRegisters(0, 21, 0).get(1).getBody();
        System.out.println(Arrays.toString(data));
        data[0] = 0;
        data[1] = 100;
        data[3] = 110;
        Map<Integer, ModWebClient.Result> resultMap = client.selected(1).writeMultipleRegisters(0, data, 0);
        data = client.all().readHoldingRegisters(0, 21, 0).get(1).getBody();
        System.out.println(Arrays.toString(data));
    }

    @Test
    public void testWriteTwice() {
        client.putPeer(1, "127.0.0.1", 502);
        client.putPeer(2, "127.0.0.1", 502);
        byte[] data = client.all().readHoldingRegisters(0, 21, 0).get(1).getBody();
    }
}
