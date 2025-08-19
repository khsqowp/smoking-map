'use client';

import Link from 'next/link'; // Next.js의 Link 컴포넌트 import
import { useUser } from '@/context/UserContext';

export default function Header() {
    const { user } = useUser();

    return (
        <header style={{
            height: '64px',
            padding: '0 1rem',
            backgroundColor: '#2d2d2d',
            color: 'white',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center'
        }}>
            <Link href="/" style={{ textDecoration: 'none', color: 'white' }}>
                <h1 style={{ fontSize: '1.5rem', margin: 0 }}>흡연 지도</h1>
            </Link>
            <div>
                {user ? (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
                        {/* [추가된 부분] 사용자의 역할이 'ADMIN'일 때만 관리자 페이지 버튼을 보여줍니다. */}
                        {user.role === 'ADMIN' && (
                            <Link href="/admin/dashboard" style={{
                                backgroundColor: '#007bff',
                                color: 'white',
                                padding: '8px 12px',
                                borderRadius: '5px',
                                textDecoration: 'none',
                                fontSize: '14px'
                            }}>
                                관리자 페이지
                            </Link>
                        )}

                        <img src={user.picture} alt={user.name} width={32} height={32} style={{ borderRadius: '50%' }} />
                        <span>{user.name}님</span>
                        <a href="/logout" style={{
                            backgroundColor: '#4a4a4a',
                            color: 'white',
                            padding: '8px 12px',
                            borderRadius: '5px',
                            textDecoration: 'none',
                            fontSize: '14px'
                        }}>
                            로그아웃
                        </a>
                    </div>
                ) : (
                    <a href="/oauth2/authorization/google" style={{
                        backgroundColor: '#4285F4',
                        color: 'white',
                        padding: '8px 15px',
                        borderRadius: '5px',
                        textDecoration: 'none',
                        fontSize: '14px'
                    }}>
                        Google로 로그인
                    </a>
                )}
            </div>
        </header>
    );
}