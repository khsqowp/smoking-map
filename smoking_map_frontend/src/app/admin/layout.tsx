'use client';

import React from 'react';
import AdminSidebar from '@/components/AdminSidebar';
import { useUser } from '@/context/UserContext';

export default function AdminLayout({ children }: { children: React.ReactNode }) {
    const { user, isLoading } = useUser();

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
        <div style={{ display: 'flex' }}>
            <AdminSidebar />
            <main style={{ flex: 1, padding: '20px', backgroundColor: '#f4f6f9' }}>
                {children}
            </main>
        </div>
    );
}