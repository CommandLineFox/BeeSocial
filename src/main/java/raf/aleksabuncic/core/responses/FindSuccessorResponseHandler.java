package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

public class FindSuccessorResponseHandler extends ResponseHandler {

    public FindSuccessorResponseHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "FIND_SUCCESSOR_RESPONSE";
    }

    @Override
    public void handle(Message msg) {
        System.out.println("Received FIND_SUCCESSOR_RESPONSE: " + msg.content());
    }
}