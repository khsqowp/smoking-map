'use client';

import { Chart } from 'react-chartjs-2';
import {
    Chart as ChartJS, CategoryScale, LinearScale, BarElement, LineElement, PointElement, Title, Tooltip, Legend, ChartData, BarController, LineController
} from 'chart.js';

ChartJS.register(
    CategoryScale, LinearScale, BarElement, LineElement, PointElement, Title, Tooltip, Legend, BarController, LineController
);

interface ChartDataPoint {
    label: string;
    newPlaces: number;
    newUsers: number;
    totalPlaces: number;
    totalUsers: number;
}

interface Props {
    chartData: ChartDataPoint[];
    range: string;
}

export default function DashboardChart({ chartData, range }: Props) {
    const options = {
        responsive: true,
        plugins: {
            legend: { position: 'top' as const },
            title: { display: true, text: `${range.toUpperCase()} Trend` },
        },
        scales: {
            y: {
                type: 'linear' as const,
                display: true,
                position: 'left' as const,
                title: { display: true, text: '신규 등록 수' },
                beginAtZero: true,
                ticks: { stepSize: 1 }
            },
            y1: {
                type: 'linear' as const,
                display: true,
                position: 'right' as const,
                title: { display: true, text: '누적 수' },
                grid: { drawOnChartArea: false },
                beginAtZero: true,
            },
        },
    };

    const data: ChartData = {
        labels: chartData.map(d => d.label),
        datasets: [
            {
                type: 'bar' as const,
                label: '신규 장소',
                data: chartData.map(d => d.newPlaces),
                backgroundColor: 'rgba(54, 162, 235, 0.5)',
                yAxisID: 'y',
            },
            {
                type: 'bar' as const,
                label: '신규 가입자',
                data: chartData.map(d => d.newUsers),
                backgroundColor: 'rgba(75, 192, 192, 0.5)',
                yAxisID: 'y',
            },
            {
                type: 'line' as const,
                label: '누적 장소',
                data: chartData.map(d => d.totalPlaces),
                borderColor: 'rgb(255, 99, 132)',
                backgroundColor: 'rgba(255, 99, 132, 0.5)',
                yAxisID: 'y1',
                tension: 0.1
            },
            {
                type: 'line' as const,
                label: '누적 가입자',
                data: chartData.map(d => d.totalUsers),
                borderColor: 'rgb(255, 205, 86)',
                backgroundColor: 'rgba(255, 205, 86, 0.5)',
                yAxisID: 'y1',
                tension: 0.1
            },
        ],
    };

    return (
        <div style={{backgroundColor: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)'}}>
            <Chart type="bar" data={data} options={options} />
        </div>
    );
}