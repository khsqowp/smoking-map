// src/app/layout.tsx

import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { UserProvider } from "@/context/UserContext";
import AppInitializer from "@/components/AppInitializer";
import GoogleTagManager from "@/components/GoogleTagManager";
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
        <head>
            <GoogleTagManager />
            <AdSenseSnippet />
        </head>
        <body className={inter.className}>
        <noscript><iframe src="https://www.googletagmanager.com/ns.html?id=GTM-MK52RHVG"
            height="0" width="0" style={{display:'none',visibility:'hidden'}}></iframe></noscript>
        <UserProvider>
            <AppInitializer />
            {children}
        </UserProvider>
        </body>
        </html>
    );
}