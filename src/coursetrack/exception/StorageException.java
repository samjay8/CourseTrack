package coursetrack.exception;

/**
 * Thrown when saving or loading the course file fails, for example because
 * the file does not exist or the disk cannot be written.
 *
 * <p>It wraps the original {@link java.io.IOException} as its cause, so the
 * technical detail is kept while the user sees a friendly message.</p>
 */
public class StorageException extends CourseException {

    /**
     * @param message user-friendly description of the storage problem
     */
    public StorageException(String message) {
        super(message);
    }

    /**
     * @param message user-friendly description of the storage problem
     * @param cause   the underlying I/O exception
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
