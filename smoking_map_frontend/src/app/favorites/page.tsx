'use client';

import { useState, useEffect } from 'react';
import { apiClient } from '@/utils/apiClient';
import Link from 'next/link';
import Header from '@/components/Header';
import { useUser } from '@/context/UserContext';
import { Place } from '@/components/MapContainer';

const FavoriteItem = ({ place }: { place: Place }) => (
    <div style={{
        backgroundColor: '#2a2a2a', border: '1px solid #444', borderRadius: '8px',
        padding: '15px', display: 'flex', gap: '15px', alignItems: 'center'
    }}>
        <img
            src={place.imageUrls?.[0] || '/default-image.png'} // 기본 이미지 경로
            alt={place.roadAddress || '장소 이미지'}
            style={{ width: '80px', height: '80px', borderRadius: '8px', objectFit: 'cover' }}
        />
        <div style={{ flex: 1 }}>
            <h3 style={{ margin: '0 0 5px 0', fontSize: '16px' }}>{place.roadAddress || place.originalAddress}</h3>
            <p style={{ margin: 0, fontSize: '14px', color: '#A0AEC0' }}>{place.description}</p>
        </div>
    </div>
);

export default function FavoritesPage() {
    const { user, isLoading: isUserLoading } = useUser();
    const [favorites, setFavorites] = useState<Place[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        if (!isUserLoading && user) {
            apiClient('/api/v1/favorites')
                .then(data => {
                    setFavorites(data);
                })
                .catch(err => {
                    console.error("Failed to fetch favorites:", err);
                    alert("즐겨찾기 목록을 불러오는 데 실패했습니다.");
                })
                .finally(() => setIsLoading(false));
        } else if (!isUserLoading && !user) {
            setIsLoading(false);
        }
    }, [user, isUserLoading]);

    if (isLoading || isUserLoading) {
        return <div style={{ textAlign: 'center', padding: '50px' }}>로딩 중...</div>;
    }

    if (!user) {
        return (
            <div style={{ textAlign: 'center', padding: '50px' }}>
                <h1>로그인이 필요합니다.</h1>
                <Link href="/" style={{ textDecoration: 'underline' }}>메인으로 돌아가기</Link>
            </div>
        );
    }

    return (
        <div style={{ minHeight: '100vh', backgroundColor: '#121212', color: '#E0E0E0' }}>
            <Header />
            <main style={{ maxWidth: '800px', margin: '0 auto', padding: '24px' }}>
                <h1 style={{ marginBottom: '24px' }}>즐겨찾기 목록</h1>
                {favorites.length > 0 ? (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                        {favorites.map(place => (
                            <FavoriteItem key={place.id} place={place} />
                        ))}
                    </div>
                ) : (
                    <p>아직 즐겨찾기한 장소가 없습니다.</p>
                )}
            </main>
        </div>
    );
}