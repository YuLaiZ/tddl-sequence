package com.yulaiz.tddl.sequence.exception;

import java.io.Serial;

public class SequenceException extends Exception {

    @Serial
    private static final long serialVersionUID = -1748347988823381089L;

    public SequenceException() {
        super();
    }

    public SequenceException(String message) {
        super(message);
    }

    public SequenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public SequenceException(Throwable cause) {
        super(cause);
    }
}

