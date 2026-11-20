# Waybill Delivery Management

Ứng dụng quản lý vận đơn và điều phối giao hàng gồm REST API Spring Boot, PostgreSQL và giao diện React.

## Mô tả nghiệp vụ

### Quản lý phiên làm việc và phân quyền

- Người dùng đăng nhập bằng email và mật khẩu. Hệ thống xác thực bằng JWT access token; refresh token được lưu băm ở cơ sở dữ liệu và gửi cho trình duyệt bằng cookie `HttpOnly`.
- Hệ thống có hai vai trò: `ADMIN` và `DRIVER`.
- Quản trị viên có quyền quản lý vận đơn; tài xế chỉ truy cập các vận đơn đã được phân công cho tài khoản của mình.
- Tài khoản đăng ký mới qua API được cấp vai trò `DRIVER`. Tài khoản bị khóa không thể đăng nhập hoặc làm mới phiên.
- Người dùng có thể làm mới phiên đăng nhập và đăng xuất; đăng xuất sẽ thu hồi refresh token.

### Quản lý vận đơn

- Quản trị viên tạo vận đơn với mã vận đơn duy nhất, thông tin người gửi, địa chỉ lấy hàng, thông tin người nhận, địa chỉ giao hàng, tiền thu hộ (COD) và phí vận chuyển.
- Mã vận đơn được sinh tự động theo mẫu có tiền tố `TMS`, ngày hiện tại và chuỗi ngẫu nhiên.
- Danh sách vận đơn hỗ trợ phân trang, sắp xếp theo thời gian tạo giảm dần và lọc theo trạng thái.
- Giao diện cho phép tìm kiếm trong trang hiện tại theo mã vận đơn, tên người nhận, số điện thoại người nhận hoặc địa chỉ giao hàng.
- Quản trị viên xem được toàn bộ vận đơn; tài xế chỉ xem chi tiết và danh sách vận đơn được phân công cho tài khoản của mình.
- REST API cho phép quản trị viên cập nhật thông tin vận đơn chỉ khi vận đơn đang ở trạng thái `CREATED`.
- Hệ thống ghi lại lịch sử mỗi lần thay đổi trạng thái cùng người thực hiện thay đổi.

### Luồng trạng thái vận đơn

Hệ thống kiểm tra chuyển trạng thái bằng state machine:

- `CREATED`: có thể chuyển sang `ASSIGNED` hoặc `CANCELLED`.
- `ASSIGNED`: có thể chuyển sang `PICKED_UP`, quay lại `CREATED` hoặc `CANCELLED`.
- `PICKED_UP`: có thể chuyển sang `IN_TRANSIT`, `DELIVERED` hoặc `DELIVERY_FAILED`.
- `IN_TRANSIT`: có thể chuyển sang `DELIVERED` hoặc `DELIVERY_FAILED`.
- `DELIVERY_FAILED`: có thể chuyển sang `IN_TRANSIT` hoặc `CANCELLED`.
- `DELIVERED` và `CANCELLED` là trạng thái kết thúc.

Trên giao diện, quản trị viên chỉ được chọn các trạng thái kế tiếp hợp lệ của vận đơn đang xem.

### Dữ liệu demo

- Môi trường demo cung cấp tài khoản quản trị và nhiều tài khoản tài xế.
- Dữ liệu seed minh họa các vận đơn ở nhiều giai đoạn: chờ xử lý, đã phân công, đã lấy hàng, đang vận chuyển, đã giao, giao thất bại, hoàn hàng và đã hủy.
- Dữ liệu seed cũng gồm thông tin tài xế, lượt gán vận đơn, xác nhận giao hàng và đối soát COD để hỗ trợ khởi tạo môi trường thử nghiệm.

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
