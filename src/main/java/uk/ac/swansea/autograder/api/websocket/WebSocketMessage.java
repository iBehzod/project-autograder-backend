package uk.ac.swansea.autograder.api.websocket;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

/**
 * WebSocket message for requesting submission test results.
 * Validates that submissionId is present and positive.
 */
@Getter
public class WebSocketMessage {
    @NotNull(message = "Submission ID is required")
    @Positive(message = "Submission ID must be positive")
    private Long submissionId;
}
