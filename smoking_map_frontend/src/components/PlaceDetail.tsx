// src/app/PlaceDetail.tsx
'use client';

import React, { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { Place } from './MapContainer';
import { useUser } from '@/context/UserContext';
import EditRequestModal from '@/components/EditRequestModal';
import { apiClient } from '@/utils/apiClient';
import StarRating from './StarRating'; // --- ▼▼▼ [추가] StarRating import ▼▼▼ --
import ReviewList from './ReviewList'; // --- ▼▼▼ [추가] ReviewList import ▼▼▼ ---// -
import ReviewFormModal from './ReviewFormModal'; // --- ▼▼▼ [추가] ReviewFormModal import ▼▼▼ ---


const StarIcon = ({ filled }: { filled: boolean }) => (
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"
        fill={filled ? "#FFD700" : "none"} stroke={filled ? "#FFD700" : "#E0E0E0"}
        strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
    </svg>
);

type PlaceDetailProps = {
    place: Place | null;
    onClose: () => void;
    onFavoriteChange: (placeId: number, isFavorited: boolean) => void;
    onDataChange: () => void; // --- ▼▼▼ [수정] 이 줄을 추가해주세요 ▼▼▼ ---
};

const ReportModal = ({ placeId, onClose }: { placeId: number, onClose: () => void }) => {
    const [reportType, setReportType] = useState('DISAPPEARED');
    const [content, setContent] = useState('');

    const handleSubmit = async () => {
        if (reportType === 'OTHER' && !content.trim()) {
            alert('기타 신고 내용은 반드시 입력해야 합니다.');
            return;
        }
        try {
            await apiClient(`/api/v1/places/${placeId}/report`, {
                method: 'POST',
                body: { type: reportType, content: reportType === 'OTHER' ? content : '' },
            });
            alert('신고가 정상적으로 접수되었습니다.');
            onClose();
        } catch (err) {
            if (err instanceof Error) {
                alert(`오류: ${err.message}`);
            } else {
                alert('알 수 없는 오류가 발생했습니다.');
            }
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content" style={{ backgroundColor: '#2a2a2a', color: '#E0E0E0', border: '1px solid #444' }}>
                <h2 style={{textAlign: 'center', marginTop: '0'}}>잘못된 정보 신고 (장소 ID: {placeId})</h2>
                <div style={{display: 'flex', flexDirection: 'column', gap: '10px', alignItems: 'flex-start'}}>
                    <label><input type="radio" name="reportType" value="INCORRECT" checked={reportType === 'INCORRECT'} onChange={(e) => setReportType(e.target.value)} /> 잘못된 정보</label>
                    <label><input type="radio" name="reportType" value="DISAPPEARED" checked={reportType === 'DISAPPEARED'} onChange={(e) => setReportType(e.target.value)} /> 사라진 흡연구역</label>
                    <label><input type="radio" name="reportType" value="OTHER" checked={reportType === 'OTHER'} onChange={(e) => setReportType(e.target.value)} /> 기타</label>
                </div>
                {reportType === 'OTHER' && (
                    <textarea
                        placeholder="상세 내용을 입력해주세요."
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        className="modal-textarea"
                    />
                )}
                <div className="modal-actions">
                    <button onClick={onClose} className="btn btn-secondary">취소</button>
                    <button onClick={handleSubmit} className="btn btn-primary">제출</button>
                </div>
            </div>
        </div>
    );
};

async function getAddressFromCoords(latitude: number, longitude: number): Promise<string> {
    try {
        const res = await fetch(`/api/v1/geocode?lat=${latitude}&lng=${longitude}`);
        if (!res.ok) return "주소를 불러올 수 없습니다.";
        const data = await res.json();
        return data.address || "주소 정보 없음";
    } catch (error) {
        console.error("Error fetching address:", error);
        return "주소 변환 중 오류 발생";
    }
}

// --- ▼▼▼ [수정] 함수 파라미터에 'onDataChange' 추가 ▼▼▼ ---
export default function PlaceDetail({ place, onClose, onFavoriteChange, onDataChange }: PlaceDetailProps) {
    const [address, setAddress] = useState('주소 로딩 중...');
    const [isReportModalOpen, setIsReportModalOpen] = useState(false);
    const [isEditRequestModalOpen, setIsEditRequestModalOpen] = useState(false);
    const { user } = useUser();
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [isFavorited, setIsFavorited] = useState(place?.isFavorited || false);
    const [isReviewModalOpen, setIsReviewModalOpen] = useState(false);
    const router = useRouter();
    const [reviewRefreshKey, setReviewRefreshKey] = useState(0);


    useEffect(() => {
        if (place) {
            apiClient(`/api/v1/places/${place.id}/view`, { method: 'POST' }).catch(console.error);

            if (place.roadAddress && place.roadAddress !== "주소 정보 없음") {
                setAddress(place.roadAddress);
            } else {
                getAddressFromCoords(place.latitude, place.longitude).then(setAddress);
            }
            setIsFavorited(place.isFavorited);
        }
    }, [place]);

    if (!place) return null;

    const handleToggleFavorite = async () => {
        if (!user) {
            if(confirm('로그인이 필요한 기능입니다. 로그인 페이지로 이동하시겠습니까?')) {
                window.location.href = '/oauth2/authorization/google';
            }
            return;
        }

        const newState = !isFavorited;
        setIsFavorited(newState);

        try {
            await apiClient(`/api/v1/places/${place.id}/favorite`, {
                method: newState ? 'POST' : 'DELETE',
            });
            onFavoriteChange(place.id, newState);
        } catch (err) {
            setIsFavorited(!newState);
            alert(`오류: ${err instanceof Error ? err.message : '알 수 없는 오류'}`);
        }
    };

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
        const files = event.target.files;
        if (!files || files.length === 0) return;

        const formData = new FormData();
        Array.from(files).forEach(file => {
            formData.append('images', file);
        });

        try {
            await apiClient(`/api/v1/places/${place.id}/images`, {
                method: 'POST',
                body: formData,
            });

            alert(`${files.length}개의 이미지가 성공적으로 추가되었습니다.`);
            onClose();
            router.refresh();
        } catch (err) {
            if (err instanceof Error) {
                alert(`업로드 실패: ${err.message || '서버 오류'}`);
            } else {
                alert('알 수 없는 오류가 발생했습니다.');
            }
        } finally {
            if(event.target) event.target.value = '';
        }
    };

    const handleRequestEdit = () => {
        if (user) {
            setIsEditRequestModalOpen(true);
        }
    };

    // --- ▼▼▼ [추가] 리뷰 작성 성공 시 호출될 함수 ▼▼▼ ---
    const handleReviewSuccess = () => {
        setIsReviewModalOpen(false);
        onDataChange(); // 부모의 데이터 새로고침 함수 호출
        setReviewRefreshKey(prev => prev + 1); // 리뷰 목록 컴포넌트 새로고침
    };

    return (
        <>
            <div className="modal-overlay" onClick={onClose}>
                <div className="info-popup" onClick={(e) => e.stopPropagation()}>
                    <button onClick={handleToggleFavorite} style={{
                        position: 'absolute', top: '15px', right: '15px',
                        background: 'rgba(0,0,0,0.5)', border: 'none', borderRadius: '50%',
                        width: '40px', height: '40px', cursor: 'pointer',
                        display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 10
                    }}>
                        <StarIcon filled={isFavorited}/>
                    </button>
                    <div className="image-section">
                        {place.imageUrls && place.imageUrls.length > 0 ? (
                            <img src={place.imageUrls[0]} alt="흡연구역 이미지"/>
                        ) : (
                            <div className="no-image-placeholder">이미지 없음</div>
                        )} {user && (
                        <>
                            <button onClick={handleAddImageClick} className="add-image-btn">+</button>
                            <input ref={fileInputRef} type="file" accept="image/*" multiple style={{display: 'none'}}
                                onChange={handleFileChange}/>
                        </>
                    )}
                    </div>
                    <div className="address-section">
                        <h4>{address}</h4>
                        <div style={{
                            display: 'flex',
                            justifyContent: 'center',
                            alignItems: 'center',
                            gap: '8px',
                            marginTop: '5px'
                        }}>
                            <StarRating rating={place.averageRating}/> <span
                            style={{fontWeight: 'bold', color: '#FFD700'}}>{place.averageRating.toFixed(1)}</span> <span
                            style={{color: '#A0AEC0'}}>({place.reviewCount}개)</span>
                        </div>
                    </div>
                    <div className="description-section">
                        <div className="description-box-readonly">
                            {place.description || "상세 설명이 없습니다."}
                        </div>
                        {user && (
                            <button onClick={handleRequestEdit}
                                className="btn btn-secondary edit-request-btn">수정 요청</button>
                        )}
                    </div>
                    <ReviewList placeId={place.id} refreshKey={reviewRefreshKey} />
                    <div className="bottom-actions">
                        {user ? (
                            <div style={{display: 'flex', gap: '10px'}}>
                                <button onClick={() => setIsReviewModalOpen(true)} className="btn btn-primary">리뷰 작성
                                </button>
                                <button onClick={() => setIsReportModalOpen(true)} className="btn btn-danger">정보 신고
                                </button>
                            </div>
                        ) : (
                            <div></div>
                        )}
                        <button onClick={onClose} className="btn btn-secondary">닫기</button>
                    </div>
                </div>
            </div>
            {isReviewModalOpen && (
                <ReviewFormModal
                    placeId={place.id}
                    onClose={() => setIsReviewModalOpen(false)}
                    onSuccess={handleReviewSuccess}
                />
            )}
        </>
    );
}