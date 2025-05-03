package com.vncafe.constants;

public class ResponseStatusCode {
    private ResponseStatusCode() {}
    //HTTP 200 - OK
    public static final int OK = 2000;

    // HTTP 400 - Bad Request
    public static final int INVALID_INPUT = 4001;
    public static final int FORBIDDEN_OPERATION = 4002; // Actually 403, see below
    public static final int CLOSURE_RULE_VIOLATION = 4003;
    public static final int INTERNAL_ERROR = 4004; // Actually 500
    public static final int REFERENCE_EXISTS = 4005;
    public static final int RESOURCE_NOT_FOUND = 4007; // 404
    public static final int NOT_UNIQUE = 4008;
    public static final int EDIT_NON_EDITABLE_FIELD = 4009;
    public static final int EDIT_INTERNAL_FIELD = 4010;
    public static final int NO_SUCH_FIELD = 4011;
    public static final int MANDATORY_FIELD_MISSING = 4012;
    public static final int EDIT_READ_ONLY_FIELD = 4014;
    public static final int RATE_LIMIT_REACHED = 4015;
    public static final int ALREADY_IN_TRASH = 4016;
    public static final int NOT_IN_TRASH = 4017;
    public static final int TUPLE_LIMIT_REACHED = 4018;
    public static final int UNAUTHORIZED_USER = 4019;
    public static final int NO_LICENSE = 7001;

    // HTTP 403 - Forbidden
    public static final int USER_NOT_ALLOWED = 4002;

    // HTTP 404 - Not Found
    public static final int INVALID_URL_OR_RESOURCE_NOT_FOUND = 4007;

    // HTTP 415 - Unsupported Media Type
    public static final int UNSUPPORTED_CONTENT_TYPE = 4013;

    // HTTP 500 - Internal Server Error
    public static final int EXACT_INTERNAL_ERROR = 4004;
}
