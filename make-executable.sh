#!/bin/bash

# Make all build scripts executable
echo "Making all build scripts executable..."

chmod +x gateway-service/build-and-push.sh
chmod +x user-couple-service/build-and-push.sh
chmod +x schedule-meeting-service/build-and-push.sh
chmod +x question-answer-service/build-and-push.sh
chmod +x LiveChatDjango-main/build-and-push.sh
chmod +x build-all-services.sh

echo "All scripts are now executable!" 