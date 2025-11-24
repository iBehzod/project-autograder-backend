package uk.ac.swansea.autograder.general.enums;

/**
 * Enumeration of all system permissions.
 * Permissions are grouped by domain and sorted alphabetically within each group.
 *
 * Implemented as a final constants class (not an enum)
 * so values can be used directly in annotations like:
 * @PreAuthorize("hasAuthority('" + VIEW_PROBLEM + "')")
 */
public final class PermissionEnum {

    private PermissionEnum() {
        // Prevent instantiation
    }

    // ---------------------------------------------------------
    // User Management (alphabetically sorted)
    // ---------------------------------------------------------
    public static final String CREATE_USER = "CREATE_USER";
    public static final String UPDATE_OWN_USER = "UPDATE_OWN_USER";
    public static final String UPDATE_USER = "UPDATE_USER";
    public static final String VIEW_OWN_USER = "VIEW_OWN_USER";
    public static final String VIEW_USER = "VIEW_USER";

    // Permission management
    public static final String VIEW_PERMISSION = "VIEW_PERMISSION";

    // ---------------------------------------------------------
    // Role Management (alphabetically sorted)
    // ---------------------------------------------------------
    public static final String ASSIGN_PERMISSION = "ASSIGN_PERMISSION";
    public static final String ASSIGN_ROLE = "ASSIGN_ROLE";
    public static final String CREATE_ROLE = "CREATE_ROLE";
    public static final String UPDATE_ROLE = "UPDATE_ROLE";
    public static final String VIEW_ROLE = "VIEW_ROLE";

    // ---------------------------------------------------------
    // Problem Management (alphabetically sorted)
    // ---------------------------------------------------------
    public static final String CREATE_PROBLEM = "CREATE_PROBLEM";
    public static final String UPDATE_OWN_PROBLEM = "UPDATE_OWN_PROBLEM";
    public static final String UPDATE_PROBLEM = "UPDATE_PROBLEM";
    public static final String VIEW_PROBLEM = "VIEW_PROBLEM";

    // ---------------------------------------------------------
    // Test Case Management (alphabetically sorted)
    // ---------------------------------------------------------
    public static final String CREATE_TEST_CASE = "CREATE_TEST_CASE";
    public static final String VIEW_TEST_CASE = "VIEW_TEST_CASE";

    // ---------------------------------------------------------
    // Submission Management (alphabetically sorted)
    // ---------------------------------------------------------
    public static final String CREATE_SUBMISSION = "CREATE_SUBMISSION";
    public static final String VIEW_OWN_SUBMISSION = "VIEW_OWN_SUBMISSION";
    public static final String VIEW_SUBMISSION = "VIEW_SUBMISSION";
}
