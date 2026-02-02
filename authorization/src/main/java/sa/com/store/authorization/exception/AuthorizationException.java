package sa.com.store.authorization.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public class AuthorizationException extends RuntimeException {
    public static final String USER_NOT_FOUND_WITH_ID = "User not found with ID: ";
    private final HttpStatus httpStatus;

    public AuthorizationException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

}
