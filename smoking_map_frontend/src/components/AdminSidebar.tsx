'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

export default function AdminSidebar() {
    const pathname = usePathname();

    return (
        <nav className="admin-nav">
            <ul>
                <li>
                    <Link href="/admin/dashboard"
                        className={pathname === '/admin/dashboard' ? 'active' : ''}> 대시보드 </Link>
                </li>
                <li>
                    <Link href="/admin/places" className={pathname === '/admin/places' ? 'active' : ''}> 장소 관리 </Link>
                </li>
                <li>
                    <Link href="/admin/reports" className={pathname === '/admin/reports' ? 'active' : ''}> 신고 관리 </Link>
                </li>
                <li>
                    <Link href="/admin/users" className={pathname === '/admin/users' ? 'active' : ''}> 사용자 관리 </Link>
                </li>
                <li>
                    <Link href="/admin/announcements"
                        className={pathname === '/admin/announcements' ? 'active' : ''}> 공지 관리 </Link>
                </li>
                <li>
                    <Link href="/admin/activity-logs"
                        className={pathname === '/admin/activity-logs' ? 'active' : ''}> 활동 분석 </Link>
                </li>
                <li>
                    <Link href="/admin/heatmap" className={pathname === '/admin/heatmap' ? 'active' : ''}> 히트맵 </Link>
                </li>
            </ul>
        </nav>
    );
}
