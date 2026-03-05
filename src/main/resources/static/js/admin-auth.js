
const AdminAuth = (() => {
    const TOKEN_KEY = 'admin_access_token';
    const REFRESH_KEY = 'admin_refresh_token';
    const USER_KEY = 'admin_user';
    const LOGIN_URL = '/admin/login';

    function getToken() {
        return localStorage.getItem(TOKEN_KEY);
    }

    function getRefreshToken() {
        return localStorage.getItem(REFRESH_KEY);
    }

    function getUser() {
        const raw = localStorage.getItem(USER_KEY);
        return raw ? JSON.parse(raw) : null;
    }

    function saveAuth(data) {
        localStorage.setItem(TOKEN_KEY, data.accessToken);
        localStorage.setItem(REFRESH_KEY, data.refreshToken);
        localStorage.setItem(USER_KEY, JSON.stringify(data.user));
    }

    function clearAuth() {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(REFRESH_KEY);
        localStorage.removeItem(USER_KEY);
    }

    function logout() {
        clearAuth();
        window.location.href = LOGIN_URL;
    }

    async function authFetch(url, options = {}) {
        const token = getToken();
        if (!token) {
            logout();
            return;
        }

        const headers = {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
            ...(options.headers || {})
        };

        let response = await fetch(url, { ...options, headers });

        if (response.status === 401) {
            const refreshed = await tryRefreshToken();
            if (refreshed) {
                headers['Authorization'] = `Bearer ${getToken()}`;
                response = await fetch(url, { ...options, headers });
            } else {
                logout();
                return;
            }
        }

        return response;
    }

    async function tryRefreshToken() {
        const refreshToken = getRefreshToken();
        if (!refreshToken) return false;

        try {
            const res = await fetch('/api/auth/refresh-token', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ refreshToken })
            });

            if (!res.ok) return false;

            const json = await res.json();
            if (json.success && json.data) {
                saveAuth(json.data);
                return true;
            }
            return false;
        } catch {
            return false;
        }
    }

    async function checkAuth() {
        const token = getToken();
        if (!token) {
            logout();
            return false;
        }

        try {
            const res = await fetch('/api/auth/me', {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (!res.ok) {
                const refreshed = await tryRefreshToken();
                if (!refreshed) {
                    logout();
                    return false;
                }
            }

            const user = getUser();
            if (!user || !user.roles || !user.roles.includes('ADMIN')) {
                clearAuth();
                window.location.href = LOGIN_URL + '?error=not_admin';
                return false;
            }

            return true;
        } catch {
            logout();
            return false;
        }
    }

    async function login(email, password) {
        const res = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        const json = await res.json();

        if (!res.ok || !json.success) {
            throw new Error(json.message || 'Đăng nhập thất bại');
        }

        const data = json.data;

        if (!data.user.roles || !data.user.roles.includes('ADMIN')) {
            throw new Error('Tài khoản không có quyền Admin');
        }

        saveAuth(data);
        return data;
    }

    function populateUserInfo() {
        const user = getUser();
        if (!user) return;

        document.querySelectorAll('[data-admin-name]').forEach(el => {
            el.textContent = user.fullName || user.email;
        });
        document.querySelectorAll('[data-admin-email]').forEach(el => {
            el.textContent = user.email;
        });
        document.querySelectorAll('[data-admin-avatar]').forEach(el => {
            if (user.profilePictureUrl) {
                el.src = user.profilePictureUrl;
            }
        });
    }

    return {
        getToken,
        getUser,
        saveAuth,
        clearAuth,
        logout,
        authFetch,
        checkAuth,
        login,
        populateUserInfo
    };
})();
