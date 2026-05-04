package j.core.service.exception;

public class SomeNodesFailedException extends Exception{
    public SomeNodesFailedException() {
        super("no response.");
    }

    public SomeNodesFailedException(String message) {
        super(message);
    }
}
