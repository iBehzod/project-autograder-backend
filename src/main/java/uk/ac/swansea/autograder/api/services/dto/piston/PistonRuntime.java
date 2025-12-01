package uk.ac.swansea.autograder.api.services.dto.piston;

import lombok.Data;

import java.util.List;

/**
 * DTO representing available runtime/language in Piston.
 */
@Data
public class PistonRuntime {
    private String language;
    private String version;
    private List<String> aliases;
}








