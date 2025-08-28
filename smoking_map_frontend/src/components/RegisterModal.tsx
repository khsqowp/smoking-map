// src/components/RegisterModal.tsx

'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { apiClient } from '@/utils/apiClient';

type RegisterModalProps = {
    coords: { lat: number; lng: number };
    onClose: () => void;
};

export default function RegisterModal({ coords, onClose }: RegisterModalProps) {
    const [description, setDescription] = useState('');
    const [imageFiles, setImageFiles] = useState<File[]>([]);
    const [imagePreviews, setImagePreviews] = useState<string[]>([]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const router = useRouter();

    useEffect(() => {
        return () => {
            imagePreviews.forEach(url => URL.revokeObjectURL(url));
        };
    }, [imagePreviews]);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files) {
            const files = Array.from(e.target.files);
            const validImageFiles = files.filter(file => file.type.startsWith('image/'));

            if (files.length !== validImageFiles.length) {
                alert('이미지 파일만 업로드할 수 있습니다.');
            }

            setImageFiles(validImageFiles);
            const previews = validImageFiles.map(file => URL.createObjectURL(file));
            setImagePreviews(previews);
        }
    };

    const handleSubmit = async () => {
        if (!description.trim()) {
            alert('상세 설명을 입력해주세요.');
            return;
        }
        setIsSubmitting(true);

        const formData = new FormData();
        const requestDto = {
            latitude: coords.lat,
            longitude: coords.lng,
            originalAddress: `위도: ${coords.lat.toFixed(6)}, 경도: ${coords.lng.toFixed(6)}`,
            description: description,
        };
        formData.append('requestDto', new Blob([JSON.stringify(requestDto)], { type: 'application/json' }));
        imageFiles.forEach(file => {
            formData.append('images', file);
        });

        try {
            await apiClient('/api/v1/places', {
                method: 'POST',
                body: formData,
            });
            alert('새로운 흡연구역이 등록되었습니다.');
            onClose();
            router.refresh();
        } catch (err) {
            console.error('장소 등록 중 오류 발생:', err);
            if (err instanceof Error) {
                alert(`등록 실패: ${err.message || '서버 오류가 발생했습니다.'}`);
            } else {
                alert('알 수 없는 오류로 등록에 실패했습니다.');
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content" style={{ backgroundColor: '#2a2a2a', color: '#E0E0E0', border: '1px solid #444' }}>
                <h2 style={{ textAlign: 'center', marginTop: '0', marginBottom: '30px' }}>흡연구역 세부 정보 등록</h2>

                <div className="modal-section">
                    <p className="modal-section-title" style={{ color: '#E0E0E0', margin: '0 0 10px 0' }}>사진 ({imageFiles.length}장)</p>
                    <div className="file-input-wrapper">
                        <label htmlFor="imageUpload" className="btn btn-secondary file-input-label">
                            파일 선택
                        </label>
                        <span className="file-name-display">
                            {imageFiles.length > 0 ? `${imageFiles.length}개 파일 선택됨` : '선택된 파일 없음'}
                        </span>
                        <input
                            id="imageUpload"
                            type="file"
                            accept="image/*"
                            multiple
                            onChange={handleFileChange}
                            className="file-input-hidden"
                        />
                    </div>
                    {imagePreviews.length > 0 && (
                        <div style={{ marginTop: '15px', display: 'flex', gap: '10px', overflowX: 'auto', padding: '5px' }}>
                            {imagePreviews.map((previewUrl, index) => (
                                <img
                                    key={index}
                                    src={previewUrl}
                                    alt={`preview ${index}`}
                                    style={{ width: '100px', height: '100px', objectFit: 'cover', borderRadius: '8px' }}
                                />
                            ))}
                        </div>
                    )}
                </div>

                <div className="modal-section">
                    <p className="modal-section-title" style={{ color: '#E0E0E0', margin: '0 0 10px 0' }}>상세설명</p>
                    <textarea
                        className="modal-textarea"
                        placeholder="이곳의 특징을 설명해주세요. (예: 벤치 있음, 지붕 있음 등)"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                    />
                </div>

                <div className="modal-actions" style={{marginTop: '20px'}}>
                    <button className="btn btn-secondary" onClick={onClose} disabled={isSubmitting}>취소</button>
                    <button className="btn btn-primary" onClick={handleSubmit} disabled={isSubmitting}>
                        {isSubmitting ? '등록 중...' : '최종 등록'}
                    </button>
                </div>
            </div>
        </div>
    );
}