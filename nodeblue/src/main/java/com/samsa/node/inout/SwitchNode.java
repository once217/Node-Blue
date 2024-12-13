package com.samsa.node.inout;

import com.samsa.core.InOutNode;
import com.samsa.core.Message;
import com.samsa.core.Pipe;
import com.samsa.core.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 메시지의 메타데이터를 기반으로 다른 출력 파이프로 메시지를 라우팅하는 노드.
 * 메타데이터의 특정 속성값을 기준으로 여러 출력 중 하나를 선택하여 메시지를 전달한다.
 */
public class SwitchNode extends InOutNode {
    /** 메타데이터에서 검사할 속성의 키 값 */
    private final String property;

    /**
     * String UUID로 SwitchNode를 생성한다.
     * 
     * @param uuid     노드의 고유 식별자 (UUID 문자열)
     * @param property 라우팅 기준이 되는 메타데이터의 키 값
     * @throws IllegalArgumentException uuid가 유효하지 않은 경우
     */
    public SwitchNode(String uuid, String property) {
        super(uuid);
        this.property = property;
    }

    /**
     * UUID 객체로 SwitchNode를 생성한다.
     * 
     * @param id       노드의 고유 식별자 (UUID 객체)
     * @param property 라우팅 기준이 되는 메타데이터의 키 값
     */
    public SwitchNode(UUID id, String property) {
        super(id);
        this.property = property;
    }

    /**
     * 메시지를 받아서 처리하는 메서드.
     * 노드가 RUNNING 상태일 때만 동작하며, 메타데이터의 특정 값을 기준으로 출력을 결정한다.
     * 
     * @param message 처리할 메시지 객체
     */
    @Override
    public void onMessage(Message message) {
        if (status != NodeStatus.RUNNING) {
            return;
        }

        // 메타데이터에서 라우팅 기준값 추출
        Map<String, Object> metadata = new HashMap<>(message.getMetadata());
        Object value = metadata.get(property);

        // 출력 파이프 선택 및 메시지 전송
        List<Pipe> outputs = getOutputPipes();
        if (value != null && outputs.size() > 1) {
            int index = Math.abs(value.hashCode() % outputs.size());
            if (outputs.get(index).isConnected()) {
                outputs.get(index).send(message);
            }
        } else {
            emit(message);
        }
    }
}

/*
 * 사용 예시:
 * 
 * // SwitchNode 생성 및 시작
 * SwitchNode switchNode = new SwitchNode(UUID.randomUUID(), "routingKey");
 * switchNode.start();
 * 
 * // 출력 파이프 설정
 * Pipe pipe1 = new Pipe("pipe1");
 * Pipe pipe2 = new Pipe("pipe2");
 * switchNode.addOutputPipe(pipe1);
 * switchNode.addOutputPipe(pipe2);
 * 
 * // 테스트 메시지 생성 및 전송
 * Map<String, Object> metadata = new HashMap<>();
 * metadata.put("routingKey", "value1");
 * Message message = new Message("testPayload", metadata);
 * 
 * // 메시지 처리 (routingKey 값에 따라 pipe1 또는 pipe2로 전송됨)
 * switchNode.onMessage(message);
 */
