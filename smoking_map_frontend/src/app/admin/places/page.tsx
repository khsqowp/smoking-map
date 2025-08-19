'use client';

import Link from 'next/link';
import { useEffect, useState, useCallback } from 'react';

interface AdminPlace {
    id: number;
    roadAddress: string;
    description: string;
    imageCount: number;
    creatorEmail: string;
    createdAt: string;
}

interface EditingPlace {
    id: number;
    description: string;
}

export default function AdminPlacesPage() {
    const [places, setPlaces] = useState<AdminPlace[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [editingPlace, setEditingPlace] = useState<EditingPlace | null>(null);
    const [searchTerm, setSearchTerm] = useState('');

    const fetchPlaces = useCallback(async (currentSearchTerm: string) => {
        setIsLoading(true);
        try {
            const url = `/api/v1/admin/places${currentSearchTerm ? `?search=${encodeURIComponent(currentSearchTerm)}` : ''}`;
            const res = await fetch(url, { credentials: 'include' });
            if (!res.ok) throw new Error('장소 목록을 불러오는데 실패했습니다.');
            const data = await res.json();
            setPlaces(data);
        } catch (err: any) {
            setError(err.message);
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchPlaces(searchTerm);
    }, [fetchPlaces, searchTerm]);

    const handleDelete = async (id: number) => {
        // ... (기존과 동일)
    };

    const handleUpdate = async () => {
        // ... (기존과 동일)
    };

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        fetchPlaces(searchTerm);
    };

    if (isLoading) return <div>목록을 불러오는 중...</div>;
    if (error) return <div style={{ color: 'red' }}>{error}</div>;

    return (
        <div>
            <div className="page-header">
                <h1>장소 관리</h1>
                <form onSubmit={handleSearch} className="search-form">
                    <input
                        type="text"
                        placeholder="도로명 주소로 검색..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                    <button type="submit">검색</button>
                </form>
            </div>

            <div className="table-container">
                <table className="admin-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>도로명 주소</th>
                        <th>설명</th>
                        <th>이미지 수</th>
                        <th>등록자</th>
                        <th>등록일</th>
                        <th>관리</th>
                    </tr>
                    </thead>
                    <tbody>
                    {places.map(place => (
                        <tr key={place.id}>
                            <td>{place.id}</td>
                            <td>{place.roadAddress}</td>
                            <td>{place.description}</td>
                            <td>
                                <Link href={`/admin/places/images/${place.id}`} target="_blank" rel="noopener noreferrer" style={{color: '#007bff', textDecoration: 'underline'}}>
                                    {place.imageCount}
                                </Link>
                            </td>
                            <td>{place.creatorEmail}</td>
                            <td>{place.createdAt}</td>
                            <td>
                                <button onClick={() => setEditingPlace({ id: place.id, description: place.description })} style={{marginRight: '5px'}}>수정</button>
                                <button onClick={() => handleDelete(place.id)}>삭제</button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {editingPlace && (
                <div className="modal-overlay">
                    {/* ... (수정 모달은 기존과 동일) ... */}
                </div>
            )}
        </div>
    );
}