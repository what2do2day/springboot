# Kubernetes 배포 가이드

## 개요
이 디렉토리에는 각 마이크로서비스를 Kubernetes에 배포하기 위한 YAML 파일들이 포함되어 있습니다.

## 서비스 목록
- **Gateway Service**: `gateway.49.50.131.82:8080`
- **User-Couple Service**: `user-couple.49.50.131.82:8081`
- **Schedule-Meeting Service**: `schedule-meeting.49.50.131.82:8082`
- **Question-Answer Service**: `question-answer.49.50.131.82:8086`
- **LiveChatDjango Service**: `livechatdjango.49.50.131.82:8000`
- **Text-AI Service**: `49.50.131.82:8003/docs`
- **Recommand-Place Service**: `49.50.131.82:8002/docs`
- **Generate-Question Service**: `49.50.131.82:8001/docs`

## 디렉토리 구조
```
k8s/
├── gateway/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── ingress.yaml
├── user-couple/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── ingress.yaml
├── schedule-meeting/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── ingress.yaml
├── question-answer/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── ingress.yaml
├── livechatdjango/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── ingress.yaml
├── text-ai/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── ingress.yaml
├── recommand-place/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── ingress.yaml
├── generate-question/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── ingress.yaml
├── deploy-all.sh
├── delete-all.sh
└── README.md
```

## 사용 방법

### 1. 모든 서비스 한 번에 배포
```bash
cd k8s
chmod +x deploy-all.sh
./deploy-all.sh
```

### 2. 개별 서비스 배포
```bash
# Gateway 서비스만 배포
kubectl apply -f gateway/deployment.yaml
kubectl apply -f gateway/service.yaml
kubectl apply -f gateway/ingress.yaml

# 다른 서비스도 동일하게 적용
```

### 3. 배포 상태 확인
```bash
# 모든 리소스 상태 확인
kubectl get all

# Deployment 상태 확인
kubectl get deployments

# Service 상태 확인
kubectl get services

# Ingress 상태 확인
kubectl get ingress

# Pod 상태 확인
kubectl get pods
```

### 4. 모든 서비스 삭제
```bash
cd k8s
chmod +x delete-all.sh
./delete-all.sh
```

### 5. 개별 서비스 삭제
```bash
# Gateway 서비스만 삭제
kubectl delete -f gateway/deployment.yaml
kubectl delete -f gateway/service.yaml
kubectl delete -f gateway/ingress.yaml
```

## 접근 방법
배포 완료 후 다음 URL로 각 서비스에 접근할 수 있습니다:

- Gateway: `http://gateway.49.50.131.82`
- User-Couple: `http://user-couple.49.50.131.82`
- Schedule-Meeting: `http://schedule-meeting.49.50.131.82`
- Question-Answer: `http://question-answer.49.50.131.82`
- LiveChatDjango: `http://livechatdjango.49.50.131.82`
- Text-AI: `http://49.50.131.82:8003/docs`
- Recommand-Place: `http://49.50.131.82:8002/docs`
- Generate-Question: `http://49.50.131.82:8001/docs`

## 주의사항
1. Kubernetes 클러스터가 실행 중이어야 합니다.
2. Docker 이미지가 `hyundae.kr.ncr.ntruss.com` 레지스트리에 push되어 있어야 합니다.
3. Ingress Controller가 클러스터에 설치되어 있어야 합니다.
4. 도메인 `49.50.131.82`에 대한 DNS 설정이 필요할 수 있습니다.

## 문제 해결
1. **Pod가 시작되지 않는 경우**: `kubectl describe pod <pod-name>`으로 상세 로그 확인
2. **서비스에 접근할 수 없는 경우**: `kubectl get ingress`로 Ingress 상태 확인
3. **이미지 pull 오류**: Docker 레지스트리 로그인 상태 확인 