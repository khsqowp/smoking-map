'use client';

import { useEffect, useState, useCallback } from 'react';
import { apiClient } from '@/utils/apiClient';
import ImageManagementModal from '@/components/admin/ImageManagementModal';
import Link from 'next/link';

interface AdminImage {
    id: number;
    url: string;
    isRepresentative: boolean;
}

// --- ▼▼▼ [수정] 누락된 description, favoriteCount 필드 다시 추가 ▼▼▼ ---
interface AdminPlace {
    id: number;
    roadAddress: string;
    description: string;
    imageCount: number;
    creatorEmail: string;
    createdAt: string;
    favoriteCount: number;
    editRequestCount: number;
}

export default function AdminPlacesPage() {
    const [places, setPlaces] = useState<AdminPlace[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [searchTerm, setSearchTerm] = useState('');

    const [isImageModalOpen, setIsImageModalOpen] = useState(false);
    const [selectedPlaceId, setSelectedPlaceId] = useState<number | null>(null);
    const [selectedImages, setSelectedImages] = useState<AdminImage[]>([]);

    const fetchPlaces = useCallback(async (searchQuery: string) => {
        setIsLoading(true);
        setError('');
        try {
            const data = await apiClient(`/api/v1/admin/places?search=${encodeURIComponent(searchQuery)}`);
            setPlaces(data);
        } catch (err) {
            if (err instanceof Error) {
                setError(err.message);
            } else {
                setError('알 수 없는 오류가 발생했습니다.');
            }
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchPlaces('');
    }, [fetchPlaces]);

    const handleSearch = () => {
        fetchPlaces(searchTerm);
    };

    const handleImageCountClick = async (placeId: number) => {
        try {
            const imagesFromApi = await apiClient(`/api/v1/admin/places/${placeId}/images`);
            setSelectedPlaceId(placeId);
            setSelectedImages(imagesFromApi);
            setIsImageModalOpen(true);
        } catch (err) {
            console.error("이미지 목록을 불러오는 데 실패했습니다.", err);
            alert("이미지 목록을 불러오는 데 실패했습니다.");
        }
    };

    const handleCloseImageModal = () => {
        setIsImageModalOpen(false);
        fetchPlaces(searchTerm);
    }

    if (isLoading) return <div>장소 목록을 불러오는 중...</div>;
    if (error) return <div style={{ color: 'red' }}>오류: {error}</div>;

    return (
        <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <h1 style={{ margin: 0 }}>장소 관리</h1>
                <div style={{ display: 'flex', gap: '8px' }}>
                    <input
                        type="text"
                        placeholder="도로명 주소로 검색..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        onKeyUp={(e) => e.key === 'Enter' && handleSearch()}
                        style={{ padding: '8px', backgroundColor: '#2D3748', color: '#E2E8F0', border: '1px solid #4A5568', borderRadius: '6px' }}
                    />
                    <button onClick={handleSearch} className="btn btn-primary">검색</button>
                </div>
            </div>

            <div className="table-container" style={{ overflowX: 'auto' }}>
                <table className="admin-table">
                    {/* --- ▼▼▼ [수정] 테이블 헤더 복원 ▼▼▼ --- */}
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>도로명 주소</th>
                        <th>설명</th>
                        <th>이미지 수</th>
                        <th>생성자</th>
                        <th>생성일</th>
                        <th>즐겨찾기 수</th>
                        <th>수정 요청</th>
                        <th>작업</th>
                    </tr>
                    </thead>
                    {/* --- ▼▼▼ [수정] 테이블 본문 전체 복원 ▼▼▼ --- */}
                    <tbody>
                    {places.map(place => (
                        <tr key={place.id}>
                            <td>{place.id}</td>
                            <td style={{ minWidth: '250px' }}>{place.roadAddress}</td>
                            <td style={{ minWidth: '300px', whiteSpace: 'pre-wrap', maxHeight: '60px', overflowY: 'auto', display: 'block' }}>{place.description}</td>
                            <td>
                                <a
                                    href="#"
                                    onClick={(e) => {
                                        e.preventDefault();
                                        if (place.imageCount > 0) {
                                            handleImageCountClick(place.id);
                                        }
                                    }}
                                    style={{
                                        textDecoration: 'underline',
                                        cursor: place.imageCount > 0 ? 'pointer' : 'default',
                                        color: place.imageCount > 0 ? '#48BB78' : 'inherit'
                                    }}
                                >
                                    {place.imageCount}
                                </a>
                            </td>
                            <td>{place.creatorEmail}</td>
                            <td style={{ minWidth: '120px' }}>{place.createdAt}</td>
                            <td>{place.favoriteCount}</td>
                            <td>{place.editRequestCount}</td>
                            <td style={{minWidth: '120px'}}>
                                <Link href={`/admin/places/${place.id}`} className="btn btn-secondary">상세</Link>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {isImageModalOpen && selectedPlaceId && (
                <ImageManagementModal
                    placeId={selectedPlaceId}
                    initialImages={selectedImages}
                    onClose={handleCloseImageModal}
                />
            )}
        </div>
    );
}