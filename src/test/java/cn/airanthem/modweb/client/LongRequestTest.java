package cn.airanthem.modweb.client;

import cn.airanthem.modweb.annotation.ModWebService;
import cn.airanthem.modweb.config.ModWebConfig;
import cn.airanthem.modweb.enums.StatusCode;
import cn.airanthem.modweb.iface.ModWebHandler;
import cn.airanthem.modweb.proto.Request;
import cn.airanthem.modweb.proto.Response;
import cn.airanthem.modweb.service.PayloadCacheService;
import cn.airanthem.modweb.util.ClientUtils;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
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
public class LongRequestTest {
    @Resource
    ModWebConfig modWebConfig;
    @Resource
    ModWebClient client;

    @Resource
    PayloadCacheService payloadCacheService;

    @ModWebService(name = "long")
    public static class LongConsumer implements ModWebHandler {
        @Override
        public byte[] handle(byte[] payload) {
            List<byte[]> splitPayload = ClientUtils.splitPayload(payload, ModWebClient.PARTITION_SIZE);
            for (byte[] bytes : splitPayload) {
                System.out.println(bytes.length);
                System.out.println(Arrays.toString(bytes));
            }
            return new byte[]{1, 1, 4, 5, 1, 4};
        }
    }

    @Test
    public void testVeryLongData() {
        HashMap<Integer, String> testCases = new HashMap<Integer, String>() {{
            put(0, "127.0.0.1");
        }};
        for (Map.Entry<Integer, String> testCase : testCases.entrySet()) {
            client.putPeer(testCase.getKey(), testCase.getValue(), modWebConfig.getPort());
        }

        Map<Integer, ModWebClient.Result> resultMap = client.all().requestService("long", new byte[1024]);
        List<ModWebClient.Result> nonNullResults = resultMap.values().stream()
                .filter(result -> result.getStatus() == StatusCode.OK.getValue()).collect(Collectors.toList());
        Assertions.assertEquals(testCases.size(), nonNullResults.size());
        for (Integer i : testCases.keySet()) {
            Assertions.assertArrayEquals(new byte[]{1, 1, 4, 5, 1, 4}, resultMap.get(i).getBody());
        }
        client.close();
        Assertions.assertEquals(0, payloadCacheService.getCachedRequestNum());
    }

    @Test
    public void testConcurrentLongData() {
        HashMap<Integer, String> testCases = new HashMap<Integer, String>() {{
            put(0, "127.0.0.1");
            put(1, "127.0.0.1");
            put(2, "127.0.0.1");
        }};
        for (Map.Entry<Integer, String> testCase : testCases.entrySet()) {
            client.putPeer(testCase.getKey(), testCase.getValue(), modWebConfig.getPort());
        }

        Map<Integer, ModWebClient.Result> resultMap = client.all().requestService("long", new byte[255]);
        List<ModWebClient.Result> nonNullResults = resultMap.values().stream()
                .filter(result -> result.getStatus() == StatusCode.OK.getValue()).collect(Collectors.toList());
        Assertions.assertEquals(testCases.size(), nonNullResults.size());
        for (Integer i : testCases.keySet()) {
            Assertions.assertArrayEquals(new byte[]{1, 1, 4, 5, 1, 4}, resultMap.get(i).getBody());
        }
        client.close();
        Assertions.assertEquals(0, payloadCacheService.getCachedRequestNum());
    }

    @Test
    public void testUnfinished() throws InvalidProtocolBufferException {
        HashMap<Integer, String> testCases = new HashMap<Integer, String>() {{
            put(0, "127.0.0.1");
        }};
        for (Map.Entry<Integer, String> testCase : testCases.entrySet()) {
            client.putPeer(testCase.getKey(), testCase.getValue(), modWebConfig.getPort());
        }

        Request request = Request.newBuilder().setName("long").setPart(1)
                .setPayload(ByteString.copyFrom(new byte[]{1, 2, 3, 4, 5})).build();
        Map<Integer, ModWebClient.Result> resultMap = client.all().readWriteMultipleRegisters(0, 0, 0, generateBodyBytes(request), 0);
        Response response = Response.parseFrom(resultMap.get(0).getBody());
        Assertions.assertEquals(StatusCode.ROGER_THAT.getValue(), response.getStatus());
        Assertions.assertEquals(1, payloadCacheService.getCachedRequestNum());
        Request decline = Request.newBuilder().setName("long").setPart(-1)
                .setPayload(ByteString.copyFrom(new byte[]{1, 2, 3, 4, 5})).build();
        Map<Integer, ModWebClient.Result> resultMap1 = client.all().readWriteMultipleRegisters(0, 0, 0, generateBodyBytes(decline), 0);
        Response response1 = Response.parseFrom(resultMap1.get(0).getBody());
        Assertions.assertEquals(StatusCode.OK.getValue(), response1.getStatus());
        Assertions.assertEquals(0, payloadCacheService.getCachedRequestNum());
    }

    private byte[] generateBodyBytes(Request request) {
        byte[] bodyBytes = request.toByteArray();
        if (bodyBytes.length >= 3 && bodyBytes.length % 2 != 0) {
            int x = bodyBytes.length;
            bodyBytes = Arrays.copyOf(bodyBytes, x + 3);
            bodyBytes[x] = 1;
            bodyBytes[x + 1] = -1;
            bodyBytes[x + 2] = 1;
        }
        return bodyBytes;
    }
}
