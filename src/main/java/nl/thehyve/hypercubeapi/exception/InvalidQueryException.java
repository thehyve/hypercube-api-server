package nl.thehyve.hypercubeapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class InvalidQueryException extends RuntimeException {

    public InvalidQueryException(String s) {
        super(s);
    }

    public InvalidQueryException(String s, Throwable throwable) {
        super(s, throwable);
    }

}
