#!/bin/bash

# Gateway Service Build and Push Script
echo "Building Gateway Service..."

# Build the application
./gradlew build -x test

# Build Docker image
docker build -t hyundae.kr.ncr.ntruss.com/gateway:latest .

# Push to Naver Cloud Container Registry
docker push hyundae.kr.ncr.ntruss.com/gateway:latest

echo "Gateway Service build and push completed!" 