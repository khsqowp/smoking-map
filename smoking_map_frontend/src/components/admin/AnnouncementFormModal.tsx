'use client';

import { useState, useEffect } from 'react';
import { apiClient } from '@/utils/apiClient';

// --- ▼▼▼ [추가] 수정 시 필요한 기존 공지 데이터 타입 ▼▼▼ ---
interface AnnouncementData {
    id: number;
    title: string;
    content: string;
    active: boolean;
    startDate: string;
    endDate: string;
}

interface Props {
    onClose: () => void;
    onSuccess: () => void;
    initialData?: AnnouncementData; // --- ▼▼▼ [수정] 수정 모드일 때 초기 데이터를 받도록 추가 ▼▼▼ ---
}

const getLocalDateTimeString = (date: Date): string => {
    const pad = (num: number) => num.toString().padStart(2, '0');
    const year = date.getFullYear();
    const month = pad(date.getMonth() + 1);
    const day = pad(date.getDate());
    const hours = pad(date.getHours());
    const minutes = pad(date.getMinutes());
    return `${year}-${month}-${day}T${hours}:${minutes}`;
};

export default function AnnouncementFormModal({ onClose, onSuccess, initialData }: Props) {
    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [active, setActive] = useState(true);
    const [startDate, setStartDate] = useState(getLocalDateTimeString(new Date()));
    const [endDate, setEndDate] = useState(() => {
        const nextWeek = new Date();
        nextWeek.setDate(nextWeek.getDate() + 7);
        return getLocalDateTimeString(nextWeek);
    });
    const [isSubmitting, setIsSubmitting] = useState(false);

    // --- ▼▼▼ [추가] 수정 모드일 때 초기 데이터 설정 ▼▼▼ ---
    useEffect(() => {
        if (initialData) {
            setTitle(initialData.title);
            setContent(initialData.content);
            setActive(initialData.active);
            setStartDate(getLocalDateTimeString(new Date(initialData.startDate)));
            setEndDate(getLocalDateTimeString(new Date(initialData.endDate)));
        }
    }, [initialData]);

    const handleSubmit = async () => {
        if (!title.trim() || !content.trim() || !startDate || !endDate) {
            alert('모든 필드를 입력해주세요.');
            return;
        }
        setIsSubmitting(true);
        try {
            const body = {
                title,
                content,
                active,
                startDate: new Date(startDate).toISOString(),
                endDate: new Date(endDate).toISOString(),
            };

            // --- ▼▼▼ [수정] 수정 모드와 생성 모드를 구분하여 다른 API 호출 ▼▼▼ ---
            if (initialData) {
                // 수정
                await apiClient(`/api/v1/admin/announcements/${initialData.id}`, {
                    method: 'PUT',
                    body: body,
                });
                alert('공지가 성공적으로 수정되었습니다.');
            } else {
                // 생성
                await apiClient('/api/v1/admin/announcements', {
                    method: 'POST',
                    body: body,
                });
                alert('공지가 성공적으로 등록되었습니다.');
            }
            onSuccess();
        } catch (err) {
            alert(`오류: ${err instanceof Error ? err.message : '알 수 없는 오류'}`);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content" style={{gap: '15px'}}>
                {/* --- ▼▼▼ [수정] 제목을 동적으로 변경 ▼▼▼ --- */}
                <h2 className="modal-title">{initialData ? '공지 수정' : '새 공지 등록'}</h2>

                {/* 폼 UI는 동일 */}
                <div className="modal-section">
                    <label className="modal-label">제목</label>
                    <input
                        type="text"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        className="modal-input"
                    />
                </div>

                <div className="modal-section">
                    <label className="modal-label">내용</label>
                    <textarea
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        className="modal-textarea"
                        rows={5}
                    />
                </div>

                <div style={{display: 'flex', gap: '20px'}}>
                    <div className="modal-section" style={{flex: 1}}>
                        <label className="modal-label">시작일</label>
                        <input
                            type="datetime-local"
                            value={startDate}
                            onChange={(e) => setStartDate(e.target.value)}
                            className="modal-input"
                        />
                    </div>
                    <div className="modal-section" style={{flex: 1}}>
                        <label className="modal-label">종료일</label>
                        <input
                            type="datetime-local"
                            value={endDate}
                            onChange={(e) => setEndDate(e.target.value)}
                            className="modal-input"
                        />
                    </div>
                </div>

                <div className="modal-section" style={{ display: 'flex', alignItems: 'center' }}>
                    <input
                        type="checkbox"
                        id="active-checkbox"
                        checked={active}
                        onChange={(e) => setActive(e.target.checked)}
                        style={{ marginRight: '10px', width: '18px', height: '18px' }}
                    />
                    <label htmlFor="active-checkbox">활성 상태로 등록</label>
                </div>

                <div className="modal-actions">
                    <button onClick={onClose} className="btn btn-secondary" disabled={isSubmitting}>취소</button>
                    <button onClick={handleSubmit} className="btn btn-primary" disabled={isSubmitting}>
                        {isSubmitting ? '저장 중...' : '저장'}
                    </button>
                </div>
            </div>
        </div>
    );
}