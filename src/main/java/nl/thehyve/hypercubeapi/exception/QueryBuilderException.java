package nl.thehyve.hypercubeapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class QueryBuilderException extends RuntimeException {

    public QueryBuilderException(String s) {
        super(s);
    }

    public QueryBuilderException(String s, Throwable throwable) {
        super(s, throwable);
    }

}
