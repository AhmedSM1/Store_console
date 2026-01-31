package sa.com.store.authorization.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public class AuthorizationException extends RuntimeException {
    private HttpStatus httpStatus;

    public AuthorizationException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

}
