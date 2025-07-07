#!/bin/bash

echo "=========================================="
echo "Deleting all services from Kubernetes"
echo "=========================================="

# Function to delete a service
delete_service() {
    local service_name=$1
    local service_dir=$2
    
    echo "Deleting $service_name..."
    
    # Delete deployment, service, and ingress
    kubectl delete -f $service_dir/deployment.yaml --ignore-not-found=true
    kubectl delete -f $service_dir/service.yaml --ignore-not-found=true
    kubectl delete -f $service_dir/ingress.yaml --ignore-not-found=true
    
    echo "$service_name deletion completed!"
    echo ""
}

# Delete all services
delete_service "Gateway" "gateway"
delete_service "User-Couple" "user-couple"
delete_service "Schedule-Meeting" "schedule-meeting"
delete_service "Question-Answer" "question-answer"
delete_service "LiveChatDjango" "livechatdjango"
delete_service "Text-AI" "text-ai"
delete_service "Recommand-Place" "recommand-place"
delete_service "Generate-Question" "generate-question"

echo "=========================================="
echo "All services deleted successfully!"
echo "==========================================" 