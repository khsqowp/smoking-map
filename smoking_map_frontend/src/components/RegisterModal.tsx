// RegisterModal.tsx

'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';

type RegisterModalProps = {
    coords: { lat: number; lng: number };
    onClose: () => void;
};

export default function RegisterModal({ coords, onClose }: RegisterModalProps) {
    const [description, setDescription] = useState('');
    const [imageFiles, setImageFiles] = useState<File[]>([]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const router = useRouter();

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files) {
            const files = Array.from(e.target.files);
            const validImageFiles = files.filter(file => file.type.startsWith('image/'));
            if (files.length !== validImageFiles.length) {
                alert('이미지 파일만 업로드할 수 있습니다.');
            }
            setImageFiles(validImageFiles);
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
            const response = await fetch('/api/v1/places', {
                method: 'POST',
                credentials: 'include',
                body: formData,
            });

            if (response.ok) {
                alert('새로운 흡연구역이 등록되었습니다.');
                onClose();
                router.refresh();
            } else {
                const error = await response.json();
                alert(`등록 실패: ${error.message || '서버 오류가 발생했습니다.'}`);
            }
        } catch (error) {
            console.error('장소 등록 중 오류 발생:', error);
            alert('장소 등록 중 오류가 발생했습니다.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="modal-overlay">
            {/* --- ▼▼▼ [수정] className="modal-content" 적용 ▼▼▼ --- */}
            <div className="modal-content">
                <h2>흡연구역 세부 정보 등록</h2>
                <div>
                    <p className="modal-section-title">사진 ({imageFiles.length}장)</p>
                    <input
                        type="file"
                        accept="image/*"
                        multiple
                        onChange={handleFileChange}
                    />
                    <div style={{ marginTop: '10px', display: 'flex', gap: '5px', overflowX: 'auto' }}>
                        {imageFiles.map((file, index) => (
                            <img key={index} src={URL.createObjectURL(file)} alt={`preview ${index}`} style={{ width: '80px', height: '80px', objectFit: 'cover' }} />
                        ))}
                    </div>
                </div>
                <div>
                    <p className="modal-section-title">상세설명</p>
                    <textarea
                        className="description-box"
                        placeholder="이곳의 특징을 설명해주세요. (예: 벤치 있음, 지붕 있음 등)"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                    />
                </div>
                <div className="modal-actions">
                    <button className="cancel-btn" onClick={onClose} disabled={isSubmitting}>취소</button>
                    <button className="register-btn" onClick={handleSubmit} disabled={isSubmitting}>
                        {isSubmitting ? '등록 중...' : '최종 등록'}
                    </button>
                </div>
            </div>
        </div>
    );
}