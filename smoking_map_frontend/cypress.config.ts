import { defineConfig } from 'cypress';

export default defineConfig({
  e2e: {
    // 테스트 실행 시 기본적으로 방문할 URL
    // Next.js 개발 서버의 기본 주소인 http://localhost:3000으로 설정
    baseUrl: 'http://localhost:3000',
    
    // `cy.visit()` 등의 명령어에 대한 기본 타임아웃 설정 (단위: ms)
    pageLoadTimeout: 60000,

    // e2e 테스트 관련 설정을 위한 setupNodeEvents 함수
    setupNodeEvents(on, config) {
      // 여기에 플러그인 등을 등록할 수 있습니다.
    },
  },
});
