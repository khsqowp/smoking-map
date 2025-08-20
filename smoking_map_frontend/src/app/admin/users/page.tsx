// src/app/admin/users/page.tsx

'use client';

import { useEffect, useState, useCallback } from 'react';

interface AdminUser {
    id: number;
    name: string;
    email: string;
    role: string;
    createdAt: string;
}

const ROLES = ['USER', 'MANAGER', 'ADMIN'];

export default function AdminUsersPage() {
    const [users, setUsers] = useState<AdminUser[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    const [editingUserId, setEditingUserId] = useState<number | null>(null);
    const [selectedRole, setSelectedRole] = useState('');

    const fetchUsers = useCallback(async () => {
        setIsLoading(true);
        try {
            const res = await fetch('/api/v1/admin/users', { credentials: 'include' });
            if (!res.ok) throw new Error('사용자 목록을 불러오는데 실패했습니다.');
            const data = await res.json();
            setUsers(data);
        } catch (err: any) {
            setError(err.message);
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchUsers();
    }, [fetchUsers]);

    const handleRoleChangeClick = (user: AdminUser) => {
        setEditingUserId(user.id);
        setSelectedRole(user.role);
    };

    const handleCancelEdit = () => {
        setEditingUserId(null);
        setSelectedRole('');
    };

    const handleUpdateRole = async () => {
        if (!editingUserId) return;
        try {
            const res = await fetch(`/api/v1/admin/users/${editingUserId}/role`, {
                method: 'PATCH',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ role: selectedRole })
            });

            if (!res.ok) throw new Error('역할 변경에 실패했습니다.');

            alert('성공적으로 역할을 변경했습니다.');
            handleCancelEdit();
            fetchUsers(); // 목록을 다시 불러와 변경사항을 반영합니다.
        } catch (err: any) {
            alert(`오류: ${err.message}`);
        }
    };

    if (isLoading) return <div>사용자 목록을 불러오는 중...</div>;
    if (error) return <div style={{ color: 'red' }}>{error}</div>;

    return (
        <div>
            <h1 style={{ marginBottom: '20px' }}>사용자 관리</h1>
            <div className="table-container">
                <table className="admin-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>이름</th>
                        <th>이메일</th>
                        <th>역할</th>
                        <th>가입일</th>
                        <th>관리</th>
                    </tr>
                    </thead>
                    <tbody>
                    {users.map(user => (
                        <tr key={user.id}>
                            <td>{user.id}</td>
                            <td>{user.name}</td>
                            <td>{user.email}</td>
                            <td>
                                {editingUserId === user.id ? (
                                    <select value={selectedRole} onChange={(e) => setSelectedRole(e.target.value)}
                                        // --- ▼▼▼ [추가] 셀렉트 박스 스타일 ▼▼▼ ---
                                        style={{
                                            backgroundColor: '#2D3748',
                                            color: '#E2E8F0',
                                            padding: '8px',
                                            borderRadius: '6px',
                                            border: '1px solid #4A5568'
                                        }}
                                    >
                                        {ROLES.map(role => <option key={role} value={role}>{role}</option>)}
                                    </select>
                                ) : (
                                    user.role
                                )}
                            </td>
                            <td>{user.createdAt}</td>
                            <td>
                                {editingUserId === user.id ? (
                                    <>
                                        {/* --- ▼▼▼ [수정] 버튼에 클래스 적용 ▼▼▼ --- */}
                                        <button onClick={handleUpdateRole} className="btn btn-primary" style={{marginRight: '5px'}}>저장</button>
                                        <button onClick={handleCancelEdit} className="btn btn-secondary">취소</button>
                                    </>
                                ) : (
                                    <button onClick={() => handleRoleChangeClick(user)} className="btn btn-secondary">역할 변경</button>
                                )}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}