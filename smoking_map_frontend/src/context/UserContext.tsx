'use client';

import React, { createContext, useState, useEffect, useContext, ReactNode } from 'react';
import { apiClient } from '@/utils/apiClient';

// [수정] User 타입에 role 필드 추가
type User = {
    name: string;
    email: string;
    picture: string;
    role: string; // 역할(role) 필드 추가
};

interface UserContextType {
    user: User | null;
    isLoading: boolean;
}

const UserContext = createContext<UserContextType>({ user: null, isLoading: true });

export const useUser = () => useContext(UserContext);

export const UserProvider = ({ children }: { children: ReactNode }) => {
    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const fetchUser = async () => {
            try {
                const data = await apiClient('/api/v1/user');
                setUser(data);
            } catch (error) {
                console.error("Failed to fetch user:", error);
                setUser(null);
            } finally {
                setIsLoading(false);
            }
        };
        fetchUser();
    }, []);

    return (
        <UserContext.Provider value={{ user, isLoading }}>
            {children}
        </UserContext.Provider>
    );
};