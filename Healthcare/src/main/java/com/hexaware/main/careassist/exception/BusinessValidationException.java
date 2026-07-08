package com.hexaware.main.careassist.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class BusinessValidationException extends RuntimeException {

    private final Map<String, String> fieldErrors;

    public BusinessValidationException(String message) {
        super(message);
        this.fieldErrors = Collections.emptyMap();
    }

    public BusinessValidationException(String field, String message) {
        super(message);
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put(field, message);
        this.fieldErrors = Collections.unmodifiableMap(errors);
    }

    public BusinessValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(fieldErrors));
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
