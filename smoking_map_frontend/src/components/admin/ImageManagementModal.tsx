'use client';

import { useState } from 'react';
import { apiClient } from '@/utils/apiClient';

const TrashIcon = () => ( <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg> );
const CheckIcon = () => ( <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg> );

interface AdminImage {
    id: number;
    url: string;
    isRepresentative: boolean;
}

interface Props {
    placeId: number;
    initialImages: AdminImage[];
    onClose: () => void;
}

export default function ImageManagementModal({ placeId, initialImages = [], onClose }: Props) {
    const [images, setImages] = useState(initialImages);
    const [isDeleting, setIsDeleting] = useState<number | null>(null);
    const [isSettingRep, setIsSettingRep] = useState<number | null>(null);

    const handleDelete = async (imageToDelete: AdminImage) => {
        if (!confirm('정말로 이 이미지를 삭제하시겠습니까?')) return;
        setIsDeleting(imageToDelete.id);
        try {
            await apiClient(`/api/v1/admin/places/${placeId}/images`, {
                method: 'DELETE',
                body: { imageUrl: imageToDelete.url },
            });
            setImages(prev => prev.filter(img => img.id !== imageToDelete.id));
        } catch (err) {
            if (err instanceof Error) {
                alert(`이미지 삭제 중 오류: ${err.message}`);
            } else {
                alert('알 수 없는 오류가 발생했습니다.');
            }
        } finally {
            setIsDeleting(null);
        }
    };

    const handleSetRepresentative = async (imageToSet: AdminImage) => {
        if (imageToSet.isRepresentative) return;
        setIsSettingRep(imageToSet.id);
        try {
            await apiClient(`/api/v1/admin/places/${placeId}/images/${imageToSet.id}/set-representative`, {
                method: 'POST',
            });
            setImages(prev => {
                const newImages = prev.map(img => ({ ...img, isRepresentative: false }));
                const targetImage = newImages.find(img => img.id === imageToSet.id);
                if (targetImage) targetImage.isRepresentative = true;
                return newImages.sort((a, b) => {
                    if (a.isRepresentative) return -1;
                    if (b.isRepresentative) return 1;
                    return 0;
                });
            });
        } catch (err) {
            if (err instanceof Error) {
                alert(`대표 이미지 설정 중 오류: ${err.message}`);
            } else {
                alert('알 수 없는 오류가 발생했습니다.');
            }
        } finally {
            setIsSettingRep(null);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="image-viewer-modal-content">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <h2 style={{ margin: 0 }}>이미지 관리 (총 {images.length}장)</h2>
                    <button onClick={onClose} className="btn btn-secondary">닫기</button>
                </div>

                {images.length > 0 ? (
                    <div className="image-grid">
                        {images.map((image) => (
                            <div key={image.id} className="image-grid-item" style={{ position: 'relative' }}>
                                <img src={image.url} alt={`장소 이미지 ${image.id}`} />

                                <button
                                    onClick={() => handleSetRepresentative(image)}
                                    disabled={isSettingRep === image.id || image.isRepresentative}
                                    style={{
                                        position: 'absolute',
                                        top: '8px',
                                        left: '8px',
                                        backgroundColor: image.isRepresentative ? 'rgba(72, 187, 120, 0.9)' : 'rgba(0, 0, 0, 0.5)',
                                        color: 'white',
                                        border: 'none',
                                        borderRadius: '50%',
                                        width: '32px',
                                        height: '32px',
                                        cursor: image.isRepresentative ? 'default' : 'pointer',
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        padding: 0
                                    }}
                                >
                                    {isSettingRep === image.id ? '...' : <CheckIcon />}
                                </button>

                                <button
                                    onClick={() => handleDelete(image)}
                                    disabled={isDeleting === image.id}
                                    style={{
                                        position: 'absolute',
                                        top: '8px',
                                        right: '8px',
                                        backgroundColor: 'rgba(229, 62, 62, 0.8)',
                                        color: 'white',
                                        border: 'none',
                                        borderRadius: '50%',
                                        width: '32px',
                                        height: '32px',
                                        cursor: 'pointer',
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        padding: 0
                                    }}
                                >
                                    {isDeleting === image.id ? '...' : <TrashIcon />}
                                </button>
                            </div>
                        ))}
                    </div>
                ) : (
                    <p style={{ textAlign: 'center', marginTop: '30px' }}>표시할 이미지가 없습니다.</p>
                )}
            </div>
        </div>
    );
}