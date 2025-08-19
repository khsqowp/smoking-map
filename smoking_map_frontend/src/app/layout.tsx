import type { Metadata } from "next";
import "./globals.css";
import { UserProvider } from "@/context/UserContext"; // UserProvider import

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
        <body>
        <UserProvider> {/* UserProvider로 children 감싸기 */}
            {children}
        </UserProvider>
        </body>
        </html>
    );
}