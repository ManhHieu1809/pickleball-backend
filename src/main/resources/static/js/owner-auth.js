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

    async function login(usernameOrEmail, password) {
        // Try Owner login first
        try {
            const ownerRes = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: usernameOrEmail, password: password })
            });
            const ownerJson = await ownerRes.json();
            
            if (ownerRes.ok && ownerJson.success && ownerJson.data) {
                saveAuth(ownerJson.data);
                return ownerJson.data;
            }
        } catch (e) {
            console.error(e);
        }

        // If Owner login fails (invalid credentials or not found), try Staff login
        try {
            const staffRes = await fetch('/api/staff/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: usernameOrEmail, password: password })
            });
            const staffJson = await staffRes.json();
            
            if (staffRes.ok && staffJson.success && staffJson.data) {
                // Ensure data structure max matches Owner
                const authData = {
                    accessToken: staffJson.data.accessToken,
                    user: staffJson.data.staff
                };
                // Make sure we denote this is a staff
                if (authData.user && !authData.user.roles) {
                    authData.user.roles = ['STAFF'];
                }
                saveAuth(authData);
                return authData;
            }
            
            throw new Error(staffJson.message || 'Tài khoản hoặc mật khẩu không đúng');
        } catch (e) {
            throw e;
        }
    }

    function hasPermission(permission) {
        const user = getUser();
        if (!user) return false;
        if (user.roles && user.roles.includes('OWNER')) return true;
        
        if (user.roles && user.roles.includes('STAFF')) {
            if (user.permissions && user.permissions.includes(permission)) {
                return true;
            }
            return false;
        }
        return false;
    }

    function checkAccessAndRenderMenu() {
        const user = getUser();
        if (!user) {
            window.location.href = LOGIN_URL;
            return;
        }

        const isStaff = user.roles && user.roles.includes('STAFF');
        
        // Allowed pages for Staff
        const staffAllowedPages = ['/owner/bookings', '/owner/bookingstaff', '/owner/dashboard', '/login'];
        const currentPath = window.location.pathname;

        // Redirect if Staff access restricted page
        if (isStaff && !staffAllowedPages.includes(currentPath) && currentPath.startsWith('/owner/')) {
            alert('Bạn không có quyền truy cập trang này. Đang chuyển hướng về danh sách lịch đặt...');
            window.location.href = '/owner/bookings';
            return;
        }
        
        // Hide sensitive nav items if Staff
        if (isStaff) {
            document.querySelectorAll('aside a').forEach(link => {
                const href = link.getAttribute('href');
                if (href && !staffAllowedPages.includes(href)) {
                    link.style.display = 'none';
                }
            });
            document.querySelectorAll('.owner-only').forEach(el => {
                el.style.display = 'none';
            });
        }

        // Populate user info in header
        populateUserInfo();
    }

    function populateUserInfo() {
        const user = getUser();
        if (!user) return;

        const displayName = user.fullName || user.email || 'Owner';
        const roleLabel = user.roles && user.roles.includes('STAFF') ? 'Staff' : 'System Owner';
        const avatarSrc = user.profilePictureUrl || 'https://i.pravatar.cc/150?u=owner';

        // Build dropdown HTML vào mỗi container [data-owner-profile]
        document.querySelectorAll('[data-owner-profile]').forEach(container => {
            container.innerHTML = `
                <img src="${avatarSrc}" alt="${displayName}"
                    class="h-8 w-8 rounded-full border border-slate-100 object-cover">
                <div class="hidden lg:block text-left leading-tight">
                    <p class="text-sm font-bold text-slate-900">${displayName}</p>
                    <p class="text-[10px] font-bold uppercase tracking-wider text-slate-400">${roleLabel}</p>
                </div>
                <i data-lucide="chevron-down" class="w-4 h-4 text-slate-300"></i>
                <!-- Dropdown -->
                <div class="absolute top-full right-0 mt-2 w-44 bg-white rounded-xl shadow-xl border border-slate-100 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all z-50">
                    <div class="px-4 py-3 border-b border-slate-100">
                        <p class="text-sm font-bold text-slate-900 truncate">${displayName}</p>
                        <p class="text-xs text-slate-400 truncate">${user.email || ''}</p>
                    </div>
                    <button id="owner-logout-btn"
                        class="w-full flex items-center gap-2 px-4 py-2.5 text-sm font-semibold text-red-500 hover:bg-red-50 rounded-b-xl transition-colors">
                        <i data-lucide="log-out" class="w-4 h-4"></i> Đăng xuất
                    </button>
                </div>
            `;
            // Re-init Lucide icons for newly injected elements
            if (window.lucide) lucide.createIcons();

            // Bind logout
            const logoutBtn = container.querySelector('#owner-logout-btn');
            if (logoutBtn) logoutBtn.addEventListener('click', () => logout());
        });
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
        login,
        hasPermission,
        checkAccessAndRenderMenu,
        populateUserInfo,
        authFetch,
        getOwnerId,
        parseJwt
    };
})();
