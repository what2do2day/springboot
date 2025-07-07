#!/bin/bash

echo "=========================================="
echo "Docker Build Status Check"
echo "=========================================="

echo ""
echo "1. Local Docker Images:"
echo "------------------------------------------"
docker images | grep hyundae || echo "No hyundae images found locally"

echo ""
echo "2. Testing Registry Pull (checking if images are pushed):"
echo "------------------------------------------"

services=("gateway" "user-couple" "schedule-meeting" "question-answer" "livechatdjango")

for service in "${services[@]}"; do
    echo "Checking $service..."
    if docker pull hyundae.kr.ncr.ntruss.com/$service:latest > /dev/null 2>&1; then
        echo "✅ $service: SUCCESS - Image is available in registry"
    else
        echo "❌ $service: FAILED - Image not found in registry"
    fi
done

echo ""
echo "3. Build Directories Check:"
echo "------------------------------------------"

# Check if JAR files exist for Spring Boot services
spring_services=("gateway-service" "user-couple-service" "schedule-meeting-service" "question-answer-service")

for service in "${spring_services[@]}"; do
    if [ -d "$service/build/libs" ] && [ "$(ls -A $service/build/libs/*.jar 2>/dev/null)" ]; then
        echo "✅ $service: JAR files found in build/libs/"
    else
        echo "❌ $service: No JAR files found in build/libs/"
    fi
done

# Check Django service
if [ -f "LiveChatDjango-main/requirements.txt" ]; then
    echo "✅ LiveChatDjango-main: requirements.txt found"
else
    echo "❌ LiveChatDjango-main: requirements.txt not found"
fi

echo ""
echo "=========================================="
echo "Check completed!"
echo "==========================================" 