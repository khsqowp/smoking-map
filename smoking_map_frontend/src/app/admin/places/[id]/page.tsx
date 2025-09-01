'use client';

import { useParams, useRouter } from 'next/navigation';
import { useState, useEffect, useCallback } from 'react';
import Link from 'next/link';
import { apiClient } from '@/utils/apiClient';

// --- ▼▼▼ [추가] 관리자용 리뷰 타입 정의 ▼▼▼ ---
interface AdminReview {
    id: number;
    userEmail: string;
    rating: number;
    comment: string;
    createdAt: string;
}

interface EditRequest {
    id: number;
    content: string;
    requesterEmail: string;
    createdAt: string;
}

interface PlaceDetail {
    id: number;
    roadAddress: string;
    description: string;
    creatorEmail: string;
    editRequests: EditRequest[];
    reviews: AdminReview[]; // --- ▼▼▼ [추가] 리뷰 목록 타입 ▼▼▼ ---
}

export default function AdminPlaceDetailPage() {
    const params = useParams();
    const router = useRouter();
    const { id } = params;

    const [place, setPlace] = useState<PlaceDetail | null>(null);
    const [description, setDescription] = useState('');
    const [isLoading, setIsLoading] = useState(true);

    const fetchPlaceDetails = useCallback(async () => {
        setIsLoading(true);
        try {
            const data = await apiClient(`/api/v1/admin/places/${id}`);
            setPlace(data);
            setDescription(data.description || '');
        } catch (error) {
            console.error("Failed to fetch place details:", error);
            alert("장소 정보를 불러오는 데 실패했습니다.");
        } finally {
            setIsLoading(false);
        }
    }, [id]);

    useEffect(() => {
        if (id) {
            fetchPlaceDetails();
        }
    }, [id, fetchPlaceDetails]);

    const handleUpdateDescription = async () => {
        if (!confirm("설명을 수정하시겠습니까? 수정 시 모든 '대기중'인 수정 제안이 '검토 완료' 상태로 변경됩니다.")) {
            return;
        }
        try {
            await apiClient(`/api/v1/admin/places/${id}`, {
                method: 'PUT',
                body: { description },
            });
            alert('설명이 성공적으로 수정되었습니다.');
            fetchPlaceDetails(); // 데이터 새로고침
        } catch (error) {
            alert('설명 수정에 실패했습니다.');
        }
    };

    const handleDeletePlace = async () => {
        if (!confirm(`정말로 이 장소(ID: ${id})를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`)) {
            return;
        }
        try {
            await apiClient(`/api/v1/admin/places/${id}`, { method: 'DELETE' });
            alert('장소가 성공적으로 삭제되었습니다.');
            router.push('/admin/places'); // 삭제 후 목록 페이지로 이동
        } catch (error) {
            alert('장소 삭제에 실패했습니다.');
        }
    };

    const handleDeleteReview = async (reviewId: number) => {
        if (!confirm(`정말로 이 리뷰(ID: ${reviewId})를 삭제하시겠습니까?`)) {
            return;
        }
        try {
            await apiClient(`/api/v1/admin/reviews/${reviewId}`, { method: 'DELETE' });
            alert('리뷰가 성공적으로 삭제되었습니다.');
            fetchPlaceDetails(); // 데이터 새로고침
        } catch (error) {
            alert('리뷰 삭제에 실패했습니다.');
        }
    };

    if (isLoading) return <div>로딩 중...</div>;
    if (!place) return <div>장소 정보를 찾을 수 없습니다.</div>;

    return (
        <div>
            <div style={{ marginBottom: '30px' }}>
                <Link href="/admin/places" style={{ textDecoration: 'underline' }}>&larr; 장소 목록으로 돌아가기</Link>
                <h1 style={{ marginTop: '10px' }}>장소 상세 정보 (ID: {place.id})</h1>
            </div>

            {/* 정보 요약 섹션 */}
            <div className="summary-card" style={{ marginBottom: '30px' }}>
                <p><strong>도로명 주소:</strong> {place.roadAddress}</p>
                <p><strong>최초 등록자:</strong> {place.creatorEmail}</p>
            </div>

            {/* 설명 수정 섹션 */}
            <div className="summary-card" style={{ marginBottom: '30px' }}>
                <h2 style={{ marginTop: 0, fontSize: '20px' }}>설명 수정</h2>
                <textarea
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    className="modal-textarea"
                    rows={5}
                    placeholder="장소에 대한 설명을 입력하세요."
                />
                <button onClick={handleUpdateDescription} className="btn btn-primary" style={{ marginTop: '10px' }}>
                    설명 저장
                </button>
            </div>

            {/* 수정 제안 목록 섹션 */}
            <div className="summary-card" style={{marginBottom: '30px'}}>
                <h2 style={{
                    marginTop: 0,
                    fontSize: '20px'
                }}>정보 수정 제안 목록 ({place.editRequests.length}개)</h2>{place.editRequests.length > 0 ? (
                <table className="admin-table">
                    <thead>
                    <tr>
                        <th>제안자</th>
                        <th>내용</th>
                        <th>제안일</th>
                    </tr>
                    </thead>
                    <tbody>
                    {place.editRequests.map(req => (
                        <tr key={req.id}>
                            <td>{req.requesterEmail}</td>
                            <td>{req.content}</td>
                            <td>{req.createdAt}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            ) : (
                <p>접수된 수정 제안이 없습니다.</p>
            )}
            </div>

            {/* --- ▼▼▼ [추가] 리뷰 관리 섹션 ▼▼▼ --- */}
            <div className="summary-card" style={{ marginBottom: '30px' }}>
                <h2 style={{ marginTop: 0, fontSize: '20px' }}>리뷰 목록 ({place.reviews.length}개)</h2>
                {place.reviews.length > 0 ? (
                    <table className="admin-table">
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>작성자</th>
                            <th>평점</th>
                            <th>내용</th>
                            <th>작성일</th>
                            <th>작업</th>
                        </tr>
                        </thead>
                        <tbody>
                        {place.reviews.map(rev => (
                            <tr key={rev.id}>
                                <td>{rev.id}</td>
                                <td>{rev.userEmail}</td>
                                <td>{'★'.repeat(rev.rating)}</td>
                                <td style={{whiteSpace: 'pre-wrap'}}>{rev.comment}</td>
                                <td>{rev.createdAt}</td>
                                <td>
                                    <button onClick={() => handleDeleteReview(rev.id)} className="btn btn-danger">삭제</button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                ) : (
                    <p>작성된 리뷰가 없습니다.</p>
                )}
            </div>
            {/* --- ▲▲▲ [추가] 리뷰 관리 섹션 ▲▲▲ --- */}

            {/* 위험 구역 */}
            <div className="summary-card" style={{borderColor: '#E53E3E'}}>
                <h2 style={{marginTop: 0, fontSize: '20px', color: '#E53E3E' }}>위험 구역</h2>
                <p>이 장소를 영구적으로 삭제합니다. 삭제된 데이터는 복구할 수 없습니다.</p>
                <button onClick={handleDeletePlace} className="btn btn-danger">
                    장소 삭제
                </button>
            </div>
        </div>
    );
}
