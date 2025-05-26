package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

/**
 * Handles what happens when a token request is sent
 */
public class TokenRequestHandler extends ResponseHandler {
    public TokenRequestHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "TOKEN_REQUEST";
    }

    @Override
    public void handle(Message msg) {
        int senderId = msg.senderPort();
        int receivedReqNum = Integer.parseInt(msg.content());

        int currentReqNum = runtime.getRN().getOrDefault(senderId, 0);
        runtime.getRN().put(senderId, Math.max(currentReqNum, receivedReqNum));

        // Ako imamo token i ne koristimo kritičnu sekciju
        if (runtime.hasToken() && !runtime.isRequestingCS()) {
            int ln = runtime.getToken().LN.getOrDefault(senderId, 0);
            int rn = runtime.getRN().getOrDefault(senderId, 0);

            if (rn > ln && !runtime.getToken().getQueue().contains(senderId)) {
                runtime.getToken().getQueue().add(senderId);
                runtime.exitCriticalSection();
            }
        }
    }
}
