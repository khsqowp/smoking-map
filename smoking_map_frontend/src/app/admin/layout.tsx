'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import AdminSidebar from '@/components/AdminSidebar';
import { useUser } from '@/context/UserContext';

export default function AdminLayout({ children }: { children: React.ReactNode }) {
    const { user, isLoading } = useUser();
    const [isSidebarOpen, setIsSidebarOpen] = useState(false);

    if (isLoading) {
        return <div>Loading...</div>;
    }

    if (user?.role !== 'ADMIN') {
        return (
            <div style={{ padding: '50px', textAlign: 'center' }}>
                <h1>접근 권한이 없습니다.</h1>
                <p>이 페이지는 관리자만 접근할 수 있습니다.</p>
            </div>
        );
    }

    return (
        <div className="admin-layout">
            <div className={`admin-sidebar-container ${isSidebarOpen ? 'open' : ''}`}>
                <AdminSidebar />
            </div>

            <div className="admin-main-content">
                <header className="admin-header">
                    {/* 모바일용 햄버거 메뉴 버튼 */}
                    <button className="hamburger-menu" onClick={() => setIsSidebarOpen(true)}>
                        ☰
                    </button>

                    <Link href="/" className="admin-header-link">
                        지도로 돌아가기
                    </Link>
                </header>

                <main className="admin-page-content">
                    {children}
                </main>
            </div>

            {/* 모바일에서 사이드바가 열렸을 때 표시될 오버레이 */}
            {isSidebarOpen && (
                <div className="sidebar-overlay" onClick={() => setIsSidebarOpen(false)}></div>
            )}
        </div>
    );
}