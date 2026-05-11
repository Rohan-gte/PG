(function (PG) {
    const routes = [];
    let currentTeardown = null;

    function add(pattern, roles, render) {
        const keys = [];
        const rx = new RegExp('^' + pattern.replace(/:([a-zA-Z0-9_]+)/g, (_, k) => { keys.push(k); return '([^/]+)'; }) + '$');
        routes.push({ rx, keys, roles, render });
    }

    function match(path) {
        for (const r of routes) {
            const m = r.rx.exec(path);
            if (m) {
                const params = {};
                r.keys.forEach((k, i) => params[k] = decodeURIComponent(m[i + 1]));
                return { route: r, params };
            }
        }
        return null;
    }

    async function dispatch() {
        if (currentTeardown) { try { currentTeardown(); } catch (_) {} currentTeardown = null; }
        const hash = location.hash || '#/login';
        const path = hash.replace(/^#/, '');

        if (path === '/logout') { PG.Auth.logout(); return; }

        if ((path === '/' || path === '') && PG.Auth.isLoggedIn()) {
            const u = PG.Auth.currentUser();
            if (u && u.role) { location.hash = PG.Auth.roleHome(u.role); return; }
        }

        const mr = match(path);
        if (!mr) {
            if (!PG.Auth.isLoggedIn()) { location.hash = '#/login'; return; }
            const u = PG.Auth.currentUser();
            location.hash = PG.Auth.roleHome(u ? u.role : null);
            return;
        }

        const needsAuth = mr.route.roles && mr.route.roles.length > 0;
        if (needsAuth) {
            if (!PG.Auth.isLoggedIn()) { location.hash = '#/login'; return; }
            const u = PG.Auth.currentUser();
            if (!u || mr.route.roles.indexOf(u.role) < 0) {
                location.hash = PG.Auth.roleHome(u ? u.role : null);
                return;
            }
        }

        try {
            const teardown = await mr.route.render(mr.params);
            if (typeof teardown === 'function') currentTeardown = teardown;
        } catch (ex) {
            console.error(ex);
            PG.UI.toast(ex.message || 'Failed to render page', 'error');
        }
    }

    PG.Router = {
        add,
        start() {
            window.addEventListener('hashchange', dispatch);
            dispatch();
        },
        navigate(hash) {
            if (location.hash === hash) dispatch();
            else location.hash = hash;
        }
    };
})(window.PG);
