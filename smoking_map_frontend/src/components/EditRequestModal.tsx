// src/components/EditRequestModal.tsx
'use client';

import { useState } from 'react';
import { apiClient } from '@/utils/apiClient'; // apiClient import

interface Props {
    placeId: number;
    onClose: () => void;
}

export default function EditRequestModal({ placeId, onClose }: Props) {
    const [content, setContent] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async () => {
        if (!content.trim()) {
            alert('수정 제안 내용을 입력해주세요.');
            return;
        }
        setIsSubmitting(true);
        try {
            // --- ▼▼▼ [수정] fetch를 apiClient로 교체 ▼▼▼ ---
            await apiClient(`/api/v1/places/${placeId}/edit-requests`, {
                method: 'POST',
                body: { content },
            });
            alert('수정 요청이 성공적으로 제출되었습니다.');
            onClose();
        } catch (err: any) {
            alert(`오류: ${err.message}`);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <h2>장소 정보 수정 제안</h2>
                <textarea
                    placeholder="수정 제안 내용을 입력해주세요. (예: 주차장 입구 바로 옆입니다.)"
                    value={content}
                    onChange={(e) => setContent(e.target.value)}
                    className="description-box"
                    style={{ minHeight: '150px' }}
                />
                <div className="modal-actions">
                    <button onClick={onClose} className="btn btn-secondary" disabled={isSubmitting}>취소</button>
                    <button onClick={handleSubmit} className="btn btn-primary" disabled={isSubmitting}>
                        {isSubmitting ? '제출 중...' : '제출'}
                    </button>
                </div>
            </div>
        </div>
    );
}