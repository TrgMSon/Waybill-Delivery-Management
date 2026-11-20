# Waybill Delivery Management

Ứng dụng quản lý vận đơn và điều phối giao hàng gồm REST API Spring Boot, PostgreSQL và giao diện React.

## Chạy local

1. Khởi động PostgreSQL:

   ```powershell
   docker compose up -d postgres
   ```

2. Chạy backend từ thư mục `backend`:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

3. Chạy frontend từ thư mục `frontend`:

   ```powershell
   npm install
   npm run dev
   ```

Mở `http://localhost:5173`. Vite chuyển tiếp `/api` tới backend tại `http://localhost:8080`.

Tài khoản demo (được tạo bởi seed database):

| Vai trò | Email | Mật khẩu |
| --- | --- | --- |
| Quản trị viên | `admin@demo.tms` | `Demo@123` |
| Tài xế | `driver1@demo.tms` | `Demo@123` |

Tài khoản tài xế chỉ xem các vận đơn được phân công; tài khoản quản trị có thể tạo vận đơn và chuyển trạng thái.

## Xác thực và cấu hình triển khai

- Access token chỉ được giữ trong bộ nhớ của frontend; tải lại trang sẽ gọi API refresh để lấy access token mới.
- Backend lưu refresh token đã băm trong cơ sở dữ liệu và gửi token thô trong cookie `HttpOnly`, `SameSite=Lax`, giới hạn đường dẫn `/api/auth`. Frontend không thể đọc cookie này bằng JavaScript.
- Mặc định local, `AUTH_COOKIE_SECURE=false`; bật HTTPS khi triển khai và đặt `AUTH_COOKIE_SECURE=true` (profile `prod` đã bật mặc định).
- Khi frontend không cùng origin với API, đặt `APP_CORS_ALLOWED_ORIGIN_PATTERNS` thành origin frontend được phép. Cấu hình credential đã được bật ở backend.
- Cookie `SameSite=Lax` phù hợp khi frontend và API cùng site. Nếu triển khai trên hai site khác nhau, cần thiết kế lại cookie/CSRF phù hợp thay vì chỉ đổi `SameSite`.
- Không dùng JWT secret mặc định local trong môi trường triển khai; đặt `JWT_SECRET_KEY` bằng secret mạnh.

Frontend có thể trỏ thẳng tới API bằng biến `VITE_API_BASE_URL`; nếu bỏ trống, các request dùng đường dẫn tương đối và proxy của Vite ở chế độ phát triển.
