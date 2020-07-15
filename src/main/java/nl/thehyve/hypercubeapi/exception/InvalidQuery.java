package nl.thehyve.hypercubeapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class InvalidQuery extends RuntimeException {

    public InvalidQuery(String s) {
        super(s);
    }

    public InvalidQuery(String s, Throwable throwable) {
        super(s, throwable);
    }

}
