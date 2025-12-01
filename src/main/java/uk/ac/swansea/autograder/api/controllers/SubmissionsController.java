package uk.ac.swansea.autograder.api.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import uk.ac.swansea.autograder.api.controllers.dto.SubmissionBriefDto;
import uk.ac.swansea.autograder.api.controllers.dto.SubmissionDetailDto;
import uk.ac.swansea.autograder.api.controllers.dto.SubmissionDto;
import uk.ac.swansea.autograder.api.entities.Submission;
import uk.ac.swansea.autograder.api.services.SubmissionDetailService;
import uk.ac.swansea.autograder.api.services.SubmissionExecutionService;
import uk.ac.swansea.autograder.api.services.SubmissionService;
import uk.ac.swansea.autograder.config.MyUserDetails;
import uk.ac.swansea.autograder.exceptions.BadRequestException;
import uk.ac.swansea.autograder.exceptions.ResourceNotFoundException;
import uk.ac.swansea.autograder.exceptions.UnauthorizedException;

import java.util.List;
import java.util.stream.Collectors;

import static uk.ac.swansea.autograder.general.enums.PermissionEnum.*;

/**
 * Get all submissions.
 * Get a specific submission.
 * View every test case of every submission.
 */
@RestController
@RequestMapping("api/submissions")
@Tag(name = "Manage submissions", description = "Can create/update submissions.")
public class SubmissionsController {
    private final SubmissionService submissionService;
    private final SubmissionDetailService submissionDetailService;
    private final SubmissionExecutionService submissionExecutionService;
    private final ModelMapper modelMapper;

    public SubmissionsController(SubmissionService submissionService,
                                 SubmissionDetailService submissionDetailService,
                                 SubmissionExecutionService submissionExecutionService,
                                 ModelMapper modelMapper) {
        this.submissionService = submissionService;
        this.submissionDetailService = submissionDetailService;
        this.submissionExecutionService = submissionExecutionService;
        this.modelMapper = modelMapper;
    }

    /**
     * Get the list of submitted solutions to a specific problem
     *
     * @return list of submissions
     */
    @GetMapping
    @PreAuthorize("hasAuthority('" + VIEW_SUBMISSION + "')")
    public Page<SubmissionBriefDto> getSubmissions(@RequestParam(required = false) Long problemId,
                                                   @RequestParam(defaultValue = "0") Integer pageNo,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
        Page<Submission> submissions;
        if (problemId != null) {
            submissions = submissionService.getSubmissionsByProblemId(problemId, pageable);
        } else {
            submissions = submissionService.getSubmissions(pageable);
        }
        return submissions.map(s -> modelMapper.map(s, SubmissionBriefDto.class));
    }

    /**
     * Get the list of submitted solutions by the student
     * also for a specific problem
     *
     * @return list of submissions
     */
    @GetMapping("own")
    @PreAuthorize("hasAuthority('" + VIEW_OWN_SUBMISSION + "')")
    public Page<SubmissionBriefDto> getOwnSubmissions(Authentication authentication,
                                                   @RequestParam(required = false) Long problemId,
                                                   @RequestParam(defaultValue = "0") Integer pageNo,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        MyUserDetails user = (MyUserDetails) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
        Page<Submission> submissions;
        if (problemId != null) {
            submissions = submissionService
                    .getSubmissionsByProblemIdAndUserId(problemId, user.getId(), pageable);
        } else {
            submissions = submissionService
                    .getSubmissionsByUserId(user.getId(), pageable);
        }
        return submissions.map(s -> modelMapper.map(s, SubmissionBriefDto.class));
    }

    /**
     * Get the specific submission
     *
     * @return submission
     */
    @GetMapping("{id}")
    @PreAuthorize("hasAuthority('" + VIEW_SUBMISSION + "')")
    public SubmissionDto getSubmission(@PathVariable Long id)
            throws ResourceNotFoundException {
        Submission submission = submissionService.getSubmission(id);
        return modelMapper.map(submission, SubmissionDto.class);
    }

    /**
     * Get own submission
     *
     * @return submission
     */
    @GetMapping("own/{id}")
    @PreAuthorize("hasAuthority('" + VIEW_OWN_SUBMISSION + "')")
    public SubmissionDto getOwnSubmission(Authentication authentication,
                                          @PathVariable Long id)
            throws ResourceNotFoundException, UnauthorizedException {
        Submission submission = submissionService.getSubmission(id);
        // check owner id
        MyUserDetails user = (MyUserDetails) authentication.getPrincipal();
        if (!submission.getUserId().equals(user.getId())) {
            throw new UnauthorizedException();
        }
        return modelMapper.map(submission, SubmissionDto.class);
    }

    /**
     * Get test cases and results
     */
    @GetMapping("{id}/detail")
    @PreAuthorize("hasAuthority('" + VIEW_SUBMISSION + "')")
    public List<SubmissionDetailDto> getSubmissionDetails(@PathVariable Long id) {
        return submissionDetailService.getSubmissionDetail(id)
                .stream()
                .map(submissionDetail -> SubmissionDetailDto.builder()
                        .id(submissionDetail.getId())
                        .submissionId(submissionDetail.getSubmissionId())
                        .input(submissionDetail.getTestCase().getInput())
                        .expectedOutput(submissionDetail.getTestCase().getExpectedOutput())
                        .actualOutput(submissionDetail.getActualOutput())
                        .testCaseIsPassed(submissionDetail.getTestCaseIsPassed())
                        .build()
                ).collect(Collectors.toList());
    }

    /**
     * Get own submission details, which include test cases and results
     */
    @GetMapping("own/{id}/detail")
    @PreAuthorize("hasAuthority('" + VIEW_OWN_SUBMISSION + "')")
    public List<SubmissionDetailDto> getOwnSubmissionDetails(Authentication authentication,
                                                          @PathVariable Long id) throws ResourceNotFoundException, UnauthorizedException {
        // check owner id
        MyUserDetails user = (MyUserDetails) authentication.getPrincipal();
        Submission submission = submissionService.getSubmission(id);
        if (!submission.getUserId().equals(user.getId())) {
            throw new UnauthorizedException();
        }
        return submissionDetailService.getSubmissionDetail(id)
                .stream()
                .map(submissionDetail -> SubmissionDetailDto.builder()
                        .id(submissionDetail.getId())
                        .submissionId(submissionDetail.getSubmissionId())
                        .input(submissionDetail.getTestCase().getInput())
                        .expectedOutput(submissionDetail.getTestCase().getExpectedOutput())
                        .actualOutput(submissionDetail.getActualOutput())
                        .testCaseIsPassed(submissionDetail.getTestCaseIsPassed())
                        .build()
                ).collect(Collectors.toList());
    }


    /**
     * Submit a solution to a problem
     *
     * @param submissionDto submission body
     */
    @PostMapping
    @PreAuthorize("hasAuthority('" + CREATE_SUBMISSION + "')")
    public ResponseEntity<Submission> submitSolution(Authentication authentication,
                                     @Valid @RequestBody SubmissionDto submissionDto)
            throws ResourceNotFoundException, BadRequestException {
        MyUserDetails user = (MyUserDetails) authentication.getPrincipal();
        submissionDto.setUserId(user.getId());
        Submission submission = submissionExecutionService.submitSolution(submissionDto);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(submission.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(submission);
    }

    /**
     * Get test cases and results
     */
    @GetMapping("own/{id}/test-result")
    @PreAuthorize("hasAuthority('" + VIEW_OWN_SUBMISSION + "')")
    public Submission getTestResult(Authentication authentication,
                                              @PathVariable Long id)
            throws ResourceNotFoundException, UnauthorizedException {
        MyUserDetails user = (MyUserDetails) authentication.getPrincipal();
        // check owner id
        Submission submission = submissionService.getSubmission(id);
        if (!submission.getUserId().equals(user.getId())) {
            throw new UnauthorizedException();
        }
        return submissionService.getSubmission(id);
    }
}
