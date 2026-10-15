package dev.samasignal.api.contract;

import java.util.Map;

public record ApiError(String code, String message, Map<String, String> fieldErrors) {}
