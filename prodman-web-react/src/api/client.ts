import axios, { AxiosError } from 'axios';

const apiClient = axios.create({
    baseURL: '',
    headers: {
        'Content-Type': 'application/json',
    },
});

/**
 * Прокидываем accessToken в Authorization и X-Tenant-Id.
 *
 * ВАЖНО: по-хорошему X-Tenant-Id должен ставить Gateway из JWT.
 * Сейчас шлём его с фронта для удобства разработки (Gateway может быть не настроен).
 * Позже уберём — когда Gateway начнёт извлекать tenantId из токена.
 */
apiClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken');
        const tenantId = localStorage.getItem('tenantId');

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        if (tenantId) {
            config.headers['X-Tenant-Id'] = tenantId;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

/**
 * Глобальный перехватчик ошибок.
 * 401 → чистим токен и перекидываем на /login.
 */
apiClient.interceptors.response.use(
    (response) => response,
    (error: AxiosError) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            localStorage.removeItem('tenantId');
            if (window.location.pathname !== '/login') {
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

export default apiClient;