// src/app/PlaceDetail.tsx
'use client';

import React, { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { Place } from './MapContainer';
import { useUser } from '@/context/UserContext';
import EditRequestModal from '@/components/EditRequestModal';

type PlaceDetailProps = {
    place: Place | null;
    onClose: () => void;
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
            const res = await fetch(`/api/v1/places/${placeId}/report`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ type: reportType, content: reportType === 'OTHER' ? content : '' }),
            });
            if (!res.ok) throw new Error('신고 제출에 실패했습니다.');
            alert('신고가 정상적으로 접수되었습니다.');
            onClose();
        } catch (err: any) {
            alert(`오류: ${err.message}`);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <h2>잘못된 정보 신고 (장소 ID: {placeId})</h2>
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
                        className="description-box"
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

export default function PlaceDetail({ place, onClose }: PlaceDetailProps) {
    const [address, setAddress] = useState('주소 로딩 중...');
    const [isReportModalOpen, setIsReportModalOpen] = useState(false);
    const [isEditRequestModalOpen, setIsEditRequestModalOpen] = useState(false);
    const { user } = useUser();
    const router = useRouter();
    const fileInputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        if (place) {
            fetch(`/api/v1/places/${place.id}/view`, { method: 'POST', credentials: 'include' });

            if (place.roadAddress && place.roadAddress !== "주소 정보 없음") {
                setAddress(place.roadAddress);
            } else {
                getAddressFromCoords(place.latitude, place.longitude).then(setAddress);
            }
        }
    }, [place]);

    if (!place) return null;

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
            const response = await fetch(`/api/v1/places/${place.id}/images`, {
                method: 'POST',
                credentials: 'include',
                body: formData,
            });

            if (response.ok) {
                alert(`${files.length}개의 이미지가 성공적으로 추가되었습니다.`);
                onClose();
                router.refresh();
            } else {
                const errorData = await response.json();
                alert(`업로드 실패: ${errorData.message || '서버 오류'}`);
            }
        } catch (error) {
            alert('이미지 업로드 중 오류가 발생했습니다.');
        } finally {
            if(event.target) event.target.value = '';
        }
    };

    const handleRequestEdit = () => {
        if (user) {
            setIsEditRequestModalOpen(true);
        }
    };

    return (
        <>
            <div className="modal-overlay" onClick={onClose}>
                <div className="info-popup" onClick={(e) => e.stopPropagation()}>

                    <div className="image-section">
                        {place.imageUrls && place.imageUrls.length > 0 ? (
                            <img src={place.imageUrls[0]} alt="흡연구역 이미지" />
                        ) : (
                            <div className="no-image-placeholder">이미지 없음</div>
                        )}
                        {user && (
                            <>
                                <button onClick={handleAddImageClick} className="add-image-btn">+</button>
                                <input ref={fileInputRef} type="file" accept="image/*" multiple style={{ display: 'none' }} onChange={handleFileChange} />
                            </>
                        )}
                    </div>

                    <div className="address-section">
                        <h4>{address}</h4>
                    </div>

                    <div className="description-section">
                        <div className="description-box-readonly">
                            {place.description || "상세 설명이 없습니다."}
                        </div>
                        {user && (
                            <button onClick={handleRequestEdit} className="btn btn-secondary edit-request-btn">수정 요청</button>
                        )}
                    </div>

                    <div className="bottom-actions">
                        {user ? (
                            <button onClick={() => setIsReportModalOpen(true)} className="btn btn-danger">수정/신고</button>
                        ) : (
                            <div></div>
                        )}
                        <button onClick={onClose} className="btn btn-secondary">닫기</button>
                    </div>
                </div>
            </div>
            {isReportModalOpen && (
                <ReportModal placeId={place.id} onClose={() => setIsReportModalOpen(false)} />
            )}
            {isEditRequestModalOpen && (
                <EditRequestModal placeId={place.id} onClose={() => setIsEditRequestModalOpen(false)} />
            )}
        </>
    );
}