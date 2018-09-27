package com.bitmex.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Operation implements Serializable {

    private static final String OPERATION_FIELD = "op";
    private static final String ARGS_FIELD = "args";

    @JsonProperty(OPERATION_FIELD)
    private final String operation;
    @JsonProperty(ARGS_FIELD)
    private final Collection<String> args;

    public Operation(String operation, Collection<String> args) {
        this.operation = operation;
        this.args = args;
    }

    public String getOperation() {
        return operation;
    }

    public Collection<String> getArgs() {
        return args;
    }
}
