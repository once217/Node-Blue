package com.samsa.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class InNode extends Node {
    private final List<Pipe> inputPipes = new ArrayList<>();

    protected InNode(String id) {
        this.id = id;
    }

    public void addPipe(Pipe pipe) {
        inputPipes.add(pipe);
    }

    public void removePipe(Pipe pipe) {
        inputPipes.remove(pipe);
    }

    protected List<Pipe> getPipes() {
        return Collections.unmodifiableList(inputPipes);
    }
}