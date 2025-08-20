// next.config.mjs

const nextConfig = {
    async rewrites() {
        return [
            {
                source: "/api/:path*",
                destination: `${process.env.NEXT_PUBLIC_API_URL}/api/:path*`,
            },
            {
                source: "/oauth2/:path*",
                destination: `${process.env.NEXT_PUBLIC_API_URL}/oauth2/:path*`,
            },
            {
                source: "/logout",
                destination: `${process.env.NEXT_PUBLIC_API_URL}/logout`,
            }
        ];
    },

    async headers() {
        return [
            {
                source: '/:path*',
                headers: [
                    {
                        key: 'X-Frame-Options',
                        value: 'SAMEORIGIN',
                    },
                    {
                        key: 'X-Content-Type-Options',
                        value: 'nosniff',
                    },
                    {
                        key: 'Content-Security-Policy',
                        // --- ▼▼▼ [수정] Content-Security-Policy 값 수정 ▼▼▼ ---
                        value: [
                            "default-src 'self'",
                            // 'script-src'에 *.naver.net 추가
                            "script-src 'self' 'unsafe-eval' 'unsafe-inline' oapi.map.naver.com *.naver.net",
                            "style-src 'self' 'unsafe-inline' *.naver.net",
                            // 'img-src'에 blob: 추가
                            "img-src * data: blob:",
                            "connect-src 'self' *.naver.com *.navercorp.com",
                            "font-src 'self' data:",
                            "object-src 'none'",
                            "base-uri 'self'",
                            "form-action 'self'",
                        ].join('; '),
                        // --- ▲▲▲ [수정] Content-Security-Policy 값 수정 ▲▲▲ ---
                    },
                    {
                        key: 'Referrer-Policy',
                        value: 'origin-when-cross-origin',
                    },
                ],
            },
        ];
    },
};

export default nextConfig;