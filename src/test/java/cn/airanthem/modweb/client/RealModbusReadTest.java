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
        client.putSlave(1, "127.0.0.1", 502);
        Map<Integer, ModWebClient.Result> resultMap = client.all().readInputRegister(0, 16, 0);
        System.out.println(Arrays.toString(resultMap.get(1).getBody()));
    }
}
