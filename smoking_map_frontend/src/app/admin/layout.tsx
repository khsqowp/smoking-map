'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import AdminSidebar from '@/components/AdminSidebar';
import { useUser } from '@/context/UserContext';
import '../globals.css'; // 전역 CSS import

export default function AdminLayout({ children }: { children: React.ReactNode }) {
    const { user, isLoading } = useUser();

    if (isLoading) {
        // 사용자 정보를 불러오는 동안 표시할 로딩 상태
        return <div style={{ padding: '50px', textAlign: 'center' }}>Loading user data...</div>;
    }

    if (user?.role !== 'ADMIN') {
        // 관리자가 아닐 경우 접근 차단
        return (
            <div style={{ padding: '50px', textAlign: 'center' }}>
                <h1>접근 권한이 없습니다.</h1>
                <p>이 페이지는 관리자만 접근할 수 있습니다.</p>
                <Link href="/" style={{ textDecoration: 'underline' }}>메인으로 돌아가기</Link>
            </div>
        );
    }

    // 관리자일 경우 정상적으로 레이아웃 렌더링
    return (
        <div className="admin-layout">
            <div className="admin-sidebar-container">
                <AdminSidebar />
            </div>

            <div className="admin-main-content">
                <header className="admin-header">
                    <Link href="/" className="admin-header-link">
                        지도로 돌아가기
                    </Link>
                </header>

                <main className="admin-page-content">
                    {children}
                </main>
            </div>
        </div>
    );
}