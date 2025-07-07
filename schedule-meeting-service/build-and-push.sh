#!/bin/bash

# Schedule-Meeting Service Build and Push Script
echo "Building Schedule-Meeting Service..."

# Build the application
./gradlew build -x test

# Build Docker image
docker build -t hyundae.kr.ncr.ntruss.com/schedule-meeting:latest .

# Push to Naver Cloud Container Registry
docker push hyundae.kr.ncr.ntruss.com/schedule-meeting:latest

echo "Schedule-Meeting Service build and push completed!" 