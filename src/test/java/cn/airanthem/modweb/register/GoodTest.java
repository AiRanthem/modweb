package cn.airanthem.modweb.register;

import cn.airanthem.modweb.annotation.ModBusService;
import cn.airanthem.modweb.iface.ModWebHandler;
import cn.airanthem.modweb.service.ModWebHandlerManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;

@SpringBootTest
public class GoodTest {
    @ModBusService(name = "good")
    public static class GoodHandler implements ModWebHandler {
        @Override
        public byte[] handle(byte[] payload) {
            System.out.println(Arrays.toString(payload));
            return new byte[]{1, 1, 1, 1, 1};
        }
    }

    @Resource
    ModWebHandlerManager manager;

    @Test void testGood() {
        ModWebHandler good = manager.getHandler("good");
        byte[] handle = good.handle(new byte[]{6, 6, 6, 6, 6});
        Assertions.assertArrayEquals(new byte[]{1, 1, 1, 1, 1}, handle);
    }
}
