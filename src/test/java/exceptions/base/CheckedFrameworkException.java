package exceptions.base;

public class CheckedFrameworkException extends Exception {

    public CheckedFrameworkException(String message) {
        super(message);
    }

    public CheckedFrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
