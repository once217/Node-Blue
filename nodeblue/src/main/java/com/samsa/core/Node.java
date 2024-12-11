package com.samsa.core;

import lombok.extern.slf4j.Slf4j;

// 노드 상태 열거형
enum NodeStatus {
    CREATED, // 생성됨
    RUNNING, // 실행 중
    STOPPED, // 중지됨
    ERROR // 에러 상태

}

@Slf4j
public abstract class Node {

    protected String id;
    protected NodeStatus status = NodeStatus.CREATED;

    // 필수 구현 메소드
    public abstract void onMessage(Message message);
    
    // 공통 구현 메소드들
    public void start() {
        status = NodeStatus.RUNNING;
        log.info("Node[{}] started", id);
    }

    public void stop() {
        status = NodeStatus.STOPPED;
        log.info("Node[{}] stopped", id);
    }

    public void handleError(Throwable error) {
        status = NodeStatus.ERROR;
        log.error("Error in Node[{}]: ", id, error);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

