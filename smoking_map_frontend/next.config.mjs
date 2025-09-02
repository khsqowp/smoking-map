// next.config.mjs

const nextConfig = {
    async rewrites() {
        const internalApiUrl = process.env.INTERNAL_API_URL;

        return [
            {
                source: "/api/:path*",
                destination: `${internalApiUrl}/api/:path*`,
            },
            {
                source: "/oauth2/:path*",
                destination: `${internalApiUrl}/oauth2/:path*`,
            },
            {
                source: "/logout",
                destination: `${internalApiUrl}/logout`,
            }
        ];
    },

    async headers() {
        // CSP 테스트를 위해 임시로 모든 헤더 설정을 비활성화합니다.
        return [];
    },
};

export default nextConfig;