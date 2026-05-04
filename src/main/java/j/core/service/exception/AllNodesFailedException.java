package j.core.service.exception;

public class AllNodesFailedException extends Exception{
    public AllNodesFailedException() {
        super("no response.");
    }

    public AllNodesFailedException(String message) {
        super(message);
    }
}
