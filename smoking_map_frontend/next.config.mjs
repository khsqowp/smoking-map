// next.config.mjs

const nextConfig = {
    // API 요청을 백엔드로 리다이렉트합니다.
    async rewrites() {
        return [
            {
                source: "/api/:path*", // /api/로 시작하는 모든 요청
                destination: `${process.env.NEXT_PUBLIC_API_URL}/api/:path*`, // 환경 변수 사용
            },
            {
                source: "/oauth2/:path*", // OAuth2 로그인 요청
                destination: `${process.env.NEXT_PUBLIC_API_URL}/oauth2/:path*`, // 환경 변수 사용
            },
            {
                source: "/logout", // 로그아웃 요청
                destination: `${process.env.NEXT_PUBLIC_API_URL}/logout`, // 환경 변수 사용
            }
        ];
    },

    // 보안 헤더 설정
    async headers() {
        return [
            {
                source: '/:path*',
                headers: [
                    {
                        key: 'X-Frame-Options',
                        value: 'SAMEORIGIN', // 클릭재킹 방지
                    },
                    {
                        key: 'X-Content-Type-Options',
                        value: 'nosniff', // MIME 타입 스니핑 방지
                    },
                    {
                        key: 'Content-Security-Policy',
                        value: "default-src 'self'; script-src 'self' 'unsafe-eval' 'unsafe-inline' https://oapi.map.naver.com; style-src 'self' 'unsafe-inline'; img-src * data:; font-src 'self' data:; object-src 'none'; base-uri 'self'; form-action 'self';", // XSS 및 데이터 주입 공격 방어
                    },
                    {
                        key: 'Referrer-Policy',
                        value: 'origin-when-cross-origin', // Referer 헤더 정보 제어
                    },
                ],
            },
        ];
    },
};

export default nextConfig;