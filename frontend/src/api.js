const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080';

export async function request(path, options = {}) {
  const token = localStorage.getItem('health_token');
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      ...(options.body && !(options.body instanceof FormData) ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { token } : {}),
      ...options.headers,
    },
  });
  const text = await response.text();
  let data = text;
  try { data = JSON.parse(text); } catch { /* string API response */ }
  if (!response.ok) throw new Error(typeof data === 'string' ? data : data.message || '请求失败');
  if (data?.code && data.code !== 200) throw new Error(data.message || '请求失败');
  return data?.data ?? data;
}

export const api = {
  login: (payload) => request('/user/login', { method: 'POST', body: JSON.stringify(payload) }),
  register: (payload) => request('/user/register', { method: 'POST', body: JSON.stringify(payload) }),
  foodList: () => request('/food/list'),
  addFood: (payload) => request('/food/add', { method: 'POST', body: JSON.stringify(payload) }),
  updateFood: (id, payload) => request('/food/' + id, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteFood: (id) => request('/food/' + id, { method: 'DELETE' }),
  moodList: (userId) => request(`/mood/list/${userId}`),
  addMood: (payload) => request('/mood', { method: 'POST', body: JSON.stringify(payload) }),
  sportList: (userId) => request(`/sport/list/${userId}`),
  addSport: (payload) => request('/sport', { method: 'POST', body: JSON.stringify(payload) }),
  aiChat: (message) => request('/ai/chat', { method: 'POST', body: JSON.stringify({ message }) }),
  dailyReport: () => request('/ai/daily-report', { method: 'POST' }),
  identifyFood: (file) => { const body = new FormData(); body.append('image', file); return request('/ai/food-image', { method: 'POST', body }); },
  identifyWorkout: (file) => { const body = new FormData(); body.append('image', file); return request('/ai/workout-image', { method: 'POST', body }); },
};
