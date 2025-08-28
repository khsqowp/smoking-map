// src/app/layout.tsx

import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { UserProvider } from "@/context/UserContext";
import AppInitializer from "@/components/AppInitializer";
// --- ▼▼▼ [추가] import ▼▼▼ ---
import GoogleAnalytics from "@/components/GoogleAnalytics";
// --- ▲▲▲ [추가] import ▲▲▲ ---


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
        {/* --- ▼▼▼ [추가] GoogleAnalytics 컴포넌트 추가 ▼▼▼ --- */}
        <GoogleAnalytics />
        {/* --- ▲▲▲ [추가] GoogleAnalytics 컴포넌트 추가 ▲▲▲ --- */}
        <body className={inter.className}>
        <UserProvider>
            <AppInitializer />
            {children}
        </UserProvider>
        </body>
        </html>
    );
}