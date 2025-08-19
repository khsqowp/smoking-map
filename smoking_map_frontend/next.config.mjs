const nextConfig = {
    // API 요청을 백엔드로 리다이렉트합니다.
    async rewrites() {
        return [
            {
                source: "/api/:path*", // /api/로 시작하는 모든 요청
                destination: "http://localhost:8080/api/:path*", // 백엔드 서버 주소로 변경
            },
            {
                source: "/oauth2/:path*", // OAuth2 로그인 요청
                destination: "http://localhost:8080/oauth2/:path*",
            },
            {
                source: "/logout", // 로그아웃 요청
                destination: "http://localhost:8080/logout",
            }
        ];
    },
};

export default nextConfig;