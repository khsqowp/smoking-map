'use client';

import { useEffect, useState } from 'react';

interface DashboardData {
    totalPlaces: number;
    totalUsers: number;
    todayPlacesCount: number;
    recentPlaces: { id: number; roadAddress: string; createdAt: string; }[];
    recentUsers: { id: number; name: string; email: string; createdAt: string; }[];
}

export default function DashboardPage() {
    const [data, setData] = useState<DashboardData | null>(null);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchData = async () => {
            try {
                const res = await fetch('/api/v1/admin/dashboard', { credentials: 'include' });
                if (!res.ok) {
                    if (res.status === 403) {
                        throw new Error('데이터를 불러오는데 실패했습니다. 관리자 권한을 확인하세요.');
                    }
                    throw new Error(`서버 오류: ${res.status}`);
                }
                const result = await res.json();
                setData(result);
            } catch (err: any) {
                setError(err.message);
            }
        };
        fetchData();
    }, []);

    if (error) return <div style={{ color: 'red' }}>{error}</div>;
    if (!data) return <div>데이터 로딩 중...</div>;

    return (
        <div>
            <h1 style={{marginBottom: '20px'}}>대시보드</h1>

            <div style={{ display: 'flex', gap: '20px', marginBottom: '40px' }}>
                <div className="summary-card">
                    <h3>총 장소 수</h3>
                    <p>{data.totalPlaces}</p>
                </div>
                <div className="summary-card">
                    <h3>총 사용자 수</h3>
                    <p>{data.totalUsers}</p>
                </div>
                <div className="summary-card">
                    <h3>오늘 등록된 장소</h3>
                    <p>{data.todayPlacesCount}</p>
                </div>
            </div>

            <div style={{ display: 'flex', gap: '20px' }}>
                <div style={{ flex: 1 }}>
                    <h3>최근 등록된 장소</h3>
                    <table className="admin-table">
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>주소</th>
                            <th>등록일</th>
                        </tr>
                        </thead>
                        <tbody>
                        {data.recentPlaces.map(place => (
                            <tr key={place.id}>
                                <td>{place.id}</td>
                                <td>{place.roadAddress}</td>
                                <td>{place.createdAt}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
                <div style={{ flex: 1 }}>
                    <h3>최근 가입한 사용자</h3>
                    <table className="admin-table">
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>이름</th>
                            <th>이메일</th>
                            <th>가입일</th>
                        </tr>
                        </thead>
                        <tbody>
                        {data.recentUsers.map(user => (
                            <tr key={user.id}>
                                <td>{user.id}</td>
                                <td>{user.name}</td>
                                <td>{user.email}</td>
                                <td>{user.createdAt}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}