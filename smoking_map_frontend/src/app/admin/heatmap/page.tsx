'use client';

import { useState, useEffect, useRef, useCallback } from 'react';
import Script from 'next/script';
import { apiClient } from '@/utils/apiClient';

// --- Naver Maps API 타입 정의 ---
interface NaverMap {
    setCenter: (coord: any) => void;
    getCenter: () => any;
    panTo: (coord: any) => void;
    fitBounds: (bounds: any) => void;
}
interface NaverLatLng {
    // Naver Maps LatLng 객체는 직접적인 속성 접근 대신 메서드를 사용할 수 있습니다.
    // 여기서는 생성자만 정의하여 타입 체크를 돕습니다.
}
interface NaverHeatMap {
    setMap: (map: NaverMap | null) => void;
}
interface NaverMapForHeatmap {
    maps: {
        Map: new (mapDiv: string | HTMLElement, mapOptions?: object) => NaverMap;
        LatLng: new (lat: number, lng: number) => NaverLatLng;
        visualization: {
            HeatMap: new (options: object) => NaverHeatMap;
        };
    };
}

type HeatmapData = {
    latitude: number;
    longitude: number;
};

export default function AdminHeatmapPage() {
    const mapElement = useRef<HTMLDivElement>(null);
    const [map, setMap] = useState<NaverMap | null>(null);
    const [heatmapData, setHeatmapData] = useState<HeatmapData[]>([]);
    const naverMapClientId = process.env.NEXT_PUBLIC_NAVER_MAP_CLIENT_ID || '';

    // 지도 초기화 함수
    const initializeMap = useCallback(() => {
        if (!mapElement.current || !(window as any).naver) return;

        const mapOptions = {
            center: new (window as any).naver.maps.LatLng(37.5665, 126.9780), // 서울 중심
            zoom: 7,
        };
        const createdMap = new (window as any).naver.maps.Map(mapElement.current, mapOptions);
        setMap(createdMap);
    }, []);

    // 데이터 로딩 및 히트맵 생성
    useEffect(() => {
        // 1. 히트맵 데이터 가져오기
        apiClient('/api/v1/admin/activity-logs/heatmap')
            .then(data => {
                setHeatmapData(data);
            })
            .catch(err => {
                console.error("Failed to fetch heatmap data:", err);
                alert("히트맵 데이터를 불러오는 데 실패했습니다.");
            });
    }, []);

    // 2. 지도와 데이터가 준비되면 히트맵 그리기
    useEffect(() => {
        if (!map || heatmapData.length === 0 || !(window as any).naver?.maps?.visualization) return;

        const latLngData = heatmapData.map(
            item => new (window as any).naver.maps.LatLng(item.latitude, item.longitude)
        );

        const heatmap = new (window as any).naver.maps.visualization.HeatMap({
            map: map,
            data: latLngData,
            opacity: 0.8,
            radius: 15,
        });

        // 컴포넌트 언마운트 시 히트맵 레이어 제거
        return () => {
            heatmap.setMap(null);
        };

    }, [map, heatmapData]);

    return (
        <div>
            <Script
                strategy="afterInteractive"
                type="text/javascript"
                src={`https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${naverMapClientId}&submodules=visualization`}
                onLoad={initializeMap}
            />
            <h1 style={{ marginBottom: '20px' }}>사용자 활동 히트맵</h1>
            <div ref={mapElement} style={{ width: '100%', height: '70vh', minHeight: '600px' }} />
        </div>
    );
}
