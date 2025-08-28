'use client';

import { useState } from 'react';
import { apiClient } from '@/utils/apiClient';

interface Props {
    placeId: number;
    onClose: () => void;
    onSuccess: () => void; // 리뷰 작성 성공 시 호출될 콜백
}

export default function ReviewFormModal({ placeId, onClose, onSuccess }: Props) {
    const [rating, setRating] = useState(0);
    const [hoverRating, setHoverRating] = useState(0);
    const [comment, setComment] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async () => {
        if (rating === 0) {
            alert('별점을 선택해주세요.');
            return;
        }
        setIsSubmitting(true);
        try {
            await apiClient(`/api/v1/places/${placeId}/reviews`, {
                method: 'POST',
                body: { rating, comment },
            });
            alert('리뷰가 등록되었습니다.');
            onSuccess();
        } catch (err) {
            alert(`오류: ${err instanceof Error ? err.message : '알 수 없는 오류'}`);
        } finally {
            setIsSubmitting(false);
        }
    };

    // 별점 UI를 위한 스타일
    const starStyle = { cursor: 'pointer', color: '#FFD700', fontSize: '32px' };

    return (
        <div className="modal-overlay">
            <div className="modal-content" style={{ backgroundColor: '#2a2a2a', color: '#E0E0E0', border: '1px solid #444' }}>
                <h2 style={{ textAlign: 'center', marginTop: '0' }}>리뷰 작성</h2>

                <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '15px' }}>
                    {[1, 2, 3, 4, 5].map(star => (
                        <span
                            key={star}
                            style={starStyle}
                            onClick={() => setRating(star)}
                            onMouseOver={() => setHoverRating(star)}
                            onMouseLeave={() => setHoverRating(0)}
                        >
                            {(hoverRating || rating) >= star ? '★' : '☆'}
                        </span>
                    ))}
                </div>

                <textarea
                    className="modal-textarea"
                    placeholder="한줄평을 남겨주세요 (선택)"
                    value={comment}
                    onChange={(e) => setComment(e.target.value)}
                    maxLength={500}
                />

                <div className="modal-actions">
                    <button onClick={onClose} className="btn btn-secondary" disabled={isSubmitting}>취소</button>
                    <button onClick={handleSubmit} className="btn btn-primary" disabled={isSubmitting}>
                        {isSubmitting ? '등록 중...' : '등록'}
                    </button>
                </div>
            </div>
        </div>
    );
}