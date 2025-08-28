'use client';

import { useState, useEffect } from 'react';
import { apiClient } from '@/utils/apiClient';
import AnnouncementModal from './AnnouncementModal';

interface Announcement {
    id: number;
    title: string;
    content: string;
}

const getCookie = (name: string): string | null => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop()?.split(';').shift() || null;
    return null;
};

export default function AppInitializer() {
    const [announcements, setAnnouncements] = useState<Announcement[]>([]);

    useEffect(() => {
        // --- ▼▼▼ [추가] 쿠키를 확인하는 로직 ▼▼▼ ---
        if (getCookie('hide_announcements_today')) {
            return;
        }

        const fetchAnnouncements = async () => {
            try {
                const data = await apiClient('/api/v1/announcements/active');
                if (data && data.length > 0) {
                    setAnnouncements(data);
                }
            } catch (error) {
                console.error("Failed to fetch announcements:", error);
            }
        };
        fetchAnnouncements();
    }, []);

    const handleCloseModal = (hideForToday: boolean) => {
        // --- ▼▼▼ [추가] '오늘 하루 보지 않기' 클릭 시 쿠키 설정 로직 ▼▼▼ ---
        if (hideForToday) {
            const expiryDate = new Date();
            expiryDate.setHours(23, 59, 59, 999); // 오늘 자정으로 만료 시간 설정
            document.cookie = `hide_announcements_today=true; expires=${expiryDate.toUTCString()}; path=/`;
        }
        setAnnouncements([]);
    };

    return (
        <>
            {announcements.length > 0 && (
                <AnnouncementModal
                    announcements={announcements}
                    onClose={handleCloseModal}
                />
            )}
        </>
    );
}