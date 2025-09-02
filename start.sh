#!/bin/bash

# .env 파일이 있는지 확인
if [ -f .env ]; then
  # .env 파일의 변수들을 환경변수로 export
  export $(cat .env | sed 's/#.*//g' | xargs)
fi

# docker-compose를 --build 옵션과 함께 실행
docker-compose up -d --build