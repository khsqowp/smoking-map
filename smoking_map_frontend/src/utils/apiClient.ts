// src/utils/apiClient.ts

// 쿠키에서 특정 이름의 값을 읽어오는 함수
function getCookie(name: string): string | null {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop()?.split(';').shift() || null;
    return null;
}

interface ApiClientOptions extends RequestInit {
    body?: any;
}

export const apiClient = async (url: string, options: ApiClientOptions = {}) => {
    const csrfToken = getCookie('XSRF-TOKEN');

    // --- ▼▼▼ [수정] 일반 객체 대신 Headers 객체 사용 ▼▼▼ ---
    // 1. new Headers()로 Headers 객체를 생성합니다.
    const headers = new Headers(options.headers);

    // 2. .set() 메서드를 사용하여 헤더를 안전하게 추가합니다.
    if (csrfToken) {
        headers.set('X-XSRF-TOKEN', csrfToken);
    }

    if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
        options.body = JSON.stringify(options.body);
        headers.set('Content-Type', 'application/json');
    }
    // --- ▲▲▲ [수정] 일반 객체 대신 Headers 객체 사용 ▲▲▲ ---

    const response = await fetch(url, {
        ...options,
        headers, // 최종적으로 완성된 headers 객체를 사용
    });

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: response.statusText }));
        throw new Error(errorData.message || 'API 요청에 실패했습니다.');
    }

    if (response.status === 204 || response.headers.get('Content-Length') === '0') {
        return null;
    }

    return response.json();
};