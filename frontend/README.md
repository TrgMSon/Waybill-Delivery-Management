# Waybill frontend

Ứng dụng React/Vite cho đăng nhập và quản lý vận đơn.

## Chạy

Từ thư mục `frontend`:

```powershell
npm install
npm run dev
```

Mặc định Vite proxy các request `/api` tới `http://localhost:8080`. Để gọi API ở origin khác, thiết lập `VITE_API_BASE_URL`.

```powershell
npm run lint
npm run build
```

Access token chỉ tồn tại trong bộ nhớ runtime. Refresh token được backend quản lý trong cookie `HttpOnly`; không lưu token vào `localStorage` hoặc `sessionStorage`.
