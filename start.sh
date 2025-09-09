#!/bin/bash
# .env 파일이 있는지 확인
if [ -f .env ]; then
  # .env 파일의 변수들을 환경변수로 export
  export $(cat .env | sed 's/#.*//g' | xargs)
fi

# 기존에 실행 중인 컨테이너가 있다면 중지하고 삭제합니다.
echo "Stopping and removing old containers..."
docker-compose down

# 새로운 이미지로 빌드하고 백그라운드에서 실행합니다.
echo "Building and starting new containers..."
docker-compose up -d --build