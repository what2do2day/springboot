#!/bin/bash

# LiveChatDjango Service Build and Push Script
echo "Building LiveChatDjango Service..."

# Build Docker image
docker build -t hyundae.kr.ncr.ntruss.com/livechatdjango:latest .

# Push to Naver Cloud Container Registry
docker push hyundae.kr.ncr.ntruss.com/livechatdjango:latest

echo "LiveChatDjango Service build and push completed!" 