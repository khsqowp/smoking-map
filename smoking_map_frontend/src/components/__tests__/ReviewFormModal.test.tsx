import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ReviewFormModal from '@/components/ReviewFormModal';
import { apiClient } from '@/utils/apiClient';

// apiClient 모듈 전체를 Mocking
// 왜? 테스트 중에 실제 네트워크 요청을 보내는 것을 방지하고, API의 성공/실패 케이스를 우리가 원하는 대로 제어하기 위함.
jest.mock('@/utils/apiClient');

// jest.spyOn을 사용하여 window.alert를 감시
// 왜? JSDOM 환경에서는 alert UI가 없으므로, 함수가 호출되었는지, 어떤 메시지로 호출되었는지를 추적하기 위함.
const alertSpy = jest.spyOn(window, 'alert').mockImplementation(() => {});

describe('ReviewFormModal 컴포넌트 테스트', () => {
    const mockOnClose = jest.fn();
    const mockOnSuccess = jest.fn();
    const placeId = 1;

    // 각 테스트가 끝나면 Mock과 스파이를 초기화
    // 왜? 각 테스트는 독립적이어야 하므로, 이전 테스트의 Mock 상태가 다음 테스트에 영향을 주지 않도록 하기 위함.
    beforeEach(() => {
        jest.clearAllMocks();
    });

    it('기본 UI 요소들이 올바르게 렌더링되어야 한다', () => {
        // 준비 및 실행 (Arrange & Act)
        render(<ReviewFormModal placeId={placeId} onClose={mockOnClose} onSuccess={mockOnSuccess} />);

        // 검증 (Assert)
        expect(screen.getByRole('heading', { name: '리뷰 작성' })).toBeInTheDocument();
        expect(screen.getByPlaceholderText('한줄평을 남겨주세요 (선택)')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: '등록' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: '취소' })).toBeInTheDocument();
    });

    it('사용자가 한줄평을 입력하면, textarea의 내용이 변경되어야 한다', async () => {
        // 준비 (Arrange)
        render(<ReviewFormModal placeId={placeId} onClose={mockOnClose} onSuccess={mockOnSuccess} />);
        const textarea = screen.getByPlaceholderText('한줄평을 남겨주세요 (선택)');

        // 실행 (Act)
        // userEvent는 실제 사용자의 타이핑과 유사하게 이벤트를 발생시킴
        await userEvent.type(textarea, '정말 좋은 곳이에요!');

        // 검증 (Assert)
        expect(textarea).toHaveValue('정말 좋은 곳이에요!');
    });

    it('"취소" 버튼을 클릭하면, onClose 함수가 호출되어야 한다', async () => {
        // 준비 (Arrange)
        render(<ReviewFormModal placeId={placeId} onClose={mockOnClose} onSuccess={mockOnSuccess} />);
        const cancelButton = screen.getByRole('button', { name: '취소' });

        // 실행 (Act)
        await userEvent.click(cancelButton);

        // 검증 (Assert)
        expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    describe('폼 제출(handleSubmit) 로직 테스트', () => {
        it('별점을 선택하지 않고 제출하면, 경고 alert가 호출되고 API는 호출되지 않아야 한다', async () => {
            // 준비 (Arrange)
            render(<ReviewFormModal placeId={placeId} onClose={mockOnClose} onSuccess={mockOnSuccess} />);
            const submitButton = screen.getByRole('button', { name: '등록' });

            // 실행 (Act)
            await userEvent.click(submitButton);

            // 검증 (Assert)
            expect(alertSpy).toHaveBeenCalledWith('별점을 선택해주세요.');
            expect(apiClient).not.toHaveBeenCalled();
        });

        it('성공적으로 제출하면, apiClient가 올바른 데이터와 함께 호출되고 onSuccess 콜백이 실행되어야 한다', async () => {
            // 준비 (Arrange)
            // apiClient가 Promise.resolve()를 반환하도록 설정 (성공 케이스)
            (apiClient as jest.Mock).mockResolvedValue(undefined);
            render(<ReviewFormModal placeId={placeId} onClose={mockOnClose} onSuccess={mockOnSuccess} />);
            
            // 4번째 별을 클릭 (별점 4점)
            const fourthStar = screen.getByText('☆').parentElement?.children[3] as HTMLElement;
            await userEvent.click(fourthStar);
            await userEvent.type(screen.getByPlaceholderText('한줄평을 남겨주세요 (선택)'), '댓글 내용');

            // 실행 (Act)
            await userEvent.click(screen.getByRole('button', { name: '등록' }));

            // 검증 (Assert)
            expect(apiClient).toHaveBeenCalledWith(`/api/v1/places/${placeId}/reviews`, {
                method: 'POST',
                body: { rating: 4, comment: '댓글 내용' },
            });
            expect(alertSpy).toHaveBeenCalledWith('리뷰가 등록되었습니다.');
            expect(mockOnSuccess).toHaveBeenCalledTimes(1);
        });

        it('API 호출이 실패하면, 에러 메시지를 담은 alert가 호출되고 onSuccess는 실행되지 않아야 한다', async () => {
            // 준비 (Arrange)
            const errorMessage = '서버 통신 실패';
            // apiClient가 에러를 포함한 Promise.reject()를 반환하도록 설정 (실패 케이스)
            (apiClient as jest.Mock).mockRejectedValue(new Error(errorMessage));
            render(<ReviewFormModal placeId={placeId} onClose={mockOnClose} onSuccess={mockOnSuccess} />);

            // 3번째 별을 클릭 (별점 3점)
            const thirdStar = screen.getByText('☆').parentElement?.children[2] as HTMLElement;
            await userEvent.click(thirdStar);

            // 실행 (Act)
            await userEvent.click(screen.getByRole('button', { name: '등록' }));

            // 검증 (Assert)
            expect(apiClient).toHaveBeenCalled();
            expect(alertSpy).toHaveBeenCalledWith(`오류: ${errorMessage}`);
            expect(mockOnSuccess).not.toHaveBeenCalled();
        });
    });
});
