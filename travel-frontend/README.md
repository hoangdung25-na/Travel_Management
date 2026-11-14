# Travel Frontend — Lữ Trình

Frontend ReactJS (Next.js App Router) + TailwindCSS cho hệ thống Travel Management,
gọi trực tiếp API Gateway theo Swagger đã cung cấp.

## Cài đặt & chạy

```bash
npm install
cp .env.local.example .env.local   # rồi chỉnh lại nếu backend chạy port/domain khác
npm run dev
```

Mặc định gọi API Gateway tại `http://localhost:8080`. Sửa `NEXT_PUBLIC_API_BASE_URL`
trong `.env.local` nếu backend của bạn chạy ở địa chỉ khác.

## Cấu trúc chính

- `src/lib/types.ts` — TypeScript types khớp 100% với OpenAPI spec 5 service
- `src/lib/api-client.ts` — axios instance: tự đính JWT, tự refresh token khi hết hạn
- `src/lib/services/*` — hàm gọi API theo từng service (auth/tour/booking/payment/ai)
- `src/lib/auth-context.tsx` — quản lý đăng nhập/đăng xuất, lưu token vào cookie
- `src/middleware.ts` — bảo vệ route `/admin`, `/guide`, `/account` theo role
- `src/app/*` — các trang, tổ chức theo route (App Router)

## Đã hoàn thành

- Luồng Du khách đầy đủ: tìm tour → chi tiết → đặt tour → thanh toán (đếm ngược giữ chỗ
  15 phút) → quản lý đơn / E-ticket QR / hủy đơn → hồ sơ cá nhân
- Đăng nhập/đăng ký, đăng nhập Google qua Keycloak (Authorization Code flow)
- Trợ lý AI (RAG chatbot tư vấn lịch trình)
- Khung Admin: quản lý Tour (tạo/sửa trạng thái/thêm lịch khởi hành), Điểm đến,
  tra cứu Đơn hàng + hoàn tiền, cập nhật trạng thái người dùng
- Khung Guide: đã có route + bảo vệ role, chờ backend bổ sung API

## Còn thiếu API ở backend (đã ghi chú ngay trong UI tương ứng)

- `GET` danh sách toàn bộ booking cho Admin (hiện tra cứu theo Booking ID)
- `GET` danh sách toàn bộ user cho Admin (hiện cập nhật trạng thái theo User ID)
- API dashboard thống kê doanh thu/tỷ lệ lấp đầy
- API lịch phân công + danh sách hành khách cho Hướng dẫn viên

## Đã build thử thành công

`npm run build` chạy sạch, không lỗi TypeScript/ESLint, cả 18 route compile được
(xác nhận trong môi trường có thể tải Google Fonts).
