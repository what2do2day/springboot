#!/usr/bin/env python3
"""
UserAnswer API 테스트 스크립트
"""

import requests
import json
import uuid

# 서비스 URL
BASE_URL = "http://localhost:8086"

def get_jwt_token():
    """Gateway에서 JWT 토큰을 얻는 함수 (테스트용)"""
    # 실제로는 Gateway의 로그인 API를 호출해야 함
    # 여기서는 테스트용 UUID를 사용
    return str(uuid.uuid4())

def test_submit_answer():
    """답변 제출 테스트"""
    print("=== 답변 제출 테스트 ===")
    
    # 테스트 데이터
    user_id = str(uuid.uuid4())
    couple_id = str(uuid.uuid4())
    question_id = str(uuid.uuid4())
    
    headers = {
        "X-User-ID": user_id,
        "X-Couple-ID": couple_id,
        "Content-Type": "application/json"
    }
    
    data = {
        "questionId": question_id,
        "selectedOption": "1"  # 1 또는 2
    }
    
    try:
        response = requests.post(
            f"{BASE_URL}/api/user-answers",
            headers=headers,
            json=data
        )
        
        print(f"Status Code: {response.status_code}")
        print(f"Response: {response.json()}")
        
        if response.status_code == 201:
            print("✅ 답변 제출 성공!")
        else:
            print("❌ 답변 제출 실패!")
            
    except Exception as e:
        print(f"❌ 오류 발생: {e}")

def test_get_my_answers():
    """내 답변 조회 테스트"""
    print("\n=== 내 답변 조회 테스트 ===")
    
    user_id = str(uuid.uuid4())
    couple_id = str(uuid.uuid4())
    
    headers = {
        "X-User-ID": user_id,
        "X-Couple-ID": couple_id
    }
    
    try:
        response = requests.get(
            f"{BASE_URL}/api/user-answers/my-answers",
            headers=headers
        )
        
        print(f"Status Code: {response.status_code}")
        print(f"Response: {response.json()}")
        
        if response.status_code == 200:
            print("✅ 답변 조회 성공!")
        else:
            print("❌ 답변 조회 실패!")
            
    except Exception as e:
        print(f"❌ 오류 발생: {e}")

def test_invalid_option():
    """잘못된 옵션 테스트"""
    print("\n=== 잘못된 옵션 테스트 ===")
    
    user_id = str(uuid.uuid4())
    couple_id = str(uuid.uuid4())
    question_id = str(uuid.uuid4())
    
    headers = {
        "X-User-ID": user_id,
        "X-Couple-ID": couple_id,
        "Content-Type": "application/json"
    }
    
    data = {
        "questionId": question_id,
        "selectedOption": "3"  # 잘못된 옵션
    }
    
    try:
        response = requests.post(
            f"{BASE_URL}/api/user-answers",
            headers=headers,
            json=data
        )
        
        print(f"Status Code: {response.status_code}")
        print(f"Response: {response.json()}")
        
        if response.status_code == 400:
            print("✅ 잘못된 옵션 검증 성공!")
        else:
            print("❌ 잘못된 옵션 검증 실패!")
            
    except Exception as e:
        print(f"❌ 오류 발생: {e}")

if __name__ == "__main__":
    print("UserAnswer API 테스트 시작...")
    print(f"서비스 URL: {BASE_URL}")
    
    # 테스트 실행
    test_submit_answer()
    test_get_my_answers()
    test_invalid_option()
    
    print("\n테스트 완료!") 