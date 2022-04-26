package cn.airanthem.modweb.register;

import cn.airanthem.modweb.annotation.ModBusService;
import cn.airanthem.modweb.exception.NoHandlerException;
import cn.airanthem.modweb.service.ModWebHandlerManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;

@SpringBootTest
public class BadTest {
    @ModBusService(name = "bad")
    public static class BadHandler {
        public byte[] handle(byte[] payload) {
            System.out.println(Arrays.toString(payload));
            return new byte[]{1, 1, 1, 1, 1};
        }
    }

    @Resource
    ModWebHandlerManager manager;

    @Test
    void testBad() {
        Assertions.assertThrows(NoHandlerException.class, () -> manager.getHandler("bad"));
    }
}
