// src/components/AdminSidebar.tsx

'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation'; // 현재 경로를 알기 위해 usePathname import

export default function AdminSidebar() {
    const pathname = usePathname(); // 현재 URL 경로를 가져옵니다.

    return (
        <nav className="admin-nav"> {/* 스타일링을 위해 클래스 추가 */}
            <ul>
                <li>
                    {/* 현재 경로와 링크 경로가 일치하면 'active' 클래스를 적용합니다. */}
                    <Link href="/admin/dashboard" className={pathname === '/admin/dashboard' ? 'active' : ''}>
                        대시보드
                    </Link>
                </li>
                <li>
                    <Link href="/admin/places" className={pathname === '/admin/places' ? 'active' : ''}>
                        장소 관리
                    </Link>
                </li>
                <li>
                    <Link href="/admin/users" className={pathname === '/admin/users' ? 'active' : ''}>
                        사용자 관리
                    </Link>
                </li>
                <li>
                    <Link href="/admin/reports" className={pathname === '/admin/reports' ? 'active' : ''}>
                        신고 관리
                    </Link>
                </li>
            </ul>
        </nav>
    );
}