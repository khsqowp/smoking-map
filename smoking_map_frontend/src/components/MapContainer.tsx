'use client';

import Script from 'next/script';
import { useRef, useState, useEffect, useCallback } from 'react';
import PlaceDetail from './PlaceDetail';
import RegisterModal from './RegisterModal';
import { useUser } from '@/context/UserContext';

import { apiClient } from '@/utils/apiClient';
import CurrentLocationButton from './CurrentLocationButton';


// --- 네이버 지도 API 타입 정의 ---
interface NaverLatLng {
    lat: () => number;
    lng: () => number;
}
interface NaverMap {
    getCenter: () => NaverLatLng;
    panTo: (coord: NaverLatLng | { lat: number; lng: number }) => void;
    fitBounds: (bounds: any) => void;
}
interface NaverMarker {
    setMap: (map: NaverMap | null) => void;
    getPosition: () => NaverLatLng;
}
interface NaverLatLngBounds {
    extend: (latlng: NaverLatLng) => void;
}
interface NaverSize {
    width: number;
    height: number;
}
interface NaverPoint {
    x: number;
    y: number;
}

declare global {
    interface Window {
        naver: {
            maps: {
                Map: new (mapDiv: string | HTMLElement, mapOptions?: object) => NaverMap;
                Marker: new (options: object) => NaverMarker;
                LatLng: new (lat: number, lng: number) => NaverLatLng;
                LatLngBounds: new () => NaverLatLngBounds;
                Size: new (width: number, height: number) => NaverSize;
                Point: new (x: number, y: number) => NaverPoint;
                Event: {
                    addListener: (instance: object, eventName: string, handler: () => void) => object;
                    removeListener: (listener: object) => void;
                };
                Position: {
                    TOP_LEFT: number;
                }
            };
        };
    }
}


export type Place = {
    id: number;
    latitude: number;
    longitude: number;
    address: string;
    description: string;
    imageUrls: string[];
    isFavorited: boolean;
    averageRating: number;
    reviewCount: number;
    roadAddress?: string;
    originalAddress?: string;
};

type MapContainerProps = {
    places: Place[];
};

