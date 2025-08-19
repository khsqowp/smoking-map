import Header from "@/components/Header";
import MapContainer from "@/components/MapContainer";

// 장소 데이터 타입을 정의합니다.
type Place = {
  id: number;
  latitude: number;
  longitude: number;
  address: string;
  description: string;
  imageUrl?: string; // imageUrl 필드 추가 (선택적)
};

// 백엔드 API에서 장소 목록을 가져오는 함수
async function getPlaces(): Promise<Place[]> {
  try {
    // 서버 컴포넌트에서는 백엔드 API의 실제 주소를 직접 호출합니다.
    const res = await fetch('http://localhost:8080/api/v1/places', { cache: 'no-store' });

    if (!res.ok) {
      console.error('Failed to fetch places');
      return [];
    }
    return res.json();
  } catch (error) {
    console.error('Error fetching places:', error);
    return [];
  }
}


export default async function Home() {
  // 페이지가 렌더링되기 전에 서버에서 장소 데이터를 가져옵니다.
  const places = await getPlaces();

  return (
      <main>
        <Header />
        <MapContainer places={places} />
      </main>
  );
}