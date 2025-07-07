#!/bin/bash

# Question-Answer Service Build and Push Script
echo "Building Question-Answer Service..."

# Build the application
./gradlew build -x test

# Build Docker image
docker build -t hyundae.kr.ncr.ntruss.com/question-answer:latest .

# Push to Naver Cloud Container Registry
docker push hyundae.kr.ncr.ntruss.com/question-answer:latest

echo "Question-Answer Service build and push completed!" 