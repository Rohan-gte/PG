(function (PG) {
    PG.Auth = {
        isLoggedIn() {
            return !!PG.token.get();
        },
        currentUser() {
            return PG.user.get();
        },
        async login(email, password) {
            const r = await PG.api.post('/api/auth/login', { email, password });
            PG.token.set(r.token);
            PG.user.set(r.user);
            return r;
        },
        async registerTenant(payload) {
            return await PG.api.post('/api/auth/register/tenant', payload);
        },
        async registerOwner(payload) {
            return await PG.api.post('/api/auth/register/owner', payload);
        },
        async refresh() {
            try {
                const u = await PG.api.get('/api/auth/me');
                PG.user.set(u);
                return u;
            } catch (_) {
                return null;
            }
        },
        logout() {
            PG.token.clear();
            PG.user.clear();
            location.hash = '#/login';
        },
        roleHome(role) {
            switch (role) {
                case 'ADMIN': return '#/admin/dashboard';
                case 'OWNER': return '#/owner/dashboard';
                case 'RECEPTIONIST': return '#/receptionist/dashboard';
                case 'TENANT': return '#/tenant/dashboard';
                default: return '#/login';
            }
        }
    };
})(window.PG);
