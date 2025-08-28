'use client';

import { useState, useEffect, useCallback } from 'react';
import { apiClient } from '@/utils/apiClient';
import DashboardChart from '@/components/DashboardChart'; // --- ▼▼▼ [수정] 새로운 차트 컴포넌트 import ▼▼▼ ---

interface SummaryData {
    totalPlaces: number;
    totalUsers: number;
    periodPlacesCount: number;
    placesGrowthRate: number;
    usersGrowthRate: number;
}

const SummaryCard = ({ title, value, growthRate }: { title: string; value: string; growthRate: number; }) => {
    const isPositive = growthRate >= 0;
    const colorClass = isPositive ? 'text-green-500' : 'text-red-500';
    const arrow = isPositive ? '↑' : '↓';

    return (
        <div className="summary-card">
            <h3 style={{ color: '#A0AEC0', fontSize: '16px', margin: '0 0 10px 0' }}>{title}</h3>
            <p style={{ fontSize: '32px', fontWeight: 'bold', margin: '0 0 10px 0' }}>{value}</p>
            <p style={{ fontSize: '14px', margin: 0 }} className={colorClass}>
                <span style={{ color: isPositive ? '#48BB78' : '#E53E3E' }}>
                    {arrow} {growthRate.toFixed(2)}%
                </span>
            </p>
        </div>
    );
};

export default function AdminDashboardPage() {
    const [summaryData, setSummaryData] = useState<SummaryData | null>(null);
    const [chartData, setChartData] = useState(null); // --- ▼▼▼ [수정] 차트 데이터 상태 추가 ▼▼▼ ---
    const [range, setRange] = useState('daily');
    const [isLoading, setIsLoading] = useState(true);

    const fetchData = useCallback(async (currentRange: string) => {
        setIsLoading(true);
        try {
            // --- ▼▼▼ [수정] 요약 API와 차트 API를 분리하여 호출 ▼▼▼ ---
            const [summaryRes, chartRes] = await Promise.all([
                apiClient(`/api/v1/admin/dashboard?range=${currentRange}`),
                apiClient(`/api/v1/admin/dashboard/chart?range=${currentRange}`)
            ]);
            setSummaryData(summaryRes);
            setChartData(chartRes.chartData); // API 응답 구조에 맞게 chartData를 설정
        } catch (err) {
            console.error('Failed to fetch data:', err);
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchData(range);
    }, [fetchData, range]);

    if (isLoading || !summaryData || !chartData) {
        return <div style={{color: 'white'}}>Loading...</div>;
    }

    const handleRangeChange = (newRange: string) => {
        setRange(newRange);
    };

    return (
        <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <h1 style={{ margin: 0 }}>대시보드</h1>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <button onClick={() => handleRangeChange('daily')} className={`btn ${range === 'daily' ? 'btn-primary' : 'btn-secondary'}`}>Daily</button>
                    <button onClick={() => handleRangeChange('weekly')} className={`btn ${range === 'weekly' ? 'btn-primary' : 'btn-secondary'}`}>Weekly</button>
                    <button onClick={() => handleRangeChange('monthly')} className={`btn ${range === 'monthly' ? 'btn-primary' : 'btn-secondary'}`}>Monthly</button>
                    <button onClick={() => handleRangeChange('yearly')} className={`btn ${range === 'yearly' ? 'btn-primary' : 'btn-secondary'}`}>Yearly</button>
                </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '24px', marginBottom: '24px' }}>
                <SummaryCard title="총 등록 장소" value={summaryData.totalPlaces.toLocaleString()} growthRate={summaryData.placesGrowthRate} />
                <SummaryCard title="총 가입자 수" value={summaryData.totalUsers.toLocaleString()} growthRate={summaryData.usersGrowthRate} />
                <SummaryCard title={`신규 장소 (${range})`} value={summaryData.periodPlacesCount.toLocaleString()} growthRate={0} />
            </div>

            {/* --- ▼▼▼ [수정] 새로운 차트 컴포넌트 렌더링 ▼▼▼ --- */}
            <DashboardChart chartData={chartData} range={range} />
        </div>
    );
}