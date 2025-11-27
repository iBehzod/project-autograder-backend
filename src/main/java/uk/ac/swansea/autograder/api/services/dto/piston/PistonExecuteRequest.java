package uk.ac.swansea.autograder.api.services.dto.piston;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for Piston code execution API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PistonExecuteRequest {
    private String language;
    private String version;
    private List<PistonFile> files;
    private String stdin;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PistonFile {
        private String name;
        private String content;
    }
}








