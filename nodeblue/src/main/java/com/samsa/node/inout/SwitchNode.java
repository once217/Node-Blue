package com.samsa.node.inout;

import com.samsa.core.InOutNode;
import com.samsa.core.Node;
import com.samsa.core.Message;
import com.samsa.core.Pipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * 메시지의 메타데이터를 기반으로 다른 출력 파이프로 메시지를 라우팅하는 노드.
 * 메타데이터의 특정 속성값을 기준으로 여러 출력 중 하나를 선택하여 메시지를 전달한다.
 */
@Slf4j
public class SwitchNode extends InOutNode {
    /** 메타데이터에서 검사할 속성의 키 값 */
    private final String propertyName;

    /**
     * String UUID로 SwitchNode를 생성한다.
     * 
     * @param uuid         노드의 고유 식별자 (UUID 문자열)
     * @param propertyName 라우팅 기준이 되는 메타데이터의 키 값
     * @throws IllegalArgumentException uuid가 유효하지 않은 경우
     */
    public SwitchNode(String uuid, String propertyName) {
        super(uuid);
        this.propertyName = propertyName;
        log.info("SwitchNode[{}] created with property: {}", getId(), propertyName);
    }

    /**
     * UUID 객체로 SwitchNode를 생성한다.
     * 
     * @param id           노드의 고유 식별자 (UUID 객체)
     * @param propertyName 라우팅 기준이 되는 메타데이터의 키 값
     */
    public SwitchNode(UUID id, String propertyName) {
        super(id);
        this.propertyName = propertyName;
        log.info("SwitchNode[{}] created with property: {}", getId(), propertyName);
    }

    @Override
    public void onMessage(Message message) {
        try {
            /** 리플렉션을 사용하여 노드의 상태 필드에 접근 */
            java.lang.reflect.Field statusField = Node.class.getDeclaredField("status");
            statusField.setAccessible(true);
            Object currentStatus = statusField.get(this);

            /** 노드가 실행 중이 아니면 메시지 처리하지 않음 */
            if (!"RUNNING".equals(currentStatus.toString())) {
                log.debug("SwitchNode[{}] skipped message: not in RUNNING state", getId());
                return;
            }

            /** 메시지의 메타데이터를 안전하게 복사 */
            Map<String, Object> metadata = new HashMap<>(message.getMetadata());
            Object value = metadata.get(propertyName);
            log.debug("SwitchNode[{}] processing message with property value: {}", getId(), value);

            /** 출력 파이프 목록 조회 */
            List<Pipe> outputs = getOutputPipes();
            if (value != null && outputs.size() > 1) {
                // 해시값을 이용해 출력 파이프 인덱스 계산
                int index = Math.abs(value.hashCode() % outputs.size());
                if (outputs.get(index).isConnected()) {
                    log.info("SwitchNode[{}] routing message to output pipe at index: {}", getId(), index);
                    outputs.get(index).send(message);
                } else {
                    log.warn("SwitchNode[{}] selected output pipe at index {} is not connected", getId(), index);
                }
            } else {
                log.info("SwitchNode[{}] using default routing", getId());
                emit(message);
            }
        } catch (Exception e) {
            log.error("Error in SwitchNode[{}]: {}", getId(), e.getMessage());
            handleError(e);
            return;
        }
    }
}

/**
 * 사용 예시:
 * 
 * // SwitchNode 생성 및 시작
 * UUID nodeId = UUID.randomUUID();
 * SwitchNode switchNode = new SwitchNode(nodeId, "routingKey");
 * switchNode.start();
 * log.info("Created and started SwitchNode with ID: {}", nodeId);
 * 
 * // 출력 파이프 설정
 * Pipe pipe1 = new Pipe("pipe1");
 * Pipe pipe2 = new Pipe("pipe2");
 * switchNode.addOutputPipe(pipe1);
 * switchNode.addOutputPipe(pipe2);
 * log.debug("Added output pipes to SwitchNode");
 * 
 * // 테스트 메시지 생성 및 전송
 * Map<String, Object> metadata = new HashMap<>();
 * metadata.put("routingKey", "value1");
 * Message message = new Message("testPayload", metadata);
 * log.debug("Created test message with routingKey: {}",
 * metadata.get("routingKey"));
 * 
 * // 메시지 처리
 * switchNode.onMessage(message);
 */
