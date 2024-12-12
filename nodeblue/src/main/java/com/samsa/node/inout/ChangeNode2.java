package com.samsa.node.inout;

import com.samsa.core.InOutNode;
import com.samsa.core.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * 메시지의 페이로드나 메타데이터의 특정 속성값을 변경하는 노드.
 * Node-RED의 change 노드와 유사한 기능을 수행하며,
 * 지정된 속성에 새로운 값을 설정하여 메시지를 변경한다.
 */
public class ChangeNode2 extends InOutNode {
    // 변경할 속성의 키 값
    private final String property;
    // 속성에 설정할 새로운 값
    private final Object value;
    // true이면 메타데이터 변경, false이면 페이로드 변경
    private final boolean isMetadata;
    
    /**
     * ChangeNode 생성자
     * @param id 노드의 고유 식별자
     * @param property 변경할 속성의 키 값
     * @param value 설정할 새로운 값
     * @param isMetadata true면 메타데이터 변경, false면 페이로드 변경
     */
    public ChangeNode2(String id, String property, Object value, boolean isMetadata) {
        super(id);
        this.property = property;
        this.value = value;
        this.isMetadata = isMetadata;
    }
    
    /**
     * 메시지를 받아서 처리하는 메서드.
     * 노드가 RUNNING 상태일 때만 동작하며, 설정에 따라 메타데이터나 페이로드를 수정한다.
     * Message 객체의 불변성을 유지하기 위해 새로운 Message 객체를 생성하여 전달한다.
     * 
     * @param message 처리할 메시지 객체
     */
    @Override
    public void onMessage(Message message) {
        // 노드가 실행 중이 아니면 메시지 처리하지 않음
        if (status != NodeStatus.RUNNING) {
            return;
        }
        
        if (isMetadata) {
            // 메타데이터 변경 로직
            // 기존 메타데이터를 복사하여 새로운 Map 생성
            Map<String, Object> newMetadata = new HashMap<>(message.getMetadata());
            // 지정된 속성에 새로운 값 설정
            newMetadata.put(property, value);
            // 새로운 메타데이터를 가진 메시지 생성 및 전송
            emit(new Message(message.getPayload(), newMetadata));
        } else {
            // 페이로드 변경 로직
            // 새로운 페이로드 Map 생성
            Map<String, Object> newPayload = new HashMap<>();
            // 기존 페이로드가 Map인 경우 데이터 복사
            if (message.getPayload() instanceof Map) {
                newPayload.putAll((Map) message.getPayload());
            }
            // 지정된 속성에 새로운 값 설정
            newPayload.put(property, value);
            // 새로운 페이로드를 가진 메시지 생성 및 전송
            emit(new Message(newPayload, message.getMetadata()));
        }
    }
}

/*
 * 사용 예시
 * 
 * // 메타데이터를 변경하는 ChangeNode 생성
 * ChangeNode metadataNode = new ChangeNode("change1", "status", "active", true);
 * metadataNode.start();
 * 
 * // 메타데이터 변경 테스트
 * Map<String, Object> metadata = new HashMap<>();
 * metadata.put("originalKey", "originalValue");
 * Message message1 = new Message("testPayload", metadata);
 * metadataNode.onMessage(message1);
 * // 결과: message1의 메타데이터에 status:active 추가됨
 * 
 * // 페이로드를 변경하는 ChangeNode 생성
 * ChangeNode payloadNode = new ChangeNode("change2", "name", "testName", false);
 * payloadNode.start();
 * 
 * // 페이로드 변경 테스트
 * Map<String, Object> payload = new HashMap<>();
 * payload.put("id", 123);
 * Message message2 = new Message(payload);
 * payloadNode.onMessage(message2);
 * // 결과: message2의 페이로드에 name:testName 추가됨
 */