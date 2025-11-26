package uk.ac.swansea.autograder.api.services;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.ac.swansea.autograder.api.services.dto.ExecutionDto;
import uk.ac.swansea.autograder.api.services.dto.ExecutionResultDto;
import uk.ac.swansea.autograder.api.services.dto.RuntimeDto;
import uk.ac.swansea.autograder.api.services.dto.piston.PistonExecuteRequest;
import uk.ac.swansea.autograder.api.services.dto.piston.PistonExecuteResponse;
import uk.ac.swansea.autograder.api.services.dto.piston.PistonRuntime;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service for executing student code submissions.
 * Uses self-hosted Piston instance for secure, isolated code execution.
 */
@Service
@Slf4j
public class ExecutionService {
    private final PistonClient pistonClient;
    private final ModelMapper modelMapper;

    public ExecutionService(PistonClient pistonClient, ModelMapper modelMapper) {
        this.pistonClient = pistonClient;
        this.modelMapper = modelMapper;
    }

    /**
     * Execute student code and compare output with expected result.
     *
     * @param dto execution request with code, language, version, input, and expected output
     * @return result indicating if output matches expected
     * @throws Exception if compilation or runtime error occurs
     */
    public ExecutionResultDto submit(ExecutionDto dto) throws Exception {
        PistonExecuteResponse response = execute(dto);
        
        // Check for compilation/runtime errors
        if (response.getRun().getStderr() != null && !response.getRun().getStderr().isBlank()) {
            throw new Exception(response.getRun().getStderr());
        }

        String output = response.getRun().getStdout();
        // Remove trailing newline from output
        if (output != null && output.length() > 1) {
            output = output.substring(0, output.length() - 1);
        }

        String expectedOutput = dto.getExpectedOutput();
        
        // Compare actual vs expected output
        return ExecutionResultDto.builder()
                .output(output)
                .expectedOutput(expectedOutput)
                .isValid(Objects.equals(output, expectedOutput))
                .build();
    }

    /**
     * Execute code using self-hosted Piston instance.
     *
     * @param dto execution parameters
     * @return Piston execution response
     */
    private PistonExecuteResponse execute(ExecutionDto dto) {
        PistonExecuteRequest request = PistonExecuteRequest.builder()
                .language(dto.getLanguage())
                .version(dto.getVersion())
                .files(List.of(
                        PistonExecuteRequest.PistonFile.builder()
                                .name(dto.getFilename())
                                .content(dto.getCode())
                                .build()
                ))
                .stdin(dto.getInput() != null ? dto.getInput() : "")
                .build();
        
        return pistonClient.execute(request);
    }

    /**
     * Get list of available programming language runtimes from Piston.
     * Results are cached to reduce load on Piston instance.
     *
     * @return list of available runtimes
     */
    @Cacheable(value = "runtimes")
    public List<RuntimeDto> getRuntimes() {
        log.debug("Fetching runtimes from Piston (cache miss)");
        
        List<PistonRuntime> pistonRuntimes = pistonClient.getRuntimes();
        
        return pistonRuntimes.stream()
                .map(runtime -> modelMapper.map(runtime, RuntimeDto.class))
                .collect(Collectors.toList());
    }
}