export default function MapContainer({ places }: MapContainerProps) {
    const mapElement = useRef<HTMLDivElement>(null);
    const [map, setMap] = useState<NaverMap | null>(null);
    const markersRef = useRef<NaverMarker[]>([]);
    const [selectedPlace, setSelectedPlace] = useState<Place | null>(null);
    const { user } = useUser();

    const [isRegisterMode, setIsRegisterMode] = useState(false);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [newPlaceCoords, setNewPlaceCoords] = useState<{ lat: number; lng: number } | null>(null);
    const [displayedPlaces, setDisplayedPlaces] = useState<Place[]>(places);
    const [isGettingLocation, setIsGettingLocation] = useState(false);
    const currentLocationMarkerRef = useRef<NaverMarker | null>(null);

    const naverMapClientId = process.env.NEXT_PUBLIC_NAVER_MAP_CLIENT_ID || '';

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
        if (!map) {
            const createdMap = new window.naver.maps.Map(mapElement.current, mapOptions);
            setMap(createdMap);
        }
    }, [map]);

    useEffect(() => {
        if (window.naver && window.naver.maps) {
            initializeMap();
        }
    }, [initializeMap]);

    // --- ▼▼▼ [추가] 사용자 활동 로그 전송 로직 ▼▼▼ ---
    useEffect(() => {
        if (!map) return;

        let debounceTimer: NodeJS.Timeout;

        const logActivity = () => {
            const center = map.getCenter();
            const payload = {
                latitude: center.lat(),
                longitude: center.lng(),
            };
            // 에러가 발생해도 사용자 경험에 영향을 주지 않도록 catch 처리
            apiClient('/api/v1/activity-log', {
                method: 'POST',
                body: payload,
            }).catch(error => console.error("활동 로그 전송 실패:", error));
        };

        const debouncedLogActivity = () => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(logActivity, 1500); // 1.5초 후에 전송
        };

        const dragEndListener = window.naver.maps.Event.addListener(map, 'dragend', debouncedLogActivity);
        const zoomChangedListener = window.naver.maps.Event.addListener(map, 'zoom_changed', debouncedLogActivity);

        return () => {
            window.naver.maps.Event.removeListener(dragEndListener);
            window.naver.maps.Event.removeListener(zoomChangedListener);
            clearTimeout(debounceTimer);
        };
    }, [map]);
    // --- ▲▲▲ [추가] 사용자 활동 로그 전송 로직 ▲▲▲ ---

    useEffect(() => {
        if (!map) return;

        markersRef.current.forEach(marker => marker.setMap(null));

        const newMarkers = displayedPlaces.map((place) => {
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

        markersRef.current = newMarkers;

        const clickListener = window.naver.maps.Event.addListener(map, 'click', () => {
            setSelectedPlace(null);
        });

        if (newMarkers.length > 0 && displayedPlaces !== places) {
            const bounds = new window.naver.maps.LatLngBounds();
            newMarkers.forEach(marker => {
                bounds.extend(marker.getPosition());
            });
            map.fitBounds(bounds);
        }

        return () => {
            if (clickListener) {
                window.naver.maps.Event.removeListener(clickListener);
            }
        };
    }, [map, displayedPlaces, places]);

    const handleFavoriteChange = (placeId: number, isFavorited: boolean) => {
        setDisplayedPlaces(prevPlaces =>
            prevPlaces.map(p =>
                p.id === placeId ? { ...p, isFavorited } : p
            )
        );
        if (selectedPlace?.id === placeId) {
            setSelectedPlace(prev => prev ? { ...prev, isFavorited } : null);
        }
    };

    const handleCurrentLocation = () => {
        if (!map) return;

        setIsGettingLocation(true);

        navigator.geolocation.getCurrentPosition(
            (position) => {
                const { latitude, longitude } = position.coords;
                const currentLocation = new window.naver.maps.LatLng(latitude, longitude);

                map.panTo(currentLocation);

                if (currentLocationMarkerRef.current) {
                    currentLocationMarkerRef.current.setMap(null);
                }

                const marker = new window.naver.maps.Marker({
                    position: currentLocation,
                    map: map,
                    icon: {
                        url: '/images/current-location-marker.png',
                        scaledSize: new window.naver.maps.Size(32, 32),
                    }

                });

                currentLocationMarkerRef.current = marker;

                setIsGettingLocation(false);
            },
            (error) => {
                console.error("Geolocation error:", error);
                alert('현재 위치를 가져오는 데 실패했습니다.');
                setIsGettingLocation(false);
            },
            {
                enableHighAccuracy: true,
                timeout: 10000,
                maximumAge: 0
            }
        );
    };

    const handleDataChange = async () => {
        try {
            const updatedPlaces = await apiClient('/api/v1/places');
            setDisplayedPlaces(updatedPlaces);

            if (selectedPlace) {
                const updatedSelectedPlace = updatedPlaces.find((p: Place) => p.id === selectedPlace.id);
                if (updatedSelectedPlace) {
                    setSelectedPlace(updatedSelectedPlace);
                }
            }
        } catch (error) {
            console.error("Failed to refetch places:", error);
            alert('데이터를 새로고침하는 데 실패했습니다.');
        }
    };

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

    // const handleSearch = async (keyword: string) => {
    //     try {
    //         const results = await apiClient(`/api/v1/places/search?keyword=${encodeURIComponent(keyword)}`);
    //         if (results.length === 0) {
    //             alert('검색 결과가 없습니다.');
    //             setDisplayedPlaces(places);
    //         } else {
    //             setDisplayedPlaces(results);
    //         }
    //     } catch (error) {
    //         alert('검색 중 오류가 발생했습니다.');
    //         console.error(error);
    //     }
    // };

    if (!naverMapClientId) {
        return (
            <div style={{ width: '100%', height: 'calc(100vh - 64px)' }}>
                <p style={{ color: 'red', fontWeight: 'bold' }}>네이버 맵 Client ID가 설정되지 않았습니다.</p>
            </div>
        );
    }

    return (
        <div style={{position: 'absolute', top: 0, left: 0, width: '100%', height: '100%'}}>
            <Script
                strategy="afterInteractive"
                type="text/javascript"
                src={`https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${naverMapClientId}&submodules=geocoder`}
                onLoad={initializeMap}
            />

            {/*<SearchControl onSearch={handleSearch} />*/}

            <CurrentLocationButton onClick={handleCurrentLocation} isLoading={isGettingLocation} />

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
                        <button className="btn btn-secondary" onClick={handleCancelRegister}>취소</button>
                        <button className="btn btn-primary" onClick={handleConfirmLocation}>위치 확정</button>
                    </div>
                </>
            )}

            <PlaceDetail
                place={selectedPlace}
                onClose={() => setSelectedPlace(null)}
                onFavoriteChange={handleFavoriteChange}
                onDataChange={handleDataChange}
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