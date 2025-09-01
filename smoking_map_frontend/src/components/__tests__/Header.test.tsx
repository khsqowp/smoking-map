import { render, screen } from '@testing-library/react';
import Header from '@/components/Header';
import { UserContext, User } from '@/context/UserContext';
import { ReactNode } from 'react';

// 테스트를 위한 Mock UserContext Provider 컴포넌트
// 왜? Header 컴포넌트는 UserContext에 의존하므로, 테스트 환경에서 가짜 Context를 제공하여 의존성을 제어하기 위함.
const MockUserProvider = ({ user, children }: { user: User | null; children: ReactNode }) => (
    <UserContext.Provider value={{ user, setUser: () => {} }}>
        {children}
    </UserContext.Provider>
);

// render 함수를 테스트 상황에 맞게 재정의
// 왜? 모든 테스트 케이스에서 반복적으로 Provider를 감싸는 코드를 줄여 테스트 코드를 간결하게 만들기 위함.
const renderHeader = (user: User | null) => {
    return render(
        <MockUserProvider user={user}>
            <Header />
        </MockUserProvider>
    );
};


describe('Header 컴포넌트 테스트', () => {

    describe('사용자가 로그인하지 않은 경우', () => {
        it('"흡연 지도" 제목과 "Google로 로그인" 버튼이 보여야 한다', () => {
            // 준비 (Arrange)
            // 왜? 비로그인 상태의 UI가 올바르게 렌더링되는지 검증하는 것이 목적.
            renderHeader(null);

            // 실행 및 검증 (Act & Assert)
            // 'toBeInTheDocument' matcher는 @testing-library/jest-dom에서 제공
            expect(screen.getByRole('heading', { name: '흡연 지도' })).toBeInTheDocument();
            expect(screen.getByRole('link', { name: 'Google로 로그인' })).toBeInTheDocument();
        });

        it('"즐겨찾기", "로그아웃", "관리자 페이지" 링크는 보이지 않아야 한다', () => {
            // 준비 (Arrange)
            // 왜? 비로그인 사용자에게는 권한이 없는 메뉴가 노출되지 않는지 확인하여 의도치 않은 접근을 방지하기 위함.
            renderHeader(null);

            // 실행 및 검증 (Act & Assert)
            // 'queryByRole'은 요소가 없을 때 null을 반환하여, 요소가 없는 상황을 테스트하기에 적합함.
            expect(screen.queryByRole('link', { name: '즐겨찾기' })).not.toBeInTheDocument();
            expect(screen.queryByRole('link', { name: '로그아웃' })).not.toBeInTheDocument();
            expect(screen.queryByRole("link", { name: "관리자 페이지" })).not.toBeInTheDocument();
        });
    });

    describe('일반 사용자(USER)로 로그인한 경우', () => {
        const mockUser: User = {
            id: 1,
            name: '테스트유저',
            email: 'test@example.com',
            picture: 'test.jpg',
            role: 'USER'
        };

        it('사용자 이름, 프로필 사진, "즐겨찾기", "로그아웃" 링크가 보여야 한다', () => {
            // 준비 (Arrange)
            // 왜? 일반 사용자로 로그인했을 때, 개인화된 UI와 기본 메뉴들이 올바르게 표시되는지 검증하기 위함.
            renderHeader(mockUser);

            // 실행 및 검증 (Act & Assert)
            expect(screen.getByText('테스트유저님')).toBeInTheDocument();
            expect(screen.getByRole('img', { name: '테스트유저' })).toBeInTheDocument();
            expect(screen.getByRole('link', { name: '즐겨찾기' })).toBeInTheDocument();
            expect(screen.getByRole('link', { name: '로그아웃' })).toBeInTheDocument();
        });

        it('"Google로 로그인"과 "관리자 페이지" 링크는 보이지 않아야 한다', () => {
            // 준비 (Arrange)
            // 왜? 로그인한 사용자에게 불필요한 로그인 버튼이나 권한 없는 관리자 메뉴가 노출되지 않는지 확인하기 위함.
            renderHeader(mockUser);

            // 실행 및 검증 (Act & Assert)
            expect(screen.queryByRole('link', { name: 'Google로 로그인' })).not.toBeInTheDocument();
            expect(screen.queryByRole("link", { name: "관리자 페이지" })).not.toBeInTheDocument();
        });
    });

    describe('관리자(ADMIN)로 로그인한 경우', () => {
        const mockAdmin: User = {
            id: 2,
            name: '관리자',
            email: 'admin@example.com',
            picture: 'admin.jpg',
            role: 'ADMIN'
        };

        it('"관리자 페이지" 링크를 포함한 모든 사용자 메뉴가 보여야 한다', () => {
            // 준비 (Arrange)
            // 왜? 관리자 권한을 가진 사용자에게만 제공되는 특정 메뉴(관리자 페이지)가 올바르게 노출되는지 검증하기 위함.
            renderHeader(mockAdmin);

            // 실행 및 검증 (Act & Assert)
            expect(screen.getByText('관리자님')).toBeInTheDocument();
            expect(screen.getByRole('link', { name: '즐겨찾기' })).toBeInTheDocument();
            expect(screen.getByRole('link', { name: '로그아웃' })).toBeInTheDocument();
            expect(screen.getByRole("link", { name: "관리자 페이지" })).toBeInTheDocument();
        });
    });
});
