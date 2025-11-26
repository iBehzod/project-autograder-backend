package uk.ac.swansea.autograder.api.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.swansea.autograder.api.services.SubmissionExecutionService;
import uk.ac.swansea.autograder.exceptions.ResourceNotFoundException;

/**
 * Redis message receiver for processing submission requests asynchronously.
 * Uses constructor injection for better testability and explicit dependency declaration.
 */
public class SubmissionReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubmissionReceiver.class);

    private final SubmissionExecutionService submissionExecutionService;

    public SubmissionReceiver(SubmissionExecutionService submissionExecutionService) {
        this.submissionExecutionService = submissionExecutionService;
    }

    public void receiveMessage(String submissionId) throws ResourceNotFoundException {
        LOGGER.info("Received submissionId=#{}", submissionId);
        submissionExecutionService.runSubmission(Long.valueOf(submissionId));
    }
}
