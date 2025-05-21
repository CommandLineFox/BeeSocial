package raf.aleksabuncic.core.response;

import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

public abstract class ResponseHandler {
    protected final NodeRuntime runtime;

    public ResponseHandler(NodeRuntime runtime) {
        this.runtime = runtime;
    }

    /**
     * Returns the type of the response.
     *
     * @return Type of the response.
     */
    public abstract String type();

    /**
     * Handles a single response.
     *
     * @param msg Message to handle.
     */
    public abstract void handle(Message msg);
}
