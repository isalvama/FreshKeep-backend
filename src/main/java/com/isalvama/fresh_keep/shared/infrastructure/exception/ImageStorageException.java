package com.isalvama.fresh_keep.shared.infrastructure.exception;

public class ImageStorageException extends InfrastructureException {
    public ImageStorageException(String message) {
        super("Image Storage Error: " + message);
    }
    public ImageStorageException(String message, Throwable cause) {
        super("Image Storage Error: " + message, cause);
    }
}
