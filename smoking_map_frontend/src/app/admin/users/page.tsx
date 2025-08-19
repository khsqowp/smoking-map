'use client';

import { useEffect, useState } from 'react';

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

    const fetchUsers = async () => {
        // ... (기존과 동일)
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    const handleRoleChangeClick = (user: AdminUser) => {
        // ... (기존과 동일)
    };

    const handleCancelEdit = () => {
        // ... (기존과 동일)
    };

    const handleUpdateRole = async () => {
        // ... (기존과 동일)
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
                                    <select value={selectedRole} onChange={(e) => setSelectedRole(e.target.value)}>
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
                                        <button onClick={handleUpdateRole} style={{marginRight: '5px'}}>저장</button>
                                        <button onClick={handleCancelEdit}>취소</button>
                                    </>
                                ) : (
                                    <button onClick={() => handleRoleChangeClick(user)}>역할 변경</button>
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