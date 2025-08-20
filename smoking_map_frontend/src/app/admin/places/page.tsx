// src/app/admin/places/page.tsx
'use client';

import { useState, useEffect, useMemo, useCallback } from 'react';
import { apiClient } from '@/utils/apiClient';

interface Place {
    id: number;
    roadAddress: string;
    description: string;
    imageCount: number;
    creatorEmail: string;
    createdAt: string;
    editRequestCount: number;
}

interface EditRequest {
    id: number;
    content: string;
    requesterEmail: string;
    createdAt: string;
}

// --- ▼▼▼ [추가] 이미지 뷰어 모달 컴포넌트 ▼▼▼ ---
const ImageViewerModal = ({ placeId, onClose }: { placeId: number; onClose: () => void; }) => {
    const [imageUrls, setImageUrls] = useState<string[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const fetchImages = async () => {
            try {
                const data = await apiClient(`/api/v1/admin/places/${placeId}/images`);
                setImageUrls(data || []);
            } catch (error) {
                console.error("Failed to fetch images:", error);
            } finally {
                setIsLoading(false);
            }
        };
        fetchImages();
    }, [placeId]);

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="image-viewer-modal-content" onClick={(e) => e.stopPropagation()}>
                <h2>장소 이미지 (ID: {placeId})</h2>
                {isLoading ? (
                    <div>이미지 로딩 중...</div>
                ) : (
                    <div className="image-grid">
                        {imageUrls.length > 0 ? (
                            imageUrls.map((url, index) => (
                                <div key={index} className="image-grid-item">
                                    <a href={url} target="_blank" rel="noopener noreferrer">
                                        <img src={url} alt={`장소 이미지 ${index + 1}`} />
                                    </a>
                                </div>
                            ))
                        ) : (
                            <div>표시할 이미지가 없습니다.</div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

const EditModal = ({ place, onClose, onConfirm }: { place: Place; onClose: () => void; onConfirm: (id: number, description: string) => void; }) => {
    const [description, setDescription] = useState(place.description);
    const [requests, setRequests] = useState<EditRequest[]>([]);

    useEffect(() => {
        const fetchRequests = async () => {
            try {
                const data = await apiClient(`/api/v1/admin/places/${place.id}/edit-requests`);
                if (data) setRequests(data);
            } catch (error) {
                console.error("Failed to fetch edit requests:", error);
            }
        };
        fetchRequests();
    }, [place.id]);

    return (
        <div className="modal-overlay">
            <div className="modal-content" style={{maxWidth: '600px', color: '#E0E0E0', backgroundColor: '#1E1E1E'}}>
                <h2>장소 설명 수정 (ID: {place.id})</h2>
                <p style={{textAlign: 'center', marginTop: '-10px', color: '#aaa'}}><strong>주소:</strong> {place.roadAddress}</p>

                <div>
                    <p className="modal-section-title" style={{color: '#E0E0E0'}}>원본 설명</p>
                    <textarea
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        className="description-box"
                        style={{ minHeight: '120px', backgroundColor: '#2D3748', color: '#E0E0E0', border: '1px solid #4A5568' }}
                    />
                </div>

                {requests.length > 0 && (
                    <div>
                        <p className="modal-section-title" style={{color: '#E0E0E0'}}>사용자 수정 제안 ({requests.length}개)</p>
                        <div style={{maxHeight: '200px', overflowY: 'auto', border: '1px solid #444', borderRadius: '8px', padding: '10px', backgroundColor: '#2D3748'}}>
                            {requests.map((req, index) => (
                                <div key={req.id} style={{borderBottom: index === requests.length - 1 ? 'none' : '1px solid #444', paddingBottom: '10px', marginBottom: '10px'}}>
                                    <p style={{margin: '0 0 5px 0', whiteSpace: 'pre-wrap'}}>{req.content}</p>
                                    <small style={{color: '#aaa'}}>{req.requesterEmail} - {req.createdAt}</small>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                <div className="modal-actions">
                    <button onClick={onClose} className="btn btn-secondary">취소</button>
                    <button onClick={() => onConfirm(place.id, description)} className="btn btn-primary">수정 완료</button>
                </div>
            </div>
        </div>
    );
};

export default function AdminPlacesPage() {
    const [places, setPlaces] = useState<Place[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [editingPlace, setEditingPlace] = useState<Place | null>(null);
    const [sortOrder, setSortOrder] = useState<'default' | 'desc'>('default');
    const [viewingImagesOfPlaceId, setViewingImagesOfPlaceId] = useState<number | null>(null); // --- ▼▼▼ [추가] 이미지 뷰어 상태 ▼▼▼ ---

    const fetchPlaces = useCallback(async () => {
        setIsLoading(true);
        try {
            const data = await apiClient('/api/v1/admin/places');
            setPlaces(data);
        } catch (err: any) {
            setError(err.message);
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchPlaces();
    }, [fetchPlaces]);

    const sortedPlaces = useMemo(() => {
        const sortablePlaces = [...places];
        if (sortOrder === 'desc') {
            sortablePlaces.sort((a, b) => b.editRequestCount - a.editRequestCount);
        }
        return sortablePlaces;
    }, [places, sortOrder]);

    const toggleSortOrder = () => {
        setSortOrder(prev => (prev === 'default' ? 'desc' : 'default'));
    };

    const handleUpdateConfirm = async (id: number, description: string) => {
        try {
            await apiClient(`/api/v1/admin/places/${id}`, {
                method: 'PUT',
                body: { description },
            });
            alert('수정이 완료되었습니다.');
            setEditingPlace(null);
            fetchPlaces();
        } catch (err: any) {
            alert(err.message);
        }
    };

    const handleDelete = async (id: number) => {
        if (!confirm(`정말로 ID ${id} 장소를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`)) {
            return;
        }
        try {
            await apiClient(`/api/v1/admin/places/${id}`, {
                method: 'DELETE',
            });
            alert('삭제가 완료되었습니다.');
            fetchPlaces();
        } catch (err: any) {
            alert(err.message);
        }
    }

    if (isLoading) return <div>로딩 중...</div>;
    if (error) return <div style={{ color: 'red' }}>{error}</div>;

    return (
        <div>
            <h1 style={{ marginBottom: '20px' }}>장소 관리</h1>
            <div className="table-container">
                <table className="admin-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>주소</th>
                        <th>설명</th>
                        <th>이미지 수</th>
                        <th>등록자</th>
                        <th>등록일</th>
                        <th onClick={toggleSortOrder} style={{cursor: 'pointer'}}>
                            작업 {sortOrder === 'desc' ? '▼' : '△'}
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    {sortedPlaces.map(place => (
                        <tr key={place.id}>
                            <td>{place.id}</td>
                            <td>{place.roadAddress}</td>
                            <td style={{ maxWidth: '300px', whiteSpace: 'pre-wrap', wordBreak: 'break-all' }}>{place.description}</td>
                            {/* --- ▼▼▼ [수정] 이미지 수 부분을 버튼으로 변경 ▼▼▼ --- */}
                            <td>
                                {place.imageCount > 0 ? (
                                    <button onClick={() => setViewingImagesOfPlaceId(place.id)} className="link-style-button">
                                        {place.imageCount}
                                    </button>
                                ) : (
                                    0
                                )}
                            </td>
                            <td>{place.creatorEmail}</td>
                            <td>{place.createdAt}</td>
                            <td>
                                <button onClick={() => setEditingPlace(place)} className="btn btn-secondary" style={{marginRight: '5px'}}>
                                    수정
                                    {place.editRequestCount > 0 &&
                                        <span style={{color: '#48BB78', marginLeft: '5px', fontWeight: 'bold'}}>({place.editRequestCount})</span>
                                    }
                                </button>
                                <button onClick={() => handleDelete(place.id)} className="btn btn-danger">삭제</button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {editingPlace && (
                <EditModal
                    place={editingPlace}
                    onClose={() => setEditingPlace(null)}
                    onConfirm={handleUpdateConfirm}
                />
            )}

            {/* --- ▼▼▼ [추가] 이미지 뷰어 모달 렌더링 ▼▼▼ --- */}
            {viewingImagesOfPlaceId && (
                <ImageViewerModal
                    placeId={viewingImagesOfPlaceId}
                    onClose={() => setViewingImagesOfPlaceId(null)}
                />
            )}
        </div>
    );
}