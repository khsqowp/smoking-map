'use client';

import { useState, useEffect, useCallback } from 'react';
import { apiClient } from '@/utils/apiClient';
import AnnouncementFormModal from '@/components/admin/AnnouncementFormModal';

interface Announcement {
    id: number;
    title: string;
    content: string;
    active: boolean;
    startDate: string;
    endDate: string;
    createdAt: string;
}

const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('ko-KR', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit',
    });
};

export default function AdminAnnouncementsPage() {
    const [announcements, setAnnouncements] = useState<Announcement[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingAnnouncement, setEditingAnnouncement] = useState<Announcement | undefined>(undefined); // --- ▼▼▼ [추가] 수정할 공지 데이터 상태 ▼▼▼ ---

    const fetchAnnouncements = useCallback(async () => {
        setIsLoading(true);
        try {
            const data = await apiClient('/api/v1/admin/announcements');
            const sortedData = data.sort((a: Announcement, b: Announcement) => b.id - a.id);
            setAnnouncements(sortedData);
        } catch (err) {
            console.error(err);
            alert('공지 목록을 불러오는 데 실패했습니다.');
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchAnnouncements();
    }, [fetchAnnouncements]);

    // --- ▼▼▼ [수정] 모달 핸들러 함수들 수정 ▼▼▼ ---
    const handleCreate = () => {
        setEditingAnnouncement(undefined); // 생성 모드에서는 수정 데이터 없음
        setIsModalOpen(true);
    };
    const handleEdit = (announcement: Announcement) => {
        setEditingAnnouncement(announcement); // 수정 모드에서는 데이터 전달
        setIsModalOpen(true);
    };
    const handleModalClose = () => setIsModalOpen(false);
    const handleSuccess = () => {
        setIsModalOpen(false);
        fetchAnnouncements();
    };

    const handleDelete = async (id: number) => {
        if (confirm(`정말로 ID ${id} 공지를 삭제하시겠습니까?`)) {
            try {
                await apiClient(`/api/v1/admin/announcements/${id}`, { method: 'DELETE' });
                alert('삭제되었습니다.');
                fetchAnnouncements();
            } catch (err) {
                alert('삭제에 실패했습니다.');
            }
        }
    };

    // --- ▼▼▼ [추가] 활성 상태 토글 핸들러 함수 ▼▼▼ ---
    const handleToggleActive = async (id: number) => {
        try {
            await apiClient(`/api/v1/admin/announcements/${id}/toggle-active`, { method: 'PATCH' });
            // API 호출 성공 시 목록을 다시 불러와 화면을 갱신
            fetchAnnouncements();
        } catch(err) {
            alert('상태 변경에 실패했습니다.');
        }
    };

    if (isLoading) return <div>로딩 중...</div>;

    return (
        <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <h1 style={{ margin: 0 }}>공지 관리</h1>
                <button onClick={handleCreate} className="btn btn-primary">새 공지 등록</button>
            </div>

            <div className="table-container" style={{ overflowX: 'auto' }}>
                <table className="admin-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>제목</th>
                        <th>상태</th>
                        <th>시작일</th>
                        <th>종료일</th>
                        <th>생성일</th>
                        <th style={{minWidth: '220px'}}>작업</th>
                    </tr>
                    </thead>
                    <tbody>
                    {announcements.map(item => (
                        <tr key={item.id}>
                            <td>{item.id}</td>
                            <td style={{ minWidth: '250px' }}>{item.title}</td>
                            <td>{item.active ? '🟢 활성' : '🔴 비활성'}</td>
                            <td style={{ minWidth: '150px' }}>{formatDate(item.startDate)}</td>
                            <td style={{ minWidth: '150px' }}>{formatDate(item.endDate)}</td>
                            <td style={{ minWidth: '150px' }}>{formatDate(item.createdAt)}</td>
                            <td>
                                {/* --- ▼▼▼ [수정] 버튼 그룹 수정 ▼▼▼ --- */}
                                <div style={{display: 'flex', gap: '8px'}}>
                                    <button onClick={() => handleToggleActive(item.id)} className="btn btn-secondary">
                                        {item.active ? '비활성화' : '활성화'}
                                    </button>
                                    <button onClick={() => handleEdit(item)} className="btn btn-secondary">수정</button>
                                    <button onClick={() => handleDelete(item.id)} className="btn btn-danger">삭제</button>
                                </div>
                                {/* --- ▲▲▲ [수정] 버튼 그룹 수정 ▲▲▲ --- */}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {isModalOpen && (
                <AnnouncementFormModal
                    onClose={handleModalClose}
                    onSuccess={handleSuccess}
                    initialData={editingAnnouncement} // --- ▼▼▼ [수정] 수정 데이터를 모달에 전달 ▼▼▼ ---
                />
            )}
        </div>
    );
}