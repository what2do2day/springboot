#!/bin/bash

# Mission-Store Service Build and Push Script
echo "Building Mission-Store Service..."

# Build the application
./gradlew build -x test

# Build Docker image
docker build -t 624227064848.dkr.ecr.ap-northeast-2.amazonaws.com/mission-store-service:latest .

# Push to AWS ECR
docker push 624227064848.dkr.ecr.ap-northeast-2.amazonaws.com/mission-store-service:latest

echo "Mission-Store Service build and push completed!" 