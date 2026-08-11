package com.travel.common.core.dto;

import java.util.List;

public record ErrorVm(
    String code,
    String message,
    List<String> fieldErrors
) {
}
