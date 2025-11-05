package com.example.hotpot0.models;

public class InvalidStatusException extends Exception {
    // Constructor for InvalidStatusException
    public InvalidStatusException(String message) {
        super(message);  // Pass the error message to the superclass (Exception)
    }
}
