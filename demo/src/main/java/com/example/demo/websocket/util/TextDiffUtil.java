package com.example.demo.websocket.util;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

public class TextDiffUtil {
    
    /**
     * 두 텍스트를 비교하여 변경 사항 계산

     * @param oldText 이전 텍스트
     * @param newText 새 텍스트
     * @return 변경 사항 (null이면 변경 사항 없음)
     */
    public static TextChange calculateDiff(String oldText, String newText) {
        if (oldText == null) oldText = "";
        if (newText == null) newText = "";
        
        // 텍스트가 같으면 변경 사항 없음 = null
        if (oldText.equals(newText)) {
            return null;
        }
        
        // 앞에서부터 같은 부분 찾기
        int start = 0;
        int minLength = Math.min(oldText.length(), newText.length());
        while (start < minLength && oldText.charAt(start) == newText.charAt(start)) {
            start++;
        }

        // 뒤에서부터 같은 부분 찾기
        int oldEnd = oldText.length();
        int newEnd = newText.length();
        
        while (oldEnd > start && newEnd > start 
            && oldText.charAt(oldEnd - 1) == newText.charAt(newEnd - 1)) {
            oldEnd--;
            newEnd--;
        }
        
        TextChange.Range range = new TextChange.Range(start, oldEnd); // 변경 범위
        String newTextPart = newText.substring(start, newEnd); // 새 텍스트 추출
        
        return new TextChange(range, newTextPart);
    }
    
    // 변경 사항을 나타내는 내부 클래스
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextChange {

        private Range range; // 변경 범위
        private String newText; // 새 텍스트
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Range {
            private Integer start; // 변경 시작 위치
            private Integer end; // 변경 끝 위치
        }
    }
}
