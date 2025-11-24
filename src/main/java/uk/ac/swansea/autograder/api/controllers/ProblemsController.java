package uk.ac.swansea.autograder.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import uk.ac.swansea.autograder.api.controllers.dto.ProblemBriefDto;
import uk.ac.swansea.autograder.api.controllers.dto.ProblemDto;
import uk.ac.swansea.autograder.api.entities.Problem;
import uk.ac.swansea.autograder.api.services.ProblemService;
import uk.ac.swansea.autograder.api.services.SubmissionMainService;
import uk.ac.swansea.autograder.api.services.dto.RuntimeDto;
import uk.ac.swansea.autograder.config.MyUserDetails;
import uk.ac.swansea.autograder.exceptions.ResourceNotFoundException;
import uk.ac.swansea.autograder.exceptions.UnauthorizedException;

import java.util.List;

/**
 * Create a problem so that students can submit a code for it.
 * Can view submissions for each problem.
 * Can view all problems created previously.
 */
@RestController
@RequestMapping("api/problems")
@Tag(name = "Manage problems", description = "Create a problem so that students can submit a code for it.")
public class ProblemsController {
    private final ProblemService problemService;
    private final SubmissionExecutionService submissionExecutionService;
    private final ModelMapper modelMapper;

    public ProblemsController(ProblemService problemService, SubmissionExecutionService submissionExecutionService, ModelMapper modelMapper) {
        this.problemService = problemService;
        this.submissionExecutionService = submissionExecutionService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_PROBLEM')")
    public List<ProblemBriefDto> getProblems(@RequestParam(defaultValue = "0") Integer pageNo,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
        List<Problem> problems = problemService.getProblems(pageable);
        return modelMapper.map(problems, new TypeToken<List<ProblemBriefDto>>() {}.getType());
    }

    @GetMapping("own")
    @PreAuthorize("hasAuthority('VIEW_PROBLEM')")
    @Operation(
            summary = "Get all problems",
            description = "Returns a paginated list of problems created by the authenticated user. Results are sorted by ID in descending order."
    )
    public List<ProblemBriefDto> getOwnProblems(Authentication authentication,
                                             @RequestParam(defaultValue = "0") Integer pageNo,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        MyUserDetails user = (MyUserDetails) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
        List<Problem> problems = problemService.getProblemsByuserId(user.getId(), pageable);
        return modelMapper.map(problems, new TypeToken<List<ProblemBriefDto>>() {}.getType());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_PROBLEM')")
    @Operation(
            summary = "Create new problem",
            description = "Creates a new programming problem with the provided details. The authenticated user will be set as the creator."
    )
    public ResponseEntity<ProblemDto> createProblem(Authentication authentication,
                                    @Valid @RequestBody ProblemDto problemDto) {
        MyUserDetails user = (MyUserDetails) authentication.getPrincipal();
        problemDto.setUserId(user.getId());
        Problem problem = problemService.createProblem(problemDto);
        ProblemDto createdProblemDto = modelMapper.map(problem, ProblemDto.class);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(problem.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(createdProblemDto);
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAuthority('VIEW_PROBLEM')")
    @Operation(
            summary = "Get problem by ID",
            description = "Returns detailed information about a specific problem."
    )
    public ProblemDto getProblem(@PathVariable Long id) throws ResourceNotFoundException {
        Problem problem = problemService.getProblem(id);
        return modelMapper.map(problem, ProblemDto.class);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAuthority('UPDATE_PROBLEM')")
    @Operation(
            summary = "Update problem",
            description = "Updates an existing problem. Only the user who created the problem can modify it."
    )
    public ProblemDto updateProblem(@PathVariable Long id,
                                    @Valid @RequestBody ProblemDto problemDto)
            throws ResourceNotFoundException {
        Problem problem = problemService.updateProblem(id, problemDto);
        return modelMapper.map(problem, ProblemDto.class);
    }

    @PutMapping("own/{id}")
    @PreAuthorize("hasAuthority('UPDATE_OWN_PROBLEM')")
    @Operation(
            summary = "Update Own problem",
            description = "Updates an existing problem. Only the user who created the problem can modify it."
    )
    public ProblemDto updateOwnProblem(Authentication authentication,
                                    @PathVariable Long id,
                                    @Valid @RequestBody ProblemDto problemDto)
            throws ResourceNotFoundException, UnauthorizedException {
        // check owner id
        MyUserDetails user = (MyUserDetails) authentication.getPrincipal();
        Problem problem = problemService.getProblem(id);
        if (!problem.getUserId().equals(user.getId())) {
            throw new UnauthorizedException();
        }
        // update
        problem = problemService.updateProblem(id, problemDto);
        return modelMapper.map(problem, ProblemDto.class);
    }

    @GetMapping("runtimes")
    @PreAuthorize("hasAuthority('CREATE_PROBLEM')")
    public List<RuntimeDto> getProblemRuntimes() {
        return submissionExecutionService.getRuntimes();
    }
}
