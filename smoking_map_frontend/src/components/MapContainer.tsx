'use client';

import Script from 'next/script';
import { useRef, useState, useEffect } from 'react';
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
    const [markers, setMarkers] = useState<any[]>([]); // 마커 인스턴스들을 상태로 관리
    const [selectedPlace, setSelectedPlace] = useState<Place | null>(null);
    const { user } = useUser();

    const [isRegisterMode, setIsRegisterMode] = useState(false);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [newPlaceCoords, setNewPlaceCoords] = useState<{ lat: number; lng: number } | null>(null);

    const naverMapClientId = process.env.NEXT_PUBLIC_NAVER_MAP_CLIENT_ID || '';

    const initializeMap = () => {
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
        const createdMap = new window.naver.maps.Map(mapElement.current, mapOptions);
        setMap(createdMap);
    };

    // 지도 초기화 및 마커 생성 Effect
    useEffect(() => {
        if (!map) return;

        // 기존 마커들 정리
        markers.forEach(marker => marker.setMap(null));

        const newMarkers = places.map((place) => {
            const marker = new window.naver.maps.Marker({
                position: new window.naver.maps.LatLng(place.latitude, place.longitude),
                map: map,
            });

            // 이벤트 리스너 추가
            window.naver.maps.Event.addListener(marker, 'click', () => {
                // 디버깅을 위해 콘솔 로그 추가
                console.log('마커 클릭됨:', place);
                setSelectedPlace(place);
                map.panTo(marker.getPosition());
            });
            return marker;
        });

        setMarkers(newMarkers); // 새로 생성된 마커들을 상태에 저장

        // 지도 클릭 시 정보창 닫기
        window.naver.maps.Event.addListener(map, 'click', () => {
            setSelectedPlace(null);
        });

        // eslint-disable-next-line react-hooks/exhaustive-deps
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
            <div style={{ width: '100%', height: 'calc(100vh - 64px)', /*...*/ }}>
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
                onLoad={initializeMap}
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

            {/* PlaceDetail 컴포넌트는 항상 렌더링하되, place prop에 따라 내부적으로 보임/숨김 처리 */}
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