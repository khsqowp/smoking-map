'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

const menuItems = [
    { href: '/admin/dashboard', label: '📈 대시보드' },
    { href: '/admin/places', label: '📍 장소 관리' },
    { href: '/admin/users', label: '👥 사용자 관리' },
];

export default function AdminSidebar() {
    const pathname = usePathname();

    return (
        <aside style={{ width: '250px', backgroundColor: '#343a40', color: 'white', padding: '20px', minHeight: '100vh' }}>
            <h2 style={{ marginBottom: '30px' }}>관리자 페이지</h2>
            <nav>
                <ul style={{ listStyle: 'none', padding: 0 }}>
                    {menuItems.map(item => (
                        <li key={item.href} style={{ marginBottom: '15px' }}>
                            <Link href={item.href} style={{
                                color: pathname === item.href ? '#007bff' : 'white',
                                textDecoration: 'none',
                                fontSize: '18px',
                                fontWeight: pathname === item.href ? 'bold' : 'normal',
                            }}>
                                {item.label}
                            </Link>
                        </li>
                    ))}
                </ul>
            </nav>
        </aside>
    );
}