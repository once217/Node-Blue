package com.samsa.node.inout;

import com.samsa.core.InOutNode;
import com.samsa.core.Node;
import com.samsa.core.Message;
import java.util.HashMap;
import java.util.Map;

/**
 * 메시지의 페이로드나 메타데이터를 수정하는 노드.
 * 지정된 속성에 새로운 값을 설정하여 메시지를 변경한다.
 */
public class ChangeNode extends InOutNode {
    // 변경할 속성의 키 값
    private final String propertyName;
    // 설정할 새로운 값
    private final Object newValue;
    // true면 메타데이터 변경, false면 페이로드 변경
    private final boolean isMetadata;
    
    /**
     * ChangeNode 생성자
     * @param id 노드의 고유 식별자
     * @param propertyName 변경할 속성의 키 값
     * @param newValue 설정할 새로운 값
     * @param isMetadata true면 메타데이터 변경, false면 페이로드 변경
     */
    public ChangeNode(String id, String propertyName, Object newValue, boolean isMetadata) {
        super(id);
        this.propertyName = propertyName;
        this.newValue = newValue;
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
        try {
            // 리플렉션을 사용하여 노드의 상태 필드에 접근
            java.lang.reflect.Field statusField = Node.class.getDeclaredField("status");
            statusField.setAccessible(true);
            Object currentStatus = statusField.get(this);
            
            // 노드가 실행 중이 아니면 메시지 처리하지 않음
            if (!"RUNNING".equals(currentStatus.toString())) {
                return;
            }
            
            if (isMetadata) {
                // 메타데이터 변경 로직
                Map<String, Object> newMetadata = new HashMap<>(message.getMetadata());
                newMetadata.put(propertyName, newValue);
                // 새로운 메타데이터를 가진 메시지 생성 및 전송
                emit(new Message(message.getPayload(), newMetadata));
            } else {
                // 페이로드 변경 로직
                Map<String, Object> payloadMap = new HashMap<>();
                Object payload = message.getPayload();
                
                // 기존 페이로드가 Map인 경우 데이터 복사
                if (payload instanceof Map<?, ?> map) {
                    map.forEach((key, value) -> {
                        if (key instanceof String) {
                            payloadMap.put((String) key, value);
                        }
                    });
                }
                
                // 새로운 값 설정
                payloadMap.put(propertyName, newValue);
                // 새로운 페이로드를 가진 메시지 생성 및 전송
                emit(new Message(payloadMap, message.getMetadata()));
            }
        } catch (Exception e) {
            // 리플렉션 또는 메시지 처리 중 발생한 예외 처리
            return;
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
 * metadataNode.onMessage(message1); // status:active 가 메타데이터에 추가됨
 * 
 * // 페이로드를 변경하는 ChangeNode 생성
 * ChangeNode payloadNode = new ChangeNode("change2", "name", "testName", false);
 * payloadNode.start();
 * 
 * // 페이로드 변경 테스트
 * Map<String, Object> payload = new HashMap<>();
 * payload.put("originalKey", "originalValue");
 * Message message2 = new Message(payload);
 * payloadNode.onMessage(message2); // name:testName 이 페이로드에 추가됨
 * 
 * // 노드 중지 상태 테스트
 * payloadNode.stop();
 * payloadNode.onMessage(message2); // 메시지가 처리되지 않음
 */
