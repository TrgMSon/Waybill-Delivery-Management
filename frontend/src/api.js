const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

let accessToken = null;
let refreshInProgress = null;

async function readJson(response) {
  if (!response.headers.get('content-type')?.includes('application/json')) {
    throw new Error(`Máy chủ API không trả về dữ liệu JSON (HTTP ${response.status}).`);
  }
  const result = await response.json();
  if (!response.ok) {
    throw new Error(result.message || 'Đã xảy ra lỗi. Vui lòng thử lại.');
  }
  return result;
}

export async function refreshSession() {
  if (refreshInProgress) return refreshInProgress;

  refreshInProgress = (async () => {
    const response = await fetch(`${API_BASE_URL}/api/auth/refresh-token`, {
      method: 'POST',
      credentials: 'include',
    });

    if (response.status === 401) {
      accessToken = null;
      return null;
    }

    const result = await readJson(response);
    accessToken = result.data.accessToken;
    return result.data;
  })();

  try {
    return await refreshInProgress;
  } finally {
    refreshInProgress = null;
  }
}

async function request(path, options = {}, retry = true) {
  const headers = new Headers(options.headers);
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`);

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
    credentials: 'include',
  });

  if (response.status === 401 && retry && accessToken && !path.startsWith('/api/auth/')) {
    const refreshed = await refreshSession();
    if (refreshed) return request(path, options, false);
  }

  return readJson(response);
}

export async function login(credentials) {
  const result = await readJson(await fetch(`${API_BASE_URL}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(credentials),
  }));
  accessToken = result.data.accessToken;
  return result.data;
}

export async function logout() {
  try {
    await request('/api/auth/logout', { method: 'POST' });
  } finally {
    accessToken = null;
  }
}

export async function getShipments({ page = 0, status = '' } = {}) {
  const params = new URLSearchParams({ page, size: 10, sort: 'createdAt,desc' });
  if (status) params.set('status', status);
  return request(`/api/shipments?${params}`);
}

export async function createShipment(shipment) {
  return request('/api/shipments', {
    method: 'POST',
    body: JSON.stringify(shipment),
  });
}

export async function transitionShipment(id, nextStatus, note) {
  return request(`/api/shipments/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ nextStatus, note }),
  });
}
