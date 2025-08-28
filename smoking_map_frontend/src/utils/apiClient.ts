// src/utils/apiClient.ts

// 쿠키에서 특정 이름의 값을 읽어오는 함수
function getCookie(name: string): string | null {
    if (typeof document === 'undefined') {
        return null;
    }
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop()?.split(';').shift() || null;
    return null;
}

// apiClient 함수에 필요한 옵션 타입을 독립적으로 정의
interface ApiClientOptions {
    method?: string;
    headers?: Record<string, string>;
    body?: Record<string, any> | FormData;
    credentials?: 'include' | 'same-origin' | 'omit';
    cache?: RequestCache;
}

export const apiClient = async (url: string, options: ApiClientOptions = {}) => {
    const csrfToken = getCookie('XSRF-TOKEN');
    const headers = new Headers(options.headers);

    if (csrfToken) {
        headers.set('X-XSRF-TOKEN', csrfToken);
    }

    // fetch에 전달할 최종 옵션을 RequestInit 타입으로 안전하게 생성
    const finalOptions: RequestInit = {
        method: options.method,
        headers: headers,
        credentials: options.credentials,
        cache: options.cache,
    };

    if (options.body) {
        if (options.body instanceof FormData) {
            finalOptions.body = options.body;
        } else if (typeof options.body === 'object') {
            finalOptions.body = JSON.stringify(options.body);
            headers.set('Content-Type', 'application/json');
        }
    }

    const response = await fetch(url, finalOptions);

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: response.statusText }));
        throw new Error(errorData.message || 'API 요청에 실패했습니다.');
    }

    if (response.status === 204 || response.headers.get('Content-Length') === '0') {
        return null;
    }

    return response.json();
};