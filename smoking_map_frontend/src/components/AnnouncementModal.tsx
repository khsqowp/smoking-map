'use client';

import { useState } from 'react';

// --- ▼▼▼ [추가] 아코디언 아이템의 아이콘 ▼▼▼ ---
const ChevronDown = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="6 9 12 15 18 9"></polyline></svg>
);

interface Announcement {
    id: number;
    title: string;
    content: string;
}

interface Props {
    announcements: Announcement[];
    onClose: (hideForToday: boolean) => void; // --- ▼▼▼ [수정] onClose prop 타입 변경 ▼▼▼ ---
}

export default function AnnouncementModal({ announcements, onClose }: Props) {
    const [openIndex, setOpenIndex] = useState<number | null>(0);

    const handleToggle = (index: number) => {
        setOpenIndex(openIndex === index ? null : index); // 같은 항목을 누르면 닫고, 다른 항목을 누르면 염
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content" style={{
                backgroundColor: '#2a2a2a',
                color: '#E0E0E0',
                border: '1px solid #444',
                display: 'flex',
                flexDirection: 'column',
                gap: '10px'
            }}>
                <h2 className="modal-title" style={{color: '#E0E0E0', textAlign: 'center'}}>공지사항</h2>

                {/* --- ▼▼▼ [수정] 공지 리스트를 아코디언 형태로 렌더링 ▼▼▼ --- */}
                <div style={{
                    maxHeight: '60vh',
                    overflowY: 'auto',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '10px',
                    paddingRight: '10px'
                }}>
                    {announcements.map((item, index) => (
                        <div key={item.id} style={{border: '1px solid #444', borderRadius: '8px'}}>
                            <div
                                onClick={() => handleToggle(index)}
                                style={{
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center',
                                    padding: '12px 15px',
                                    cursor: 'pointer',
                                    backgroundColor: openIndex === index ? '#3a3a3a' : 'transparent'
                                }}
                            >
                                <span style={{fontWeight: '600'}}>{item.title}</span>
                                <div style={{
                                    transform: openIndex === index ? 'rotate(180deg)' : 'rotate(0deg)',
                                    transition: 'transform 0.2s'
                                }}>
                                    <ChevronDown/>
                                </div>
                            </div>
                            {openIndex === index && (
                                <div
                                    className="description-box-readonly"
                                    style={{padding: '15px', borderTop: '1px solid #444', whiteSpace: 'pre-wrap'}}
                                >
                                    {item.content}
                                </div>
                            )}
                        </div>
                    ))}
                </div>
                {/* --- ▲▲▲ [수정] 공지 리스트를 아코디언 형태로 렌더링 ▲▲▲ --- */}

                <div className="modal-actions" style={{marginTop: '10px', display: 'flex', gap: '10px'}}>
                    {/* --- ▼▼▼ [수정] 버튼 그룹 수정 ▼▼▼ --- */}
                    <button onClick={() => onClose(true)} className="btn btn-secondary" style={{flex: 1}}>오늘 하루 보지 않기
                    </button>
                    <button onClick={() => onClose(false)} className="btn btn-primary" style={{flex: 1}}>확인</button>
                    {/* --- ▲▲▲ [수정] 버튼 그룹 수정 ▲▲▲ --- */}
                </div>
            </div>
        </div>
    );
}