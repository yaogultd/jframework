package j.core.service.exception;

public class NoResponseException extends Exception{
    public NoResponseException() {
        super("no response.");
    }

    public NoResponseException(String message) {
        super(message);
    }
}
