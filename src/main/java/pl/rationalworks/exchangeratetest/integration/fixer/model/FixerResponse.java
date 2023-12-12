package pl.rationalworks.exchangeratetest.integration.fixer.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(FixerErrorResponse.class),
        @JsonSubTypes.Type(LatestRates.class)})
@Getter
public abstract class FixerResponse {
    /**
     * Returns true or false depending on whether your API request has succeeded.
     */
    private final boolean success;

    public FixerResponse(boolean success) {
        this.success = success;
    }
}
