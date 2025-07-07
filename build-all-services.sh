#!/bin/bash

# All Services Build and Push Script
echo "Starting build and push for all services..."

# Function to build and push a service
build_and_push_service() {
    local service_name=$1
    local service_dir=$2
    
    echo "=========================================="
    echo "Building and pushing $service_name service..."
    echo "=========================================="
    
    cd "$service_dir"
    
    if [ -f "build-and-push.sh" ]; then
        chmod +x build-and-push.sh
        ./build-and-push.sh
    else
        echo "Error: build-and-push.sh not found in $service_dir"
        return 1
    fi
    
    cd ..
    echo ""
}

# Build and push each service
build_and_push_service "Gateway" "gateway-service"
build_and_push_service "User-Couple" "user-couple-service"
build_and_push_service "Schedule-Meeting" "schedule-meeting-service"
build_and_push_service "Question-Answer" "question-answer-service"
build_and_push_service "LiveChatDjango" "LiveChatDjango-main"

echo "=========================================="
echo "All services build and push completed!"
echo "==========================================" 