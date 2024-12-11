package com.samsa.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class InOutNode extends Node {
    private final List<Pipe> inputPipes = new ArrayList<>();
    private final List<Pipe> outputPipes = new ArrayList<>();

    protected InOutNode(String id) {
        this.id = id;
    }

    public void emit(Message message) {
    for (Pipe pipe : getOutputPipes()) {
        if (pipe.isConnected()) {
            pipe.send(message);
        }
    }
}

    public void addInputPipe(Pipe pipe) {
        inputPipes.add(pipe);
    }

    public void addOutputPipe(Pipe pipe) {
        outputPipes.add(pipe);
    }

    public void removeInputPipe(Pipe pipe) {
        inputPipes.remove(pipe);
    }

    public void removeOutputPipe(Pipe pipe) {
        outputPipes.remove(pipe);
    }

    protected List<Pipe> getInputPipes() {
        return Collections.unmodifiableList(inputPipes);
    }

    protected List<Pipe> getOutputPipes() {
        return Collections.unmodifiableList(outputPipes);
    }


}