import type { EventItem } from "@/types/event";

// Mock data — chưa có backend, port trực tiếp từ prototype thiết kế gốc.
export const EVENTS: EventItem[] = [
  {
    id: "e1",
    title: "Đêm Nhạc Hoàng Hôn — Live Concert",
    category: "concert",
    featured: true,
    date: "2026-08-14",
    dateLabel: "14 Th08, 2026",
    venue: "SVĐ Quân Khu 7",
    city: "TP.HCM",
    organizer: "Sao Việt Entertainment",
    description:
      "Một đêm nhạc hoành tráng quy tụ dàn nghệ sĩ hàng đầu, hòa âm sống động cùng dàn nhạc giao hưởng trên sân khấu ánh sáng hiện đại.",
    tickets: [
      { name: "Vé Thường", price: 450000 },
      { name: "Vé VIP", price: 850000 },
      { name: "Vé Premium", price: 1500000 },
    ],
  },
  {
    id: "e2",
    title: 'Vở Kịch "Giấc Mơ Phố Cổ"',
    category: "sankhau",
    featured: true,
    date: "2026-08-02",
    dateLabel: "02 Th08, 2026",
    venue: "Nhà Hát Thành Phố",
    city: "TP.HCM",
    organizer: "Sân Khấu Kịch Sài Gòn",
    description:
      "Câu chuyện cảm động về ký ức đô thị được kể qua ngôn ngữ sân khấu đương đại, dàn diễn viên gạo cội.",
    tickets: [
      { name: "Vé Thường", price: 250000 },
      { name: "Vé VIP", price: 450000 },
    ],
  },
  {
    id: "e3",
    title: "Giải Chạy Đêm Thành Phố 2026",
    category: "thethao",
    featured: false,
    date: "2026-09-06",
    dateLabel: "06 Th09, 2026",
    venue: "Công viên Bờ Sông",
    city: "Hà Nội",
    organizer: "Run Club VN",
    description:
      "Đường chạy 5km/10km/21km xuyên qua khu vực trung tâm, ánh sáng LED và nhạc sống dọc đường chạy.",
    tickets: [
      { name: "Cự ly 5km", price: 200000 },
      { name: "Cự ly 10km", price: 300000 },
      { name: "Cự ly 21km", price: 450000 },
    ],
  },
  {
    id: "e4",
    title: "Workshop: Kể Chuyện Bằng Thiết Kế",
    category: "hoithao",
    featured: true,
    date: "2026-08-20",
    dateLabel: "20 Th08, 2026",
    venue: "The Hive Coworking",
    city: "TP.HCM",
    organizer: "Design Circle",
    description:
      "Buổi workshop chuyên sâu về tư duy kể chuyện trong thiết kế sản phẩm, cùng các chuyên gia trong ngành.",
    tickets: [
      { name: "Vé Cá Nhân", price: 350000 },
      { name: "Vé Nhóm (3 người)", price: 900000 },
    ],
  },
  {
    id: "e5",
    title: "Concert Acoustic — Mùa Thu Về",
    category: "concert",
    featured: false,
    date: "2026-09-12",
    dateLabel: "12 Th09, 2026",
    venue: "Nhà Văn Hóa Thanh Niên",
    city: "TP.HCM",
    organizer: "Indie Sound",
    description:
      "Không gian ấm cúng cùng những giai điệu acoustic mộc mạc từ các nghệ sĩ indie được yêu thích.",
    tickets: [
      { name: "Vé Thường", price: 300000 },
      { name: "Vé VIP", price: 550000 },
    ],
  },
  {
    id: "e6",
    title: "Chung Kết Giải Bóng Rổ Vô Địch Quốc Gia",
    category: "thethao",
    featured: true,
    date: "2026-08-29",
    dateLabel: "29 Th08, 2026",
    venue: "Nhà Thi Đấu Phú Thọ",
    city: "TP.HCM",
    organizer: "VBA",
    description:
      "Trận chung kết đỉnh cao của mùa giải, hai đội mạnh nhất tranh ngôi vô địch trước hàng chục nghìn khán giả.",
    tickets: [
      { name: "Khán đài thường", price: 150000 },
      { name: "Khán đài VIP", price: 400000 },
    ],
  },
  {
    id: "e7",
    title: "Triển Lãm & Talkshow Nghệ Thuật Đương Đại",
    category: "sankhau",
    featured: false,
    date: "2026-09-18",
    dateLabel: "18 Th09, 2026",
    venue: "Bảo Tàng Mỹ Thuật",
    city: "Hà Nội",
    organizer: "Hanoi Art Collective",
    description:
      "Không gian trưng bày kết hợp talkshow cùng các nghệ sĩ đương đại về hành trình sáng tạo.",
    tickets: [
      { name: "Vé Vào Cửa", price: 100000 },
      { name: "Vé Talkshow", price: 220000 },
    ],
  },
  {
    id: "e8",
    title: "Hội Thảo Khởi Nghiệp Công Nghệ 2026",
    category: "hoithao",
    featured: false,
    date: "2026-09-25",
    dateLabel: "25 Th09, 2026",
    venue: "GEM Center",
    city: "TP.HCM",
    organizer: "StartupVN",
    description:
      "Kết nối founder, nhà đầu tư và chuyên gia công nghệ hàng đầu trong một ngày hội thảo đầy cảm hứng.",
    tickets: [
      { name: "Vé Tiêu Chuẩn", price: 500000 },
      { name: "Vé VIP + Networking", price: 1200000 },
    ],
  },
  {
    id: "e9",
    title: "Lễ Hội Âm Nhạc Điện Tử — Neon Nights",
    category: "concert",
    featured: false,
    date: "2026-10-03",
    dateLabel: "03 Th10, 2026",
    venue: "Công viên 23/9",
    city: "TP.HCM",
    organizer: "Neon Productions",
    description:
      "Đại tiệc âm nhạc điện tử với dàn DJ quốc tế, sân khấu ánh sáng laser hoành tráng.",
    tickets: [
      { name: "Vé Thường", price: 400000 },
      { name: "Vé VIP", price: 900000 },
      { name: "Vé Premium", price: 1800000 },
    ],
  },
];

export function getEventById(id: string | undefined): EventItem | undefined {
  return EVENTS.find((e) => e.id === id);
}
