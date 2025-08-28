'use client';

interface Props {
    rating: number;
    size?: number;
}

const StarIcon = ({ filled, size }: { filled: boolean, size: number }) => (
    <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24"
        fill={filled ? "#FFD700" : "none"} stroke="#FFD700"
        strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
    </svg>
);

export default function StarRating({ rating, size = 16 }: Props) {
    const fullStars = Math.floor(rating);
    const halfStar = rating % 1 >= 0.5;
    const emptyStars = 5 - fullStars - (halfStar ? 1 : 0);

    return (
        <div style={{ display: 'flex', alignItems: 'center' }}>
            {[...Array(fullStars)].map((_, i) => <StarIcon key={`full-${i}`} filled={true} size={size} />)}
            {/* 현재 구현에서는 반쪽 별은 미사용, 추후 확장 가능 */}
            {[...Array(5 - fullStars)].map((_, i) => <StarIcon key={`empty-${i}`} filled={false} size={size} />)}
        </div>
    );
}