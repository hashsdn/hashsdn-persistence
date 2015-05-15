package org.opendaylight.persistence.util.common.log;


/**
 * Logger.
 * <p>
 * You generally don't catch Throwable. Throwable is the superclass to Exception and Error. Errors
 * are generally things which a normal application wouldn't and shouldn't catch, so just use
 * Exception unless you have a specific reason to use Throwable.
 * <p>
 * In this case Throwable is used since a logger just keeps the trace.
 * 
 * @author Fabiel Zuniga
 */
public interface Logger {

    /**
     * Logs an error message.
     * 
     * @param message message
     */
    void error(String message);

    /**
     * Logs an error message.
     * 
     * @param message message
     * @param cause cause
     */
    void error(String message, Throwable cause);

    /**
     * Logs a warning message.
     * 
     * @param message message
     */
    void warning(String message);

    /**
     * Logs a warning message.
     * 
     * @param message message
     * @param cause cause
     */
    void warning(String message, Throwable cause);

    /**
     * Logs an information message.
     * 
     * @param message message
     */
    void info(String message);

    /**
     * Logs an information message.
     * 
     * @param message message
     * @param cause cause
     */
    void info(String message, Throwable cause);

    /**
     * Logs a debug message.
     * 
     * @param message message
     */
    void debug(String message);

    /**
     * Logs a debug message.
     * 
     * @param message message
     * @param cause cause
     */
    void debug(String message, Throwable cause);
}