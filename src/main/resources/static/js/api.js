(function (PG) {
    const TOKEN_KEY = 'pg.token';
    const USER_KEY = 'pg.user';

    PG.token = {
        get() { try { return localStorage.getItem(TOKEN_KEY); } catch (_) { return null; } },
        set(v) { try { localStorage.setItem(TOKEN_KEY, v); } catch (_) {} },
        clear() { try { localStorage.removeItem(TOKEN_KEY); } catch (_) {} }
    };

    PG.user = {
        get() { try { const v = localStorage.getItem(USER_KEY); return v ? JSON.parse(v) : null; } catch (_) { return null; } },
        set(v) { try { localStorage.setItem(USER_KEY, JSON.stringify(v)); } catch (_) {} },
        clear() { try { localStorage.removeItem(USER_KEY); } catch (_) {} }
    };

    async function request(method, path, body, opts) {
        opts = opts || {};
        const headers = Object.assign({}, opts.headers || {});
        const t = PG.token.get();
        if (t) headers['Authorization'] = 'Bearer ' + t;

        let payload;
        if (body instanceof FormData) {
            payload = body;
        } else if (body != null) {
            headers['Content-Type'] = 'application/json';
            payload = JSON.stringify(body);
        }

        let resp;
        try {
            resp = await fetch(path, { method, headers, body: payload, credentials: 'same-origin' });
        } catch (ex) {
            throw new ApiError(0, 'Network error', null);
        }

        const ct = resp.headers.get('content-type') || '';
        let data = null;
        if (ct.indexOf('application/json') >= 0) {
            try { data = await resp.json(); } catch (_) { data = null; }
        } else {
            try { data = await resp.text(); } catch (_) { data = null; }
        }

        if (!resp.ok) {
            const msg = (data && data.message) ? data.message : ('Request failed (' + resp.status + ')');
            if (resp.status === 401 && !opts.suppress401Redirect) {
                PG.token.clear();
                PG.user.clear();
                if (location.hash !== '#/login') location.hash = '#/login';
            }
            throw new ApiError(resp.status, msg, data);
        }
        return data;
    }

    function ApiError(status, message, body) {
        this.status = status;
        this.message = message;
        this.body = body;
    }
    ApiError.prototype = Object.create(Error.prototype);
    ApiError.prototype.name = 'ApiError';
    PG.ApiError = ApiError;

    PG.api = {
        get: (p, opts) => request('GET', p, null, opts),
        post: (p, b, opts) => request('POST', p, b, opts),
        put: (p, b, opts) => request('PUT', p, b, opts),
        del: (p, opts) => request('DELETE', p, null, opts),
        upload: async function (file, type) {
            const fd = new FormData();
            fd.append('file', file);
            if (type) fd.append('type', type);
            return request('POST', '/api/files/upload', fd);
        },
        publicUpload: async function (file) {
            const fd = new FormData();
            fd.append('file', file);
            return request('POST', '/api/public/files/upload', fd);
        }
    };
})(window.PG);
