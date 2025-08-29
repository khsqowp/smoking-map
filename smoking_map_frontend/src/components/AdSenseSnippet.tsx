// src/components/AdSenseSnippet.tsx

'use client';

import Script from 'next/script';

export default function AdSenseSnippet() {
    const adClient = process.env.NEXT_PUBLIC_ADSENSE_CLIENT_ID;

    if (!adClient) {
        // 환경 변수가 설정되지 않았을 경우, 개발 환경에서 오류를 방지하기 위해 null을 반환합니다.
        console.warn("AdSense Client ID가 설정되지 않았습니다.");
        return null;
    }

    return (
        <Script
            async
            src={`https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=${adClient}`}
            crossOrigin="anonymous"
            strategy="afterInteractive"
        />
    );
}