'use client';

import { useState, useEffect, useCallback } from 'react';
import { apiClient } from '@/utils/apiClient';
import StarRating from './StarRating';
import { useUser } from '@/context/UserContext';

interface Review {
    id: number;
    userName: string;
    userPicture: string;
    rating: number;
    comment: string;
    createdAt: string;
    writtenByCurrentUser: boolean;
}

interface Props {
    placeId: number;
    refreshKey: number; // 부모로부터 이 값이 변경되면 데이터를 다시 불러옴
}

export default function ReviewList({ placeId, refreshKey }: Props) {
    const [reviews, setReviews] = useState<Review[]>([]);
    const { user } = useUser();

    const fetchReviews = useCallback(async () => {
        try {
            const data = await apiClient(`/api/v1/places/${placeId}/reviews`);
            setReviews(data);
        } catch (error) {
            console.error("Failed to fetch reviews", error);
        }
    }, [placeId]);

    useEffect(() => {
        fetchReviews();
    }, [fetchReviews, refreshKey]);

    const handleDelete = async (reviewId: number) => {
        if (!confirm("정말로 이 리뷰를 삭제하시겠습니까?")) return;
        try {
            await apiClient(`/api/v1/reviews/${reviewId}`, { method: 'DELETE' });
            alert("리뷰가 삭제되었습니다.");
            fetchReviews(); // 목록 새로고침
        } catch (error) {
            alert("리뷰 삭제에 실패했습니다.");
        }
    };

    return (
        <div style={{ marginTop: '20px', borderTop: '1px solid #444', paddingTop: '20px' }}>
            <h4 style={{ margin: '0 0 15px 0' }}>리뷰 ({reviews.length}개)</h4>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '15px', maxHeight: '200px', overflowY: 'auto' }}>
                {reviews.length > 0 ? reviews.map(review => (
                    <div key={review.id}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '5px' }}>
                            {/* --- ▼▼▼ [수정] 익명일 경우 빈 공간을 차지하는 플레이스홀더 추가 ▼▼▼ --- */}
                            {review.writtenByCurrentUser ? (
                                <img src={review.userPicture} alt={review.userName} style={{ width: '30px', height: '30px', borderRadius: '50%' }} />
                            ) : (
                                <div style={{ width: '30px', height: '30px', flexShrink: 0 }} />
                            )}
                            {/* --- ▲▲▲ [수정] 익명일 경우 빈 공간을 차지하는 플레이스홀더 추가 ▲▲▲ --- */}
                            <span>{review.userName}</span>
                            <StarRating rating={review.rating} size={14} />
                        </div>
                        <p style={{ margin: '0 0 5px 40px', whiteSpace: 'pre-wrap' }}>{review.comment}</p>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <small style={{ marginLeft: '40px', color: '#A0AEC0' }}>{review.createdAt}</small>
                            {review.writtenByCurrentUser && (
                                <button onClick={() => handleDelete(review.id)} className='btn btn-danger' style={{padding: '2px 6px', fontSize: '12px'}}>삭제</button>
                            )}
                        </div>
                    </div>
                )) : <p>아직 작성된 리뷰가 없습니다.</p>}
            </div>
        </div>
    );
}