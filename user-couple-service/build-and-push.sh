#!/bin/bash

# User-Couple Service Build and Push Script
echo "Building User-Couple Service..."

# Build the application
./gradlew build -x test

# Build Docker image
docker build -t hyundae.kr.ncr.ntruss.com/user-couple:latest .

# Push to Naver Cloud Container Registry
docker push hyundae.kr.ncr.ntruss.com/user-couple:latest

echo "User-Couple Service build and push completed!" 