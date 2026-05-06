const OwnerAuth = (() => {
    const TOKEN_KEY = 'owner_access_token';
    const REFRESH_KEY = 'owner_refresh_token';
    const USER_KEY = 'owner_user';
    const LOGIN_URL = '/login'; // Assuming standard login or /owner/login based on routing

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
        if (data.accessToken) localStorage.setItem(TOKEN_KEY, data.accessToken);
        if (data.refreshToken) localStorage.setItem(REFRESH_KEY, data.refreshToken);
        if (data.user) localStorage.setItem(USER_KEY, JSON.stringify(data.user));
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

    // Decode JWT logic to extract ownerId
    function parseJwt(token) {
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));

            return JSON.parse(jsonPayload);
        } catch (e) {
            return null;
        }
    }

    function getOwnerId() {
        const user = getUser();
        if (user && user.id) {
            return user.id;
        }

        const token = getToken();
        if (!token) return null;
        const decoded = parseJwt(token);
        // Fallback
        return decoded ? (decoded.ownerId || decoded.id || decoded.sub) : null;
    }

    async function authFetch(url, options = {}) {
        const token = getToken();
        if (!token) {
            logout();
            return Promise.reject(new Error("No token available"));
        }

        const headers = new Headers(options.headers || {});
        if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
            headers.set('Content-Type', 'application/json');
        }
        headers.set('Authorization', `Bearer ${token}`);

        const fetchOptions = { ...options, headers };
        let response = await fetch(url, fetchOptions);

        if (response.status === 401 || response.status === 403) {
            const refreshed = await tryRefreshToken();
            if (refreshed) {
                headers.set('Authorization', `Bearer ${getToken()}`);
                fetchOptions.headers = headers;
                response = await fetch(url, fetchOptions);
            } else {
                logout();
                return Promise.reject(new Error("Token expired and refresh failed"));
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

    return {
        getToken,
        getUser,
        saveAuth,
        clearAuth,
        logout,
        authFetch,
        getOwnerId,
        parseJwt
    };
})();
