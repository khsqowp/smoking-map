// src/utils/apiClient.ts (최종 완성 버전)

// 코드가 서버에서 실행되는지 클라이언트에서 실행되는지 확인
const IS_SERVER = typeof window === 'undefined';

// 실행 환경에 따라 다른 기본 URL을 사용
const BASE_URL = IS_SERVER
  ? 'http://backend:8080' // 서버 환경에서는 Docker 서비스 이름으로 직접 통신
  : process.env.NEXT_PUBLIC_API_URL; // 클라이언트 환경에서는 외부 접속용 URL 사용

// 쿠키에서 특정 이름의 값을 읽어오는 함수
function getCookie(name: string): string | null {
    if (IS_SERVER) {
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

    // BASE_URL을 사용하여 전체 URL을 구성합니다.
    const fullUrl = url.startsWith('http') ? url : `${BASE_URL}${url}`;

    if (csrfToken) {
        headers.set('X-XSRF-TOKEN', csrfToken);
    }

    // fetch에 전달할 최종 옵션을 RequestInit 타입으로 안전하게 생성
    const finalOptions: RequestInit = {
        method: options.method,
        headers: headers,
        credentials: options.credentials || 'include',
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

    const response = await fetch(fullUrl, finalOptions);

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: response.statusText }));
        throw new Error(errorData.message || 'API 요청에 실패했습니다.');
    }

    if (response.status === 204 || response.headers.get('Content-Length') === '0') {
        return null;
    }

    return response.json();
};
