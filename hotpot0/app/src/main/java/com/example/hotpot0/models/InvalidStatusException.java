package com.example.hotpot0.models;

/**
 * Exception thrown when an invalid or unrecognized status value
 * is assigned to an  EventUserLink or Status object.
 * <p>
 * This custom exception extends Exception and is used to enforce
 * valid status transitions or status field validation in the application.
 * </p>
 */
public class InvalidStatusException extends Exception {

    /**
     * Constructor for InvalidStatusException
     *
     * @param message a descriptive message explaining the reason for the exception
     */
    public InvalidStatusException(String message) {
        super(message);  // Pass the error message to the superclass (Exception)
    }
}
