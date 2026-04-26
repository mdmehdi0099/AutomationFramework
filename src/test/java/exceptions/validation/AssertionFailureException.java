package exceptions.validation;

import exceptions.base.FrameworkException;

public class AssertionFailureException extends FrameworkException {

    public AssertionFailureException(String message) {
        super(message);
    }

}
