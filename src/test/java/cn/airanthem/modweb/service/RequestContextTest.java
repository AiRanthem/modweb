package cn.airanthem.modweb.service;

import cn.airanthem.modweb.annotation.ModWebService;
import cn.airanthem.modweb.client.ModWebClient;
import cn.airanthem.modweb.config.ModWebConfig;
import cn.airanthem.modweb.iface.ModWebHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RequestContextTest {
    @ModWebService(name = "context")
    public static class DataProvicer implements ModWebHandler {

        @Resource
        RequestContext context;

        @Override
        public byte[] handle(byte[] payload) {
            String address = context.getAddress();
            return address.getBytes(StandardCharsets.UTF_8);
        }
    }

    @Resource
    ModWebClient client;

    @Resource
    ModWebConfig modWebConfig;

    @Test
    public void testLotsOfContext() {
        HashMap<Integer, String> testCases = new HashMap<Integer, String>() {{
            put(0, "127.0.0.1");
            put(1, "127.0.0.1");
            put(2, "127.0.0.1");
            put(3, "127.0.0.1");
        }};
        for (Map.Entry<Integer, String> testCase : testCases.entrySet()) {
            client.putPeer(testCase.getKey(), testCase.getValue(), modWebConfig.getPort());
        }
        Map<Integer, ModWebClient.Result> resultMap = client.all().requestService("context", new byte[0]);
        Set<String> distinctContexts = resultMap.values().stream().map(ModWebClient.Result::getBody).map(String::new).peek(System.out::println).collect(Collectors.toSet());
        assertEquals(testCases.size(), distinctContexts.size());
        client.close();
    }
}