package cn.airanthem.modweb.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusCode {
    OK(0),
    NO_HANDLER(1),
    BAD_REQUEST(2),
    BAD_RESPONSE(3),
    ROGER_THAT(10),
    UNKNOWN_ERROR(100);
    private final int value;
}
