package com.samsa.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Message {
   private final String id;
   private final Object payload;
   private final Map<String, Object> metadata;

   // 기본 생성자 - 메타데이터 없이
   public Message(Object payload) {
       this.id = UUID.randomUUID().toString();
       this.payload = payload;
       this.metadata = new HashMap<>();
   }

   // 메타데이터 포함 생성자
   public Message(Object payload, Map<String, Object> metadata) {
       this.id = UUID.randomUUID().toString();
       this.payload = payload;
       this.metadata = new HashMap<>(metadata);  // 메타데이터 복사
   }

   // 전체 지정 생성자 
   public Message(String id, Object payload, Map<String, Object> metadata) {
       this.id = id;
       this.payload = payload;
       this.metadata = new HashMap<>(metadata);
   }


   public String getId() {
       return id;
   }


   public Object getPayload() {
       return payload;
   }

   public Map<String, Object> getMetadata() {
       return metadata;
   }
}