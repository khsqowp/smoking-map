const nextJest = require('next/jest');

// next/jest를 사용하여 Jest 설정 생성
const createJestConfig = nextJest({
  // 테스트 환경에서 next.config.js와 .env 파일을 로드할 경로 제공
  dir: './',
});

// Jest에 전달할 커스텀 설정
const customJestConfig = {
  // 각 테스트 전에 실행할 파일 설정
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
  // 테스트 환경 설정 (브라우저와 유사한 환경을 위해 jsdom 사용)
  testEnvironment: 'jest-environment-jsdom',
  // TypeScript 경로 별칭(@/...)을 Jest가 이해할 수 있도록 설정
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/src/$1',
  },
  // 테스트 파일 경로 패턴
  testPathIgnorePatterns: ['<rootDir>/.next/', '<rootDir>/node_modules/'],
};

// createJestConfig를 호출하여 Next.js 설정을 Jest 설정에 맞게 비동기적으로 로드
module.exports = createJestConfig(customJestConfig);
