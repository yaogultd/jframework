package j.core.service.exception;

public class NoNodeAvailableException extends Exception{
    public NoNodeAvailableException() {
        super("no nodes available.");
    }

    public NoNodeAvailableException(String message) {
        super(message);
    }
}
