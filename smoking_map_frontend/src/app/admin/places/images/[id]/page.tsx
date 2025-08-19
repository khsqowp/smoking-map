'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation'; // useParams 훅 import

export default function PlaceImagesPage() {
    const params = useParams(); // params prop 대신 useParams 훅을 사용
    const id = params.id as string; // 훅을 통해 id 값을 가져옴

    const [imageUrls, setImageUrls] = useState<string[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        if (!id) return;

        const fetchImageUrls = async () => {
            setIsLoading(true);
            try {
                const res = await fetch(`/api/v1/admin/places/${id}/images`, { credentials: 'include' });
                if (!res.ok) throw new Error('이미지 URL을 불러오는데 실패했습니다.');
                const data = await res.json();
                setImageUrls(data);
            } catch (err: any) {
                setError(err.message);
            } finally {
                setIsLoading(false);
            }
        };

        fetchImageUrls();
    }, [id]);

    if (isLoading) return <div>이미지 목록을 불러오는 중...</div>;
    if (error) return <div style={{ color: 'red' }}>{error}</div>;

    return (
        <div style={{ padding: '20px' }}>
            <h1 style={{ marginBottom: '20px' }}>장소 ID: {id}의 이미지 목록</h1>
            {imageUrls.length > 0 ? (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px' }}>
                    {imageUrls.map((url, index) => (
                        <div key={index} style={{
                            display: 'flex',
                            alignItems: 'center',
                            border: '1px solid #ccc',
                            padding: '10px',
                            borderRadius: '8px',
                            justifyContent: 'space-between'
                        }}>
                            <img src={url} alt={`Image ${index + 1}`} style={{
                                width: '150px',
                                height: '150px',
                                objectFit: 'cover',
                                borderRadius: '4px'
                            }} />

                            <a
                                href={url}
                                target="_blank"
                                rel="noopener noreferrer"
                                style={{
                                    padding: '10px 15px',
                                    backgroundColor: '#007bff',
                                    color: 'white',
                                    textDecoration: 'none',
                                    borderRadius: '5px',
                                    fontWeight: 'bold',
                                    textAlign: 'center'
                                }}
                            >
                                이미지 {index + 1}
                            </a>
                        </div>
                    ))}
                </div>
            ) : (
                <p>등록된 이미지가 없습니다.</p>
            )}
        </div>
    );
}