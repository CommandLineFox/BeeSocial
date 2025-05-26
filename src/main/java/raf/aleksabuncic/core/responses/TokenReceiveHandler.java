package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Token;
import raf.aleksabuncic.util.Utils;

/**
 * Handles what happens when a token response is sent
 */
public class TokenReceiveHandler extends ResponseHandler {
    public TokenReceiveHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "TOKEN";
    }

    @Override
    public void handle(Message msg) {
        Token token = Utils.deserializeToken(msg.content());
        runtime.setToken(token);

        synchronized (runtime) {
            runtime.notifyAll();
        }

        System.out.println("Received token from " + msg.senderPort());
    }
}
