package cn.airanthem.modweb.client;

import cn.airanthem.modweb.annotation.ModBusService;
import cn.airanthem.modweb.config.ModWebConfig;
import cn.airanthem.modweb.enums.StatusCode;
import cn.airanthem.modweb.iface.ModWebHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
class CaseLoadDataTest {

    @Resource
    ModWebConfig modWebConfig;

    @Resource
    ModWebClient client;

    @ModBusService(name = "data")
    public static class DataProvicer implements ModWebHandler {
        @Override
        public byte[] handle(byte[] payload) {
            System.out.println(Arrays.toString(payload));
            return new byte[]{1, 1, 4, 5, 1, 4};
        }
    }

    @Test
    public void testRequestExecutor() {
        int peerNum = 4;
        for (int i = 0; i < peerNum; i++) {
            client.putPeer(i, "127.0.0.1", modWebConfig.getPort());
        }
        ModWebClient.RequestExecutor executor = client.all();
        Map<Integer, ModWebClient.Result> resultMap = executor.requestService("data", new byte[]{1, 1, 1, 1, 1});
        List<ModWebClient.Result> nonNullResults = resultMap.values().stream()
                .filter(result -> result.getStatus() == StatusCode.OK.getValue()).collect(Collectors.toList());
        Assertions.assertEquals(peerNum, nonNullResults.size());
        for (int i = 0; i < peerNum; i++) {
            Assertions.assertArrayEquals(new byte[]{1, 1, 4, 5, 1, 4}, resultMap.get(i).getBody());
        }
    }

    @Test
    public void testRequestTwice() {
        client.putPeer(1, "127.0.0.1", modWebConfig.getPort());
        client.putPeer(2, "127.0.0.1", modWebConfig.getPort());
        client.putPeer(3, "127.0.0.1", modWebConfig.getPort());
        Map<Integer, ModWebClient.Result> resultMap1 = client.all().requestService("data", new byte[]{1, 1, 1, 1, 1});
        Map<Integer, ModWebClient.Result> resultMap2 = client.all().requestService("data", new byte[]{2, 2, 2, 2});
        Map<Integer, ModWebClient.Result> resultMap3 = client.all().requestService("data", new byte[]{3, 3, 3, 3, 3, 3});
        System.out.println();
    }

    @Test
    public void testAnother() {
        client.putPeer(1, "127.0.0.1", modWebConfig.getPort());
        client.putPeer(2, "127.0.0.1", modWebConfig.getPort());
        Map<Integer, ModWebClient.Result> resultMap1 = client.all().readHoldingRegisters(0, 10, 0);
        Map<Integer, ModWebClient.Result> resultMap2 = client.all().readHoldingRegisters(0, 10, 0);
        System.out.println();
    }

}