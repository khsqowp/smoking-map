// src/app/MapContainer.tsx

'use client';

import Script from 'next/script';
import { useRef, useState, useEffect, useCallback } from 'react'; // useCallback 추가
import PlaceDetail from './PlaceDetail';
import RegisterModal from './RegisterModal';
import { useUser } from '@/context/UserContext';

declare global {
    interface Window {
        naver: any;
    }
}

export type Place = {
    id: number;
    latitude: number;
    longitude: number;
    address: string;
    description: string;
    imageUrls: string[];
    roadAddress?: string;
    originalAddress?: string;
};

type MapContainerProps = {
    places: Place[];
};

export default function MapContainer({ places }: MapContainerProps) {
    const mapElement = useRef<HTMLDivElement>(null);
    const [map, setMap] = useState<any>(null);
    const [markers, setMarkers] = useState<any[]>([]);
    const [selectedPlace, setSelectedPlace] = useState<Place | null>(null);
    const { user } = useUser();

    const [isRegisterMode, setIsRegisterMode] = useState(false);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [newPlaceCoords, setNewPlaceCoords] = useState<{ lat: number; lng: number } | null>(null);

    const naverMapClientId = process.env.NEXT_PUBLIC_NAVER_MAP_CLIENT_ID || '';

    // --- ▼▼▼ [수정] 지도 초기화 로직을 useCallback으로 감싸기 ▼▼▼ ---
    const initializeMap = useCallback(() => {
        if (!mapElement.current || !window.naver) return;

        const mapOptions = {
            center: new window.naver.maps.LatLng(37.5665, 126.9780),
            zoom: 15,
            minZoom: 6,
            scaleControl: false,
            logoControl: true,
            logoControlOptions: { position: window.naver.maps.Position.TOP_LEFT },
            mapDataControl: false,
        };
        // map 상태가 이미 존재하면 새로 생성하지 않도록 방지
        if (!map) {
            const createdMap = new window.naver.maps.Map(mapElement.current, mapOptions);
            setMap(createdMap);
        }
    }, [map]);
    // --- ▲▲▲ [수정] 지도 초기화 로직을 useCallback으로 감싸기 ▲▲▲ ---

    // --- ▼▼▼ [수정] 스크립트 로드 후 지도를 초기화하는 useEffect 추가 ▼▼▼ ---
    useEffect(() => {
        // window.naver 객체가 로드되었는지 확인 후 지도 초기화
        if (window.naver && window.naver.maps) {
            initializeMap();
        }
    }, [initializeMap]);
    // --- ▲▲▲ [수정] 스크립트 로드 후 지도를 초기화하는 useEffect 추가 ▲▲▲ ---


    // 마커 생성 Effect (기존 로직 유지)
    useEffect(() => {
        if (!map) return;

        markers.forEach(marker => marker.setMap(null));

        const newMarkers = places.map((place) => {
            const marker = new window.naver.maps.Marker({
                position: new window.naver.maps.LatLng(place.latitude, place.longitude),
                map: map,
            });

            window.naver.maps.Event.addListener(marker, 'click', () => {
                setSelectedPlace(place);
                map.panTo(marker.getPosition());
            });
            return marker;
        });

        setMarkers(newMarkers);

        window.naver.maps.Event.addListener(map, 'click', () => {
            setSelectedPlace(null);
        });

    }, [map, places]);


    const handleStartRegister = () => setIsRegisterMode(true);
    const handleCancelRegister = () => setIsRegisterMode(false);

    const handleConfirmLocation = () => {
        if (!map) return;
        const center = map.getCenter();
        setNewPlaceCoords({ lat: center.lat(), lng: center.lng() });
        setIsRegisterMode(false);
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setNewPlaceCoords(null);
    };

    if (!naverMapClientId) {
        return (
            <div style={{ width: '100%', height: 'calc(100vh - 64px)' }}>
                <p style={{ color: 'red', fontWeight: 'bold' }}>네이버 맵 Client ID가 설정되지 않았습니다.</p>
            </div>
        );
    }

    return (
        <div style={{ position: 'relative', width: '100%', height: 'calc(100vh - 64px)' }}>
            <Script
                strategy="afterInteractive"
                type="text/javascript"
                src={`https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${naverMapClientId}&submodules=geocoder`}
                onLoad={initializeMap} // onLoad는 최초 로드 시에만 사용하고, 이후에는 useEffect가 처리
            />
            <div ref={mapElement} style={{ width: '100%', height: '100%' }} />

            {user && !isRegisterMode && (
                <button onClick={handleStartRegister} className="fab">+</button>
            )}

            {isRegisterMode && (
                <>
                    <div className="center-pin-container">
                        <div className="center-pin-label">이곳에 흡연구역 등록</div>
                        <div className="center-pin"></div>
                    </div>
                    <div className="register-actions">
                        <button className="cancel-btn" onClick={handleCancelRegister}>취소</button>
                        <button className="register-btn" onClick={handleConfirmLocation}>위치 확정</button>
                    </div>
                </>
            )}

            <PlaceDetail
                place={selectedPlace}
                onClose={() => setSelectedPlace(null)}
            />

            {isModalOpen && newPlaceCoords && (
                <RegisterModal
                    coords={newPlaceCoords}
                    onClose={handleCloseModal}
                />
            )}
        </div>
    );
}