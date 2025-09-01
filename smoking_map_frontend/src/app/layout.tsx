// src/app/layout.tsx

import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { UserProvider } from "@/context/UserContext";
import AppInitializer from "@/components/AppInitializer";
// --- ▼▼▼ [수정] GoogleAnalytics -> GoogleTagManager로 변경 ▼▼▼ ---
import GoogleTagManager from "@/components/GoogleTagManager";
// --- ▲▲▲ [수정] GoogleAnalytics -> GoogleTagManager로 변경 ▲▲▲ ---
import AdSenseSnippet from "@/components/AdSenseSnippet";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
    title: "흡연 지도",
    description: "내 주변 흡연구역을 찾아보세요.",
};

export default function RootLayout({
                                       children,
                                   }: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <html lang="ko">
        {/* --- ▼▼▼ [수정] <head> 태그에 GoogleTagManager 컴포넌트 적용 ▼▼▼ --- */}
        <head>
            <GoogleTagManager />
            <AdSenseSnippet />
        </head>
        {/* --- ▲▲▲ [수정] <head> 태그에 GoogleTagManager 컴포넌트 적용 ▲▲▲ --- */}
        <body className={inter.className}>
        {/* --- ▼▼▼ [추가] GTM noscript 태그 ▼▼▼ --- */}
        <noscript><iframe src="https://www.googletagmanager.com/ns.html?id=GTM-MK52RHVG"
                          height="0" width="0" style={{display:'none',visibility:'hidden'}}></iframe></noscript>
        {/* --- ▲▲▲ [추가] GTM noscript 태그 ▲▲▲ --- */}
        <UserProvider>
            <AppInitializer />
            {children}
        </UserProvider>
        </body>
        </html>
    );
}