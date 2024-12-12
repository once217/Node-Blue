package com.samsa.node.inout;

import com.samsa.core.InOutNode;
import com.samsa.core.Message;
import com.samsa.core.Pipe;
import com.samsa.core.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 메시지의 메타데이터를 기반으로 다른 출력 파이프로 메시지를 라우팅하는 노드.
 * Node-RED의 switch 노드와 유사한 기능을 수행하며,
 * 각 프로퍼티의 존재 여부에 따라 해당 출력으로 메시지를 전달한다.
 */
public class SwitchNode2 extends InOutNode {
    /** 라우팅 규칙을 저장하는 리스트 */
    private final List<Property> properties = new ArrayList<>();
    
    /**
     * 라우팅 규칙을 정의하는 내부 클래스.
     * 각 규칙은 이름과 검사할 메타데이터 속성으로 구성된다.
     */
    public static class Property {
        private final String name;        // 규칙의 식별 이름
        private final String property;    // 검사할 메타데이터 속성 키
        
        /**
         * Property 생성자
         * @param name 규칙의 식별 이름
         * @param property 검사할 메타데이터 속성 키
         */
        public Property(String name, String property) {
            this.name = name;
            this.property = property;
        }
    }
    
    /**
     * SwitchNode 생성자
     * @param id 노드의 고유 식별자
     */
    public SwitchNode2(String id) {
        super(id);
    }
    
    /**
     * 새로운 라우팅 규칙을 추가한다.
     * @param name 규칙의 식별 이름
     * @param property 검사할 메타데이터 속성 키
     */
    public void addProperty(String name, String property) {
        properties.add(new Property(name, property));
    }
    
    /**
     * 메시지를 받아서 처리하는 메서드.
     * 각 프로퍼티의 존재 여부를 순차적으로 검사하여 조건을 만족하는 출력으로 메시지를 전달한다.
     * 노드가 RUNNING 상태일 때만 동작한다.
     * 
     * @param message 처리할 메시지 객체
     */
    @Override
    public void onMessage(Message message) {
        // 노드가 실행 중이 아니면 메시지 처리하지 않음
        if (status != NodeStatus.RUNNING) {
            return;
        }
        
        // 메시지의 메타데이터 추출
        Map<String, Object> metadata = message.getMetadata();
        // 사용 가능한 출력 파이프 목록 조회
        List<Pipe> outputs = getOutputPipes();
        
        // 각 프로퍼티와 출력 파이프를 순차적으로 검사
        for (int i = 0; i < properties.size() && i < outputs.size(); i++) {
            Property prop = properties.get(i);
            // 메타데이터에서 해당 프로퍼티 값 확인
            Object value = metadata.get(prop.property);
            
            // 프로퍼티가 존재하면 해당 출력으로 메시지 전송
            if (value != null) {
                Pipe output = outputs.get(i);
                if (output.isConnected()) {
                    output.send(message);
                }
            }
        }
    }
}

/*
// 사용 예시
SwitchNode switchNode = new SwitchNode("switch1");

// 프로퍼티 추가
switchNode.addProperty("활성 상태 체크", "status");
switchNode.addProperty("나이 체크", "age");
switchNode.addProperty("이메일 체크", "email");

// 메시지 처리
Map<String, Object> metadata = new HashMap<>();
metadata.put("status", "active");
metadata.put("age", 25);
metadata.put("email", "test@example.com");

Message message = new Message("payload", metadata);
switchNode.onMessage(message);
*/