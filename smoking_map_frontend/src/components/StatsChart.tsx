'use client';

import { Bar } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend,
} from 'chart.js';

ChartJS.register(
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend
);

interface StatsData {
    placesDaily: number;
    placesWeekly: number;
    placesMonthly: number;
    placesYearly: number;
    usersDaily: number;
    usersWeekly: number;
    usersMonthly: number;
    usersYearly: number;
}

export default function StatsChart({ statsData }: { statsData: StatsData }) {
    const options = {
        responsive: true,
        plugins: {
            legend: {
                position: 'top' as const,
            },
            title: {
                display: true,
                text: '기간별 등록 현황',
            },
        },
        scales: {
            y: {
                beginAtZero: true,
                ticks: {
                    stepSize: 1, // y축 눈금을 1 단위로 설정
                },
            },
        },
    };

    const labels = ['오늘', '이번 주', '이번 달', '올해'];

    const data = {
        labels,
        datasets: [
            {
                label: '신규 장소',
                data: [statsData.placesDaily, statsData.placesWeekly, statsData.placesMonthly, statsData.placesYearly],
                backgroundColor: 'rgba(54, 162, 235, 0.5)',
            },
            {
                label: '신규 가입자',
                data: [statsData.usersDaily, statsData.usersWeekly, statsData.usersMonthly, statsData.usersYearly],
                backgroundColor: 'rgba(255, 99, 132, 0.5)',
            },
        ],
    };

    return (
        <div style={{backgroundColor: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)'}}>
            <Bar options={options} data={data} />
        </div>
    );
}