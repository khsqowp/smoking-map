'use client';

// 현재 위치(GPS) 아이콘 SVG
const GpsIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
         stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="10"></circle>
        <path d="M12 2L12 6"></path>
        <path d="M12 18L12 22"></path>
        <path d="M22 12L18 12"></path>
        <path d="M6 12L2 12"></path>
    </svg>
);

interface Props {
    onClick: () => void;
    isLoading: boolean;
}

/**
 * 현재 위치 조회 버튼 컴포넌트
 * @param onClick - 버튼 클릭 시 실행될 함수
 * @param isLoading - 위치 조회 중인지 여부 (true일 경우 버튼 비활성화)
 */
export default function CurrentLocationButton({ onClick, isLoading }: Props) {
    return (
        <button
            onClick={onClick}
            disabled={isLoading}
            className="current-location-btn"
            aria-label="현재 위치로 이동"
        >
            {isLoading ? '...' : <GpsIcon />}
        </button>
    );
}