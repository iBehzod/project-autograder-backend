package uk.ac.swansea.autograder.api.services.dto.piston;

import lombok.Data;

/**
 * Response DTO from Piston code execution API.
 */
@Data
public class PistonExecuteResponse {
    private String language;
    private String version;
    private RunResult run;
    
    @Data
    public static class RunResult {
        private String stdout;
        private String stderr;
        private Integer code;
        private String signal;
        private String output;
    }
}








