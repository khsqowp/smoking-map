// 이 파일은 커스텀 Cypress 명령어를 정의하는 곳입니다.
// 예: Cypress.Commands.add('login', (email, password) => { ... });
//
// 더 자세한 정보는 아래 주소에서 확인하세요:
// https://on.cypress.io/custom-commands

// TypeScript가 이 파일을 모듈로 인식하도록 하기 위해 export를 추가합니다.
export {};

declare global {
  namespace Cypress {
    interface Chainable {
      // 여기에 커스텀 명령어의 타입을 정의합니다.
      // 예: login(email: string, password: string): Chainable<void>;
    }
  }
}
