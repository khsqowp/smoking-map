/// <reference types="cypress" />

// 왜? 이 파일은 전체 사용자 흐름을 검증하는 최종 단계의 테스트입니다.
// 사용자가 지도와 상호작용하여 새로운 장소를 등록하는 핵심 기능이
// 프론트엔드와 (가상)백엔드 간의 연동을 통해 올바르게 동작하는지 확인합니다.
describe('새 흡연 구역 등록 E2E 테스트', () => {

  // 각 테스트 전에 실행되는 훅
  // 왜? 테스트를 실행하기 전에 필요한 사전 조건을 설정하기 위함.
  beforeEach(() => {
    // API 요청을 가로채서(intercept) 미리 정의된 응답을 반환하도록 설정합니다.
    // 왜? 실제 백엔드 서버에 의존하지 않고, 프론트엔드의 요청/응답 처리 로직만을 독립적으로 테스트하기 위함입니다.
    cy.intercept('POST', '/api/v1/places', {
      statusCode: 200,
      body: { id: 123 }, // 새로운 장소 ID를 반환하는 것처럼 시뮬레이션
    }).as('createPlace'); // 이 요청에 'createPlace'라는 별칭을 부여

    // 커스텀 로그인 명령어 (실제 프로젝트에서는 support/commands.ts에 구현 필요)
    // 여기서는 로그인 상태를 시뮬레이션하기 위해 쿠키를 직접 설정합니다.
    // 왜? 매번 UI를 통해 로그인하는 반복적인 과정을 생략하여 테스트를 빠르고 안정적으로 만들기 위함.
    const mockUser = {
        name: '테스트유저',
        email: 'test@example.com',
        picture: 'test.jpg',
        role: 'USER'
    };
    cy.setCookie('session', 'mock-session-id'); // 세션 쿠키 설정
    // UserContext가 이 정보를 사용하도록 localStorage에 저장 (실제 앱의 인증 방식에 따라 달라짐)
    window.localStorage.setItem('user', JSON.stringify(mockUser));

    // 메인 페이지 방문
    cy.visit('/');
  });

  it('사용자가 지도를 클릭하고, 정보를 입력하여 새 장소를 성공적으로 등록할 수 있다', () => {
    // 1. 준비 (Arrange)
    // 왜? 테스트에 필요한 모든 요소가 화면에 준비되었는지 확인하는 단계.
    // 지도가 로드될 때까지 기다립니다. (실제 앱에서는 로딩 상태에 따라 더 구체적인 요소를 기다려야 할 수 있음)
    cy.get('[aria-label="네이버 지도"]').should('be.visible');

    // 2. 실행 (Act)
    // 왜? 실제 사용자의 행동을 시뮬레이션하는 단계.

    // 지도의 중앙을 클릭하여 장소 등록 모달을 띄웁니다.
    // 참고: 실제 지도 라이브러리의 클릭 이벤트 처리에 따라 이 부분은 더 복잡한 구현이 필요할 수 있습니다.
    cy.get('[aria-label="네이버 지도"]').click();

    // 모달이 나타났는지 확인하고, 내용을 입력합니다.
    cy.get('h2').contains('장소 등록').should('be.visible'); // 모달 제목으로 확인
    cy.get('textarea').type('여기는 테스트 흡연 구역입니다.');
    cy.get('input[type="file"]').selectFile('cypress/fixtures/test-image.jpg');

    // '등록' 버튼을 클릭하여 폼을 제출합니다.
    cy.get('button').contains('등록').click();

    // 3. 검증 (Assert)
    // 왜? 사용자의 행동 결과가 우리가 의도한 대로 나타났는지 확인하는 단계.

    // API 요청이 올바른 데이터와 함께 전송되었는지 확인합니다.
    cy.wait('@createPlace').its('request.body').should(body => {
        // FormData를 직접 검증하기는 복잡하므로, 주요 필드가 포함되었는지 확인합니다.
        expect(body).to.include('latitude');
        expect(body).to.include('longitude');
        expect(body).to.include('테스트 흡연 구역입니다');
    });

    // 성공 메시지(alert)가 나타나는지 확인합니다.
    cy.on('window:alert', (str) => {
      expect(str).to.equal('장소가 등록되었습니다.');
    });

    // 등록 완료 후, 모달이 닫혔는지 확인합니다.
    cy.get('h2').contains('장소 등록').should('not.exist');
  });
});
