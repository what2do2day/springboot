#!/bin/bash

echo "=========================================="
echo "Deploying all services to Kubernetes"
echo "=========================================="

# Function to deploy a service
deploy_service() {
    local service_name=$1
    local service_dir=$2
    
    echo "Deploying $service_name..."
    
    # Apply deployment
    kubectl apply -f $service_dir/deployment.yaml
    kubectl apply -f $service_dir/service.yaml
    kubectl apply -f $service_dir/ingress.yaml
    
    echo "$service_name deployment completed!"
    echo ""
}

# Deploy all services
deploy_service "Gateway" "gateway"
deploy_service "User-Couple" "user-couple"
deploy_service "Schedule-Meeting" "schedule-meeting"
deploy_service "Question-Answer" "question-answer"
deploy_service "LiveChatDjango" "livechatdjango"
deploy_service "Text-AI" "text-ai"
deploy_service "Recommand-Place" "recommand-place"
deploy_service "Generate-Question" "generate-question"

echo "=========================================="
echo "All services deployed successfully!"
echo "=========================================="

# Show deployment status
echo ""
echo "Checking deployment status..."
kubectl get deployments
echo ""
echo "Checking services..."
kubectl get services
echo ""
echo "Checking ingress..."
kubectl get ingress 