package raf.aleksabuncic.core.response;

import raf.aleksabuncic.core.responses.*;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

import java.util.HashMap;
import java.util.Map;

public class ResponseRegistry {
    private final Map<String, ResponseHandler> handlers = new HashMap<>();

    public ResponseRegistry(NodeRuntime runtime) {
        register(new AcceptHandler(runtime));
        register(new FindSuccessorHandler(runtime));
        register(new FindSuccessorResponseHandler(runtime));
        register(new FollowHandler(runtime));
        register(new GetPredecessorHandler(runtime));
        register(new ListFilesHandler(runtime));
        register(new ListFilesResponseHandler(runtime));
        register(new NotifyHandler(runtime));
        register(new PingHandler(runtime));
        register(new PongHandler(runtime));
        register(new RegisterResponseHandler(runtime));
    }

    /**
     * Registers a response handler in the registry.
     *
     * @param handler Response handler to register.
     */
    public void register(ResponseHandler handler) {
        handlers.put(handler.type(), handler);
    }

    /**
     * Returns a response handler by its type.
     *
     * @param msg Message to retrieve the handler for.
     * @return Response handler or null if not found.
     */
    public boolean handle(Message msg) {
        ResponseHandler handler = handlers.get(msg.type());
        if (handler != null) {
            handler.handle(msg);
            return true;
        }
        return false;
    }
}