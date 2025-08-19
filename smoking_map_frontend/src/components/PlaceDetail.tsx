'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { Place } from './MapContainer';
import { useUser } from '@/context/UserContext';

type PlaceDetailProps = {
    place: Place | null;
    onClose: () => void;
};

async function getAddressFromCoords(latitude: number, longitude: number): Promise<string> {
    try {
        const res = await fetch(`http://localhost:8080/api/v1/geocode?lat=${latitude}&lng=${longitude}`);
        if (!res.ok) return "주소를 불러올 수 없습니다.";
        const data = await res.json();
        return data.address || "주소 정보 없음";
    } catch (error) {
        console.error("Error fetching address:", error);
        return "주소 변환 중 오류 발생";
    }
}

export default function PlaceDetail({ place, onClose }: PlaceDetailProps) {
    const [currentImageIndex, setCurrentImageIndex] = useState(0);
    const [address, setAddress] = useState('');
    const { user } = useUser();
    const router = useRouter();
    const fileInputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        if (place) {
            setCurrentImageIndex(0);
            // roadAddress 필드가 있으면 그것을 사용하고, 없으면 API 호출
            if (place.roadAddress && place.roadAddress !== "주소 정보 없음") {
                setAddress(place.roadAddress);
            } else {
                getAddressFromCoords(place.latitude, place.longitude).then(setAddress);
            }
        }
    }, [place]);

    // place prop이 null이면 컴포넌트 자체를 렌더링하지 않음
    if (!place) {
        return null;
    }

    const handleAddImageClick = () => {
        if (user) {
            fileInputRef.current?.click();
        } else {
            if(confirm('로그인이 필요한 기능입니다. 로그인 페이지로 이동하시겠습니까?')) {
                window.location.href = '/oauth2/authorization/google';
            }
        }
    };

    const handleFileChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
        // 파일 업로드 로직 (이전과 동일)
    };

    const handleRequestEdit = () => {
        // TODO: 수정 요청 기능 구현
        alert('수정 요청 기능은 현재 개발 중입니다.');
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="info-popup" onClick={(e) => e.stopPropagation()}>
                {/* 상단: 이미지 */}
                <div className="image-section">
                    {place.imageUrls && place.imageUrls.length > 0 ? (
                        <img src={place.imageUrls[currentImageIndex]} alt="흡연구역 이미지" />
                    ) : (
                        <div className="no-image-placeholder">이미지 없음</div>
                    )}
                    <button onClick={handleAddImageClick} className="add-image-btn-popup">+</button>
                    <input ref={fileInputRef} type="file" accept="image/*" multiple style={{ display: 'none' }} onChange={handleFileChange} />
                </div>

                {/* 중간: 주소 */}
                <div className="address-section">
                    <h4>{address || place.originalAddress || "주소 로딩 중..."}</h4>
                </div>

                {/* 하단: 상세설명 */}
                <div className="description-section">
                    <div className="description-box-readonly">
                        {place.description || "상세 설명이 없습니다."}
                    </div>
                    <button onClick={handleRequestEdit} className="edit-request-btn">수정 요청</button>
                </div>

                {/* 닫기 버튼 */}
                <div className="bottom-actions">
                    <button onClick={onClose} className="close-btn-bottom">닫기</button>
                </div>
            </div>
        </div>
    );
}