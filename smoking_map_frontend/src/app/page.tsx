export const dynamic = 'force-dynamic'; // 이 라인을 추가
import Header from "@/components/Header";
import MapContainer, { Place } from "@/components/MapContainer"; // Place 타입을 MapContainer에서 import

// 백엔드 API에서 장소 목록을 가져오는 함수
async function getPlaces(): Promise<Place[]> {
  try {
    // 서버 사이드 렌더링 시에는 Docker 내부 네트워크 주소인 서비스 이름을 사용합니다.
    const apiUrl = 'http://backend:8080'; //마지막 베포시 변경  'http://backend:8080'     | 내부 개발시 'http://localhost:8080'
    const res = await fetch(`${apiUrl}/api/v1/places`, { cache: 'no-store' });


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
    <div style={{ display: 'flex', flexDirection: 'column', height: '100vh' }}>
      <Header />
      <main style={{ flexGrow: 1, position: 'relative' }}>
        <MapContainer places={places} />
      </main>
    </div>
  );
}
