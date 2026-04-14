#!/bin/bash

cd /home/coze/publish/backend

echo "Starting Media Publish Backend..."
echo "Backend will be available at: http://localhost:8080"
echo "H2 Console available at: http://localhost:8080/h2-console"
echo ""

mvn spring-boot:run
