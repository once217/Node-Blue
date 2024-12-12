package com.samsa.node.inout;

import com.samsa.core.InOutNode;
import com.samsa.core.Node;
import com.samsa.core.Message;
import com.samsa.core.Pipe;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 메시지의 메타데이터를 기반으로 다른 출력 파이프로 메시지를 라우팅하는 노드.
 * 메타데이터의 특정 속성값을 기준으로 여러 출력 중 하나를 선택하여 메시지를 전달한다.
 */
public class SwitchNode extends InOutNode {
    // 메타데이터에서 검사할 속성의 키 값
    private final String propertyName;
    
    /**
     * SwitchNode 생성자
     * @param id 노드의 고유 식별자
     * @param propertyName 라우팅 기준이 되는 메타데이터의 키 값
     */
    public SwitchNode(String id, String propertyName) {
        super(id);
        this.propertyName = propertyName;
    }
    
    /**
     * 메시지를 받아서 처리하는 메서드.
     * 노드가 RUNNING 상태일 때만 동작하며, 메타데이터의 특정 값을 기준으로 출력을 결정한다.
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
            
            // 메시지의 메타데이터를 안전하게 복사
            Map<String, Object> metadata = new HashMap<>(message.getMetadata());
            // 라우팅 기준이 될 값을 메타데이터에서 추출
            Object value = metadata.get(propertyName);
            
            // 사용 가능한 출력 파이프 목록 조회
            List<Pipe> outputs = getOutputPipes();
            
            // 기준값이 존재하고 출력이 여러 개인 경우 해시 기반 라우팅 수행
            if (value != null && outputs.size() > 1) {
                // 해시값을 이용해 출력 파이프 인덱스 계산
                int index = Math.abs(value.hashCode() % outputs.size());
                if (outputs.get(index).isConnected()) {
                    outputs.get(index).send(message);
                }
            } else {
                // 조건을 만족하지 않으면 기본 출력으로 전송
                emit(message);
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
 * // 기본적인 SwitchNode 생성 및 설정
 * SwitchNode switchNode = new SwitchNode("switch1", "routingKey");
 * switchNode.start();
 * 
 * // 메타데이터 기반 라우팅 테스트
 * Map<String, Object> metadata = new HashMap<>();
 * metadata.put("routingKey", "value1");
 * Message message = new Message("testPayload", metadata);
 * switchNode.onMessage(message);
 * 
 * // 기본 라우팅 테스트 (메타데이터 없는 경우)
 * Message defaultMessage = new Message("defaultPayload");
 * switchNode.onMessage(defaultMessage);
 * 
 * // 노드 중지 상태 테스트
 * switchNode.stop();
 * switchNode.onMessage(message); // 메시지가 처리되지 않음
 * commit test
 */
