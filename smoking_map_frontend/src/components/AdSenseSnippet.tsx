// src/components/AdSenseSnippet.tsx (수정된 최종 버전)

'use client';

// 'next/script' import를 제거합니다.

export default function AdSenseSnippet() {
    const adClient = process.env.NEXT_PUBLIC_ADSENSE_CLIENT_ID;

    if (!adClient) {
        // 환경 변수가 설정되지 않았을 경우, 개발 환경에서 오류를 방지하기 위해 null을 반환합니다.
        console.warn("AdSense Client ID가 설정되지 않았습니다.");
        return null;
    }

    return (
        // Next.js의 <Script> 컴포넌트를 일반 <script> 태그로 변경합니다.
        <script
            async
            src={`https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=${adClient}`}
            crossOrigin="anonymous"
        ></script>
    );
}
