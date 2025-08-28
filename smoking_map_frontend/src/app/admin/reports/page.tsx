// src/app/admin/reports/page.tsx

'use client';

import { useState, useEffect } from 'react';

// 데이터 타입을 정의합니다.
interface GroupedReport {
    placeId: number;
    roadAddress: string;
    reportTypeCounts: Record<string, number>;
    otherContents: string[];
}

// '기타' 내용 표시를 위한 모달
const OtherContentsModal = ({ contents, onClose }: { contents: string[]; onClose: () => void; }) => (
    <div className="modal-overlay" onClick={onClose}>
        <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>기타 신고 내용</h2>
            <ul style={{ maxHeight: '400px', overflowY: 'auto', paddingLeft: '20px' }}>
                {contents.map((content, index) => (
                    <li key={index} style={{ marginBottom: '10px', whiteSpace: 'pre-wrap' }}>{content}</li>
                ))}
            </ul>
            <div className="modal-actions">
                <button onClick={onClose} className="close-btn-bottom">닫기</button>
            </div>
        </div>
    </div>
);

export default function AdminReportsPage() {
    const [reports, setReports] = useState<GroupedReport[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    // 모달 상태 관리
    const [modalContents, setModalContents] = useState<string[] | null>(null);

    useEffect(() => {
        const fetchReports = async () => {
            setIsLoading(true);
            setError('');
            try {
                // 새로 만든 그룹화 API를 호출합니다.
                const res = await fetch('/api/v1/admin/reports/grouped', { credentials: 'include' });
                if (!res.ok) throw new Error('신고 목록을 불러오는데 실패했습니다.');
                const data = await res.json();
                setReports(data);
            } catch (err) {
                if (err instanceof Error) {
                    setError(err.message);
                } else {
                    setError('알 수 없는 오류가 발생했습니다.');
                }
            } finally {
                setIsLoading(false);
            }
        };
        fetchReports();
    }, []);

    if (isLoading) return <div>로딩 중...</div>;
    if (error) return <div style={{ color: 'red' }}>{error}</div>;

    return (
        <div>
            <h1 style={{ marginBottom: '20px' }}>신고 관리 (그룹화)</h1>
            <div className="table-container">
                <table className="admin-table">
                    <thead>
                    <tr>
                        <th>장소 ID</th>
                        <th>주소</th>
                        <th>신고 내역</th>
                    </tr>
                    </thead>
                    <tbody>
                    {reports.map(report => (
                        <tr key={report.placeId}>
                            <td>{report.placeId}</td>
                            <td>{report.roadAddress}</td>
                            <td>
                                {Object.entries(report.reportTypeCounts).map(([type, count]) => {
                                    if (type === '기타') {
                                        return (
                                            <div key={type}>
                                                {type} * {count}
                                                <button onClick={() => setModalContents(report.otherContents)}
                                                    className="btn btn-secondary" style={{marginLeft: '10px'}}>
                                                    내용 보기
                                                </button>
                                            </div>
                                        );
                                    }
                                    return <div key={type}>{type} * {count}</div>;
                                })}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {modalContents && (
                <OtherContentsModal contents={modalContents} onClose={() => setModalContents(null)} />
            )}
        </div>
    );
}