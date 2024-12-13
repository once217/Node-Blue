package com.samsa.node.inout;

import com.samsa.core.InOutNode;
import com.samsa.core.Node;
import com.samsa.core.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * 메시지의 페이로드나 메타데이터를 수정하는 노드.
 * 지정된 속성에 새로운 값을 설정하여 메시지를 변경한다.
 * 메시지의 불변성을 유지하기 위해 항상 새로운 Message 객체를 생성한다.
 */
@Slf4j
public class ChangeNode extends InOutNode {
    /** 변경하려는 속성의 키 값 */
    private final String propertyName;

    /** 속성에 설정할 새로운 값 */
    private final Object newValue;

    /** 변경 대상 지정 플래그 (true: 메타데이터, false: 페이로드) */
    private final boolean isMetadata;

    /**
     * String UUID로 ChangeNode를 생성한다.
     * 생성 시 노드의 상태는 CREATED이며, 명시적으로 start()를 호출해야 메시지를 처리한다.
     * 
     * @param uuid         노드의 고유 식별자 (UUID 문자열)
     * @param propertyName 변경할 속성의 키 값
     * @param newValue     설정할 새로운 값
     * @param isMetadata   true면 메타데이터 변경, false면 페이로드 변경
     * @throws IllegalArgumentException uuid가 유효하지 않은 경우
     */
    public ChangeNode(String uuid, String propertyName, Object newValue, boolean isMetadata) {
        super(uuid);
        this.propertyName = propertyName;
        this.newValue = newValue;
        this.isMetadata = isMetadata;
        log.info("ChangeNode[{}] created - property: {}, target: {}",
                getId(), propertyName, isMetadata ? "metadata" : "payload");
    }

    /**
     * UUID 객체로 ChangeNode를 생성한다.
     * 생성 시 노드의 상태는 CREATED이며, 명시적으로 start()를 호출해야 메시지를 처리한다.
     * 
     * @param id           노드의 고유 식별자 (UUID 객체)
     * @param propertyName 변경할 속성의 키 값
     * @param newValue     설정할 새로운 값
     * @param isMetadata   true면 메타데이터 변경, false면 페이로드 변경
     */
    public ChangeNode(UUID id, String propertyName, Object newValue, boolean isMetadata) {
        super(id);
        this.propertyName = propertyName;
        this.newValue = newValue;
        this.isMetadata = isMetadata;
        log.info("ChangeNode[{}] created - property: {}, target: {}",
                getId(), propertyName, isMetadata ? "metadata" : "payload");
    }

    /**
     * 메시지를 받아서 처리하는 메서드.
     * 노드가 RUNNING 상태일 때만 동작하며, 설정에 따라 메타데이터나 페이로드를 수정한다.
     * 메시지의 불변성을 유지하기 위해 새로운 Message 객체를 생성하여 전달한다.
     * 
     * @param message 처리할 메시지 객체
     */
    @Override
    public void onMessage(Message message) {
        try {
            /** 리플렉션을 사용하여 노드의 상태 필드에 접근 */
            java.lang.reflect.Field statusField = Node.class.getDeclaredField("status");
            statusField.setAccessible(true);
            Object currentStatus = statusField.get(this);

            /** 노드가 실행 중이 아니면 메시지 처리하지 않음 */
            if (!"RUNNING".equals(currentStatus.toString())) {
                log.debug("ChangeNode[{}] skipped message: not in RUNNING state", getId());
                return;
            }

            if (isMetadata) {
                /** 메타데이터 변경 로직 */
                log.debug("ChangeNode[{}] modifying metadata - property: {}, value: {}",
                        getId(), propertyName, newValue);
                /** 기존 메타데이터를 복사하여 새로운 Map 생성 */
                Map<String, Object> newMetadata = new HashMap<>(message.getMetadata());
                /** 지정된 속성에 새로운 값 설정 */
                newMetadata.put(propertyName, newValue);
                /** 새로운 메타데이터를 가진 메시지 생성 및 전송 */
                emit(new Message(message.getPayload(), newMetadata));
                log.info("ChangeNode[{}] metadata modified successfully", getId());
            } else {
                /** 페이로드 변경 로직 */
                log.debug("ChangeNode[{}] modifying payload - property: {}, value: {}",
                        getId(), propertyName, newValue);
                Map<String, Object> payloadMap = new HashMap<>();
                Object payload = message.getPayload();

                /** 기존 페이로드가 Map인 경우 데이터 복사 */
                if (payload instanceof Map<?, ?> map) {
                    map.forEach((key, value) -> {
                        if (key instanceof String) {
                            payloadMap.put((String) key, value);
                        }
                    });
                }

                /** 새로운 값 설정 */
                payloadMap.put(propertyName, newValue);
                /** 새로운 페이로드를 가진 메시지 생성 및 전송 */
                emit(new Message(payloadMap, message.getMetadata()));
                log.info("ChangeNode[{}] payload modified successfully", getId());
            }
        } catch (Exception e) {
            /** 예외 발생 시 로그 기록 및 에러 처리 */
            log.error("Error in ChangeNode[{}]: {}", getId(), e.getMessage());
            handleError(e);
            return;
        }
    }
}

/**
 * 사용 예시:
 * 
 * /** ChangeNode 생성 및 시작
 * UUID nodeId = UUID.randomUUID();
 * ChangeNode metadataNode = new ChangeNode(nodeId, "status", "active", true);
 * metadataNode.start();
 * log.info("Created and started ChangeNode with ID: {}", nodeId);
 * 
 * /** 메타데이터 변경 테스트
 * Map<String, Object> metadata = new HashMap<>();
 * metadata.put("originalKey", "originalValue");
 * Message message1 = new Message("testPayload", metadata);
 * log.debug("Created test message with metadata: {}", metadata);
 * metadataNode.onMessage(message1);
 * 
 * /** 페이로드 변경 테스트
 * UUID payloadNodeId = UUID.randomUUID();
 * ChangeNode payloadNode = new ChangeNode(payloadNodeId, "name", "testName",
 * false);
 * payloadNode.start();
 * 
 * Map<String, Object> payload = new HashMap<>();
 * payload.put("originalKey", "originalValue");
 * Message message2 = new Message(payload);
 * log.debug("Created test message with payload: {}", payload);
 * payloadNode.onMessage(message2);
 */
