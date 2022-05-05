package cn.airanthem.modweb.client;

import cn.airanthem.modweb.annotation.ModWebService;
import cn.airanthem.modweb.config.ModWebConfig;
import cn.airanthem.modweb.enums.StatusCode;
import cn.airanthem.modweb.iface.ModWebHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
class ServiceCasesTest {

    @Resource
    ModWebConfig modWebConfig;

    @Resource
    ModWebClient client;

    @ModWebService(name = "test")
    public static class DataProvicer implements ModWebHandler {
        @Override
        public byte[] handle(byte[] payload) {
            System.out.println(Arrays.toString(payload));
            return new byte[]{1, 1, 4, 5, 1, 4};
        }
    }

    @Test
    public void testRequestExecutor() {
        HashMap<Integer, String> testCases = new HashMap<Integer, String>() {{
            put(0, "127.0.0.1");
            put(1, "127.0.0.1");
            put(2, "127.0.0.1");
            put(3, "127.0.0.1");
        }};
        for (Map.Entry<Integer, String> testCase : testCases.entrySet()) {
            client.putPeer(testCase.getKey(), testCase.getValue(), modWebConfig.getPort());
        }

        ModWebClient.RequestExecutor executor = client.all();
        Map<Integer, ModWebClient.Result> resultMap = executor.requestService("test", new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9});
        List<ModWebClient.Result> nonNullResults = resultMap.values().stream()
                .filter(result -> result.getStatus() == StatusCode.OK.getValue()).collect(Collectors.toList());
        Assertions.assertEquals(testCases.size(), nonNullResults.size());
        for (Integer i : testCases.keySet()) {
            Assertions.assertArrayEquals(new byte[]{1, 1, 4, 5, 1, 4}, resultMap.get(i).getBody());
        }
        client.close();
    }

    @Test
    public void testRequestTwice() {
        HashMap<Integer, String> testCases = new HashMap<Integer, String>() {{
            put(0, "127.0.0.1");
            put(1, "127.0.0.1");
            put(2, "127.0.0.1");
            put(3, "127.0.0.1");
            put(4, "127.0.0.1");
        }};
        for (Map.Entry<Integer, String> testCase : testCases.entrySet()) {
            client.putPeer(testCase.getKey(), testCase.getValue(), modWebConfig.getPort());
        }

        ModWebClient.RequestExecutor executor = client.all();
        executor.requestService("test", new byte[]{1, 1, 1, 1, 6});
        Map<Integer, ModWebClient.Result> resultMap = executor.requestService("test", new byte[]{1, 1, 1, 2, 7});
        List<ModWebClient.Result> nonNullResults = resultMap.values().stream()
                .filter(result -> result.getStatus() == StatusCode.OK.getValue()).collect(Collectors.toList());
        Assertions.assertEquals(testCases.size(), nonNullResults.size());
        for (Integer i : testCases.keySet()) {
            Assertions.assertArrayEquals(new byte[]{1, 1, 4, 5, 1, 4}, resultMap.get(i).getBody());
        }
        client.close();
    }
}