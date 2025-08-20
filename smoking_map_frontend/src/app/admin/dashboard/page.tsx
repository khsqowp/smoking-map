// src/app/admin/dashboard/page.tsx

'use client';

import { useEffect, useState } from 'react';
import { Bar, Line } from 'react-chartjs-2'; // Line 차트 추가
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, PointElement, LineElement, Title, Tooltip, Legend } from 'chart.js';

ChartJS.register(CategoryScale, LinearScale, BarElement, PointElement, LineElement, Title, Tooltip, Legend);

// 데이터 타입 정의 (백엔드 DTO와 일치)
interface DashboardData {
    totalPlaces: number;
    totalUsers: number;
    periodPlacesCount: number;
    placesGrowthRate: number;
    usersGrowthRate: number;
    newPlacesChartData: Record<string, number>;
    newUsersChartData: Record<string, number>;
}

// 증감률 표시 컴포넌트
const GrowthRate = ({ rate }: { rate: number }) => {
    const isPositive = rate > 0;
    const isNegative = rate < 0;
    const rateColor = isPositive ? '#E53E3E' : isNegative ? '#3182CE' : '#A0AEC0';
    const arrow = isPositive ? '▲' : isNegative ? '▼' : '';

    return (
        <span style={{ color: rateColor, fontSize: '14px', fontWeight: 'bold' }}>
      {arrow} {Math.abs(rate).toFixed(1)}%
    </span>
    );
};

export default function DashboardPage() {
    const [data, setData] = useState<DashboardData | null>(null);
    const [error, setError] = useState('');
    const [timeRange, setTimeRange] = useState('weekly'); // 기간 상태 관리

    useEffect(() => {
        const fetchData = async () => {
            setError('');
            try {
                // timeRange를 쿼리 파라미터로 추가
                const res = await fetch(`/api/v1/admin/dashboard?range=${timeRange}`, { credentials: 'include' });
                if (!res.ok) {
                    throw new Error('데이터를 불러오는데 실패했습니다. 관리자 권한을 확인하세요.');
                }
                const result = await res.json();
                setData(result);
            } catch (err: any) {
                setError(err.message);
            }
        };
        fetchData();
    }, [timeRange]); // timeRange가 변경될 때마다 데이터 다시 호출

    const chartOptions = {
        responsive: true,
        plugins: { legend: { display: false } },
        scales: { y: { ticks: { color: '#E0E0E0' } }, x: { ticks: { color: '#E0E0E0' } } },
    };

    const lineChartData = (chartData: Record<string, number>, label: string) => ({
        labels: Object.keys(chartData),
        datasets: [{
            label,
            data: Object.values(chartData),
            borderColor: '#48BB78', // 네온 그린
            backgroundColor: 'rgba(72, 187, 120, 0.2)',
            fill: true,
            tension: 0.3,
        }],
    });

    if (error) return <div style={{ color: 'red' }}>{error}</div>;
    if (!data) return <div>데이터 로딩 중...</div>;

    return (
        <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <h1>대시보드</h1>
                <div>
                    {['weekly', 'monthly', 'yearly'].map(range => (
                        <button key={range} onClick={() => setTimeRange(range)}
                            style={{
                                padding: '8px 12px',
                                margin: '0 5px',
                                border: timeRange === range ? '1px solid #48BB78' : '1px solid #444',
                                backgroundColor: timeRange === range ? '#48BB78' : '#2a2a2a',
                                color: 'white',
                                borderRadius: '5px',
                                cursor: 'pointer'
                            }}>
                            {range.charAt(0).toUpperCase() + range.slice(1)}
                        </button>
                    ))}
                </div>
            </div>

            <div className="summary-card-container" style={{ display: 'flex', gap: '20px', marginBottom: '30px' }}>
                <div className="summary-card">
                    <h3>총 장소 수</h3>
                    <p>{data.totalPlaces.toLocaleString()} <GrowthRate rate={data.placesGrowthRate} /></p>
                </div>
                <div className="summary-card">
                    <h3>총 사용자 수</h3>
                    <p>{data.totalUsers.toLocaleString()} <GrowthRate rate={data.usersGrowthRate} /></p>
                </div>
                <div className="summary-card">
                    <h3>기간 내 등록 장소</h3>
                    <p>{data.periodPlacesCount.toLocaleString()}</p>
                </div>
            </div>

            <div className="recent-activity-container" style={{ display: 'flex', gap: '20px' }}>
                <div style={{flex: 1, backgroundColor: '#1E1E1E', padding: '20px', borderRadius: '8px', border: '1px solid #333'}}>
                    <h3>신규 장소 등록 추이</h3>
                    <Line options={chartOptions} data={lineChartData(data.newPlacesChartData, '신규 장소')} />
                </div>
                <div style={{flex: 1, backgroundColor: '#1E1E1E', padding: '20px', borderRadius: '8px', border: '1px solid #333'}}>
                    <h3>신규 가입자 추이</h3>
                    <Line options={chartOptions} data={lineChartData(data.newUsersChartData, '신규 가입자')} />
                </div>
            </div>
        </div>
    );
}