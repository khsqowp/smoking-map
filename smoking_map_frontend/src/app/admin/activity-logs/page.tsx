// src/app/admin/activity-logs/page.tsx

'use client';

import { useState, useEffect } from 'react';
import { apiClient } from '@/utils/apiClient';

interface ActivityLog {
    id: number;
    activityTime: string;
    latitude: number;
    longitude: number;
    userType: string;
    identifier: string;
}

export default function ActivityLogsPage() {
    const [logs, setLogs] = useState<ActivityLog[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchLogs = async () => {
            try {
                const data = await apiClient('/api/v1/admin/activity-logs');
                setLogs(data);
            } catch (err) {
                setError('데이터를 불러오는 중 오류가 발생했습니다.');
                console.error(err);
            } finally {
                setIsLoading(false);
            }
        };
        fetchLogs();
    }, []);

    if (isLoading) {
        return <div>데이터를 불러오는 중...</div>;
    }

    if (error) {
        return <div style={{ color: '#E53E3E' }}>{error}</div>;
    }

    return (
        <div>
            <h1 style={{ fontSize: '24px', fontWeight: '600', marginBottom: '20px' }}>최근 활동 로그 (최대 100개)</h1>
            <div style={{ overflowX: 'auto' }}>
                <table className="admin-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>활동 시간</th>
                        <th>좌표 (위도, 경도)</th>
                        <th>사용자 구분</th>
                        <th>식별자</th>
                    </tr>
                    </thead>
                    <tbody>
                    {logs.map(log => (
                        <tr key={log.id}>
                            <td>{log.id}</td>
                            <td>{log.activityTime}</td>
                            <td>{log.latitude.toFixed(6)}, {log.longitude.toFixed(6)}</td>
                            <td>{log.userType}</td>
                            <td style={{ wordBreak: 'break-all' }}>{log.identifier}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}