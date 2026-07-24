package com.symphony.applaunch.exception;

import org.springframework.http.HttpStatus;

public class ApplicationException extends RuntimeException {

    private static final long serialVersionUID = 4265758284484258031L;

    private String message;/* NOSONAR */

    private boolean logged;/* NOSONAR */

    private int errorCodeId;/* NOSONAR */

    private HttpStatus status;/* NOSONAR */

    public ApplicationException(final int errorCodeId) {
        this.errorCodeId = errorCodeId;
    }

    public ApplicationException(final String message, final int errorCodeId) {
        this.setMessage(message);
        this.errorCodeId = errorCodeId;
    }

    public ApplicationException(final String message, final int errorCodeId, final Throwable throwable) {
        super(throwable);
        this.errorCodeId = errorCodeId;
        this.setMessage(message);
    }

    public ApplicationException(String message, int errorCodeId, HttpStatus status) {
        super();
        this.message = message;
        this.errorCodeId = errorCodeId;
        this.status = status;
    }

    /**
     * @return the errorCodeId
     */
    public int getErrorCodeId() {
        return errorCodeId;
    }

    /**
     * @param errorCodeId the errorCodeId to set
     */
    public void setErrorCodeId(int errorCodeId) {
        this.errorCodeId = errorCodeId;
    }

    /**
     * @return the message
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the logged
     */
    public boolean isLogged() {
        return logged;
    }

    /**
     * @param logged the logged to set
     */
    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    /**
     * @return the status
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(HttpStatus status) {
        this.status = status;
    }

}
