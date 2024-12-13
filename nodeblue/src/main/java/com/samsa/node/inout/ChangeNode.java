package com.samsa.node.inout;

import com.samsa.core.InOutNode;
import com.samsa.core.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 메시지의 페이로드나 메타데이터의 특정 속성값을 변경하는 노드.
 * 지정된 속성에 새로운 값을 설정하여 메시지를 변경한다.
 */
public class ChangeNode extends InOutNode {
    /** 변경할 속성의 키 값 */
    private final String property;
    /** 속성에 설정할 새로운 값 */
    private final Object value;
    /** true이면 메타데이터 변경, false이면 페이로드 변경 */
    private final boolean isMetadata;

    /**
     * String UUID로 ChangeNode를 생성한다.
     * 
     * @param uuid       노드의 고유 식별자 (UUID 문자열)
     * @param property   변경할 속성의 키 값
     * @param value      설정할 새로운 값
     * @param isMetadata true면 메타데이터 변경, false면 페이로드 변경
     * @throws IllegalArgumentException uuid가 유효하지 않은 경우
     */
    public ChangeNode(String uuid, String property, Object value, boolean isMetadata) {
        /* 부모 클래스의 생성자를 호출하여 uuid 값을 전달 */
        super(uuid);
        this.property = property;
        this.value = value;
        this.isMetadata = isMetadata;
    }

    /**
     * UUID 객체로 ChangeNode를 생성한다.
     * 
     * @param id         노드의 고유 식별자 (UUID 객체)
     * @param property   변경할 속성의 키 값
     * @param value      설정할 새로운 값
     * @param isMetadata true면 메타데이터 변경, false면 페이로드 변경
     */
    public ChangeNode(UUID id, String property, Object value, boolean isMetadata) {
        super(id);
        this.property = property;
        this.value = value;
        this.isMetadata = isMetadata;
    }

    /**
     * 메시지를 받아서 처리하는 메서드.
     * 노드가 RUNNING 상태일 때만 동작하며, 설정에 따라 메타데이터나 페이로드를 수정한다.
     * 
     * @param message 처리할 메시지 객체
     */
    @Override
    public void onMessage(Message message) {
        if (status != NodeStatus.RUNNING) {
            return;
        }

        if (isMetadata) {
            // 메타데이터 변경
            Map<String, Object> newMetadata = new HashMap<>(message.getMetadata());
            newMetadata.put(property, value);
            emit(new Message(message.getPayload(), newMetadata));
        } else {
            // 페이로드 변경
            Map<String, Object> newPayload = new HashMap<>();
            if (message.getPayload() instanceof Map) {
                newPayload.putAll((Map) message.getPayload());
            }
            newPayload.put(property, value);
            emit(new Message(newPayload, message.getMetadata()));
        }
    }
}

/*
 * 사용 예시:
 * 
 * // 메타데이터를 변경하는 ChangeNode 생성 및 시작
 * ChangeNode metadataNode = new ChangeNode(
 * UUID.randomUUID(),
 * "status",
 * "active",
 * true
 * );
 * metadataNode.start();
 * 
 * // 출력 파이프 설정
 * Pipe outputPipe = new Pipe("pipe1");
 * metadataNode.addOutputPipe(outputPipe);
 * 
 * // 테스트 메시지 생성 및 전송
 * Map<String, Object> metadata = new HashMap<>();
 * metadata.put("originalKey", "originalValue");
 * Message message = new Message("testPayload", metadata);
 * 
 * // 메시지 처리 (메타데이터에 status:active 추가됨)
 * metadataNode.onMessage(message);
 */
