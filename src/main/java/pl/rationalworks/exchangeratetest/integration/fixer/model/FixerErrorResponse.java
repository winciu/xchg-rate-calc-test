package pl.rationalworks.exchangeratetest.integration.fixer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public final class FixerErrorResponse extends FixerResponse {

    private final Error error;

    @JsonCreator
    public FixerErrorResponse(@JsonProperty("success") boolean success, @JsonProperty("error") Error error) {
        super(success);
        this.error = error;
    }

    public record Error(int code, String info) {
    }
}
