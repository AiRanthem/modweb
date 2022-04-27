package cn.airanthem.modweb.client;

import cn.airanthem.modweb.annotation.ModBusService;
import cn.airanthem.modweb.config.ApplicationConfig;
import cn.airanthem.modweb.iface.ModWebHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;

@SpringBootTest
class CaseLoadDataTest {

    @Resource
    ApplicationConfig applicationConfig;

    @Resource
    ModWebClient client;

    @ModBusService(name = "data")
    public static class DataProvicer implements ModWebHandler {
        @Override
        public byte[] handle(byte[] payload) {
            System.out.println(Arrays.toString(payload));
            return new byte[]{1, 1, 4, 5, 1, 4, 6};
        }
    }

    @Test
    public void testRequestExecutor() {
        client.putSlave(1, "127.0.0.1", applicationConfig.getPort());
        ModWebClient.RequestExecutor executor = client.all();
        Map<Integer, ModWebClient.Result> resultMap = executor.requestService("data", new byte[]{1, 1, 1, 1, 1});
        Assertions.assertEquals(1, resultMap.size());
        Assertions.assertArrayEquals(new byte[]{1, 1, 4, 5, 1, 4, 6}, resultMap.get(1).getBody());
    }

}