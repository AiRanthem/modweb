package cn.airanthem.modweb.util;

import cn.airanthem.modweb.client.ModWebClient;
import cn.airanthem.modweb.enums.StatusCode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModWebResultUtils {
    public static List<Integer> whoFails(Map<Integer, ModWebClient.Result> resultMap) {
        return resultMap.entrySet().stream()
                .filter(e -> {
                    Integer status = e.getValue().getStatus();
                    return status != StatusCode.OK.getValue() && status != StatusCode.ROGER_THAT.getValue();
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
