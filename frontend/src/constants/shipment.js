export const STATUS_LABELS = {
  CREATED: 'Chờ xử lý',
  ASSIGNED: 'Đã phân công',
  PICKED_UP: 'Đã lấy hàng',
  IN_TRANSIT: 'Đang vận chuyển',
  DELIVERED: 'Đã giao',
  DELIVERY_FAILED: 'Giao thất bại',
  RETURNING: 'Đang hoàn hàng',
  RETURNED: 'Đã hoàn hàng',
  CANCELLED: 'Đã hủy',
};

export const STATUS_OPTIONS = [
  ['', 'Tất cả trạng thái'],
  ['CREATED', 'Chờ xử lý'],
  ['ASSIGNED', 'Đã phân công'],
  ['PICKED_UP', 'Đã lấy hàng'],
  ['IN_TRANSIT', 'Đang vận chuyển'],
  ['DELIVERED', 'Đã giao'],
  ['DELIVERY_FAILED', 'Giao thất bại'],
  ['RETURNING', 'Đang hoàn hàng'],
  ['RETURNED', 'Đã hoàn hàng'],
  ['CANCELLED', 'Đã hủy'],
];

export const STATUS_TRANSITIONS = {
  CREATED: ['ASSIGNED', 'CANCELLED'],
  ASSIGNED: ['PICKED_UP', 'CREATED', 'CANCELLED'],
  PICKED_UP: ['IN_TRANSIT', 'DELIVERED', 'DELIVERY_FAILED'],
  IN_TRANSIT: ['DELIVERED', 'DELIVERY_FAILED'],
  DELIVERY_FAILED: ['IN_TRANSIT', 'CANCELLED'],
};
