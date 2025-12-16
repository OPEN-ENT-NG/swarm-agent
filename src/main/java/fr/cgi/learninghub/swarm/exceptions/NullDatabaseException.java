package fr.cgi.learninghub.swarm.exceptions;

public class NullDatabaseException extends RuntimeException {
    public NullDatabaseException() {
        super("Deployment database is null");
    }
}
