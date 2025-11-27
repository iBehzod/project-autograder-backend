package uk.ac.swansea.autograder.api.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import uk.ac.swansea.autograder.api.entities.Submission;
import uk.ac.swansea.autograder.api.services.SubmissionService;
import uk.ac.swansea.autograder.config.MyUserDetails;
import uk.ac.swansea.autograder.exceptions.UnauthorizedException;

import static uk.ac.swansea.autograder.general.enums.PermissionEnum.VIEW_SUBMISSION;

@Controller
public class WebSocketController {
    private final SubmissionService submissionService;

    public WebSocketController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @MessageMapping("test-result")
    @SendTo("/topic/test-results")
    public Submission getTestResult(WebSocketMessage webSocketMessage, Authentication authentication)
            throws Exception {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User must be authenticated");
        }

        Submission submission = submissionService.getSubmission(webSocketMessage.getSubmissionId());
        MyUserDetails user = (MyUserDetails) authentication.getPrincipal();

        // Check if user owns submission OR has VIEW_SUBMISSION permission (Admin/Lecturer)
        boolean isOwner = submission.getUserId().equals(user.getId());
        boolean hasPermission = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(VIEW_SUBMISSION.name()));

        if (!isOwner && !hasPermission) {
            throw new UnauthorizedException("Access Denied");
        }

        return submission;
    }

}