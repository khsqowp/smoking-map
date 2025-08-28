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
        // CSP 테스트를 위해 임시로 모든 헤더 설정을 비활성화합니다.
        return [];
    },
};

export default nextConfig;