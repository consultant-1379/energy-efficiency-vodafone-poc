package com.ericsson.vodafone.poc.eee.jar.exception;

public class EnergyEfficiencyJobException extends Throwable {
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public EnergyEfficiencyJobException() {
        super();
    }

    /**
     * Exception that can be thrown managing history objects.
     * @param message : the message used to decribe the oparation that throwed the exception
     */
    public EnergyEfficiencyJobException(final String message) {
        super(message);
    }

    /**
     * Exception that can be thrown managing history objects.
     * @param message : the message used to decribe the oparation that throwed the exception
     * @param cause : the exception cause
     */
    public EnergyEfficiencyJobException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Exception that can be thrown managing history objects.
     * @param cause : the cause of the exception
     */
    public EnergyEfficiencyJobException(final Throwable cause) {
        super(cause);
    }

    /**
     * Exception that can be thrown managing history objects.
     * @param message the message used to decribe the oparation that throwed the exception
     * @param cause the exception cause
     * @param enableSuppression tenables or disables suppression
     * @param writableStackTrace enables or disables the writable stack trace
     */
    protected EnergyEfficiencyJobException(final String message, final Throwable cause,
                                           final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
