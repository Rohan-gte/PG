window.PG = window.PG || {};
(function (PG) {
    PG.q = (sel, root) => (root || document).querySelector(sel);
    PG.qa = (sel, root) => Array.from((root || document).querySelectorAll(sel));

    PG.el = function (tag, attrs, children) {
        const e = document.createElement(tag);
        if (attrs) for (const k in attrs) {
            const v = attrs[k];
            if (v == null) continue;
            if (k === 'class') e.className = v;
            else if (k === 'style' && typeof v === 'object') Object.assign(e.style, v);
            else if (k.startsWith('on') && typeof v === 'function') e.addEventListener(k.slice(2).toLowerCase(), v);
            else if (k === 'html') e.innerHTML = v;
            else if (k === 'text') e.textContent = v;
            else if (k in e && typeof e[k] !== 'function' && k !== 'list') e[k] = v;
            else e.setAttribute(k, v);
        }
        if (children != null) {
            const arr = Array.isArray(children) ? children : [children];
            for (const c of arr) {
                if (c == null || c === false) continue;
                if (typeof c === 'string' || typeof c === 'number') e.appendChild(document.createTextNode(String(c)));
                else e.appendChild(c);
            }
        }
        return e;
    };

    PG.escape = function (s) {
        return String(s == null ? '' : s).replace(/[&<>"']/g, c => ({
            '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
        })[c]);
    };

    PG.fmtMoney = function (v) {
        if (v == null || v === '') return '—';
        const n = Number(v);
        if (Number.isNaN(n)) return '—';
        return '₹' + n.toLocaleString('en-IN', { maximumFractionDigits: 0 });
    };

    PG.fmtDate = function (iso) {
        if (!iso) return '—';
        try {
            const d = new Date(iso);
            return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
        } catch (_) { return '—'; }
    };

    PG.fmtDateTime = function (iso) {
        if (!iso) return '—';
        try {
            const d = new Date(iso);
            return d.toLocaleString('en-IN', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
        } catch (_) { return '—'; }
    };

    PG.fmtMonth = function (ym) {
        if (!ym) return '—';
        const [y, m] = ym.split('-');
        if (!y || !m) return ym;
        const d = new Date(Number(y), Number(m) - 1, 1);
        return d.toLocaleDateString('en-IN', { month: 'long', year: 'numeric' });
    };

    PG.initials = function (name) {
        if (!name) return '?';
        return name.trim().split(/\s+/).slice(0, 2).map(s => s[0]).join('').toUpperCase();
    };

    PG.debounce = function (fn, ms) {
        let t; return function () {
            const a = arguments, c = this; clearTimeout(t);
            t = setTimeout(() => fn.apply(c, a), ms || 250);
        };
    };

    PG.iconMap = {
        dashboard: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="9"/><rect x="14" y="3" width="7" height="5"/><rect x="14" y="12" width="7" height="9"/><rect x="3" y="16" width="7" height="5"/></svg>',
        building: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="2" width="16" height="20"/><line x1="9" y1="22" x2="9" y2="18"/><line x1="15" y1="22" x2="15" y2="18"/><circle cx="9" cy="8" r="0.5" fill="currentColor"/><circle cx="15" cy="8" r="0.5" fill="currentColor"/><circle cx="9" cy="13" r="0.5" fill="currentColor"/><circle cx="15" cy="13" r="0.5" fill="currentColor"/></svg>',
        users: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>',
        user: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>',
        request: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="9" y1="15" x2="15" y2="15"/></svg>',
        bed: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 9v9"/><path d="M22 18v-7a3 3 0 0 0-3-3H2"/><circle cx="7" cy="13" r="2"/><path d="M22 18H2"/></svg>',
        money: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>',
        search: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>',
        logout: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>',
        add: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>',
        check: '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>',
        x: '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>',
        menu: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg>',
        chart: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>',
        bell: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>',
        receipt: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 2v20l3-2 3 2 3-2 3 2 3-2 3 2V2l-3 2-3-2-3 2-3-2-3 2-3-2z"/><line x1="8" y1="9" x2="16" y2="9"/><line x1="8" y1="13" x2="16" y2="13"/></svg>'
    };
    PG.icon = function (name) {
        const s = PG.iconMap[name];
        if (!s) return '';
        return s;
    };

    PG.statusBadge = function (status) {
        const map = {
            PENDING: 'warning', APPROVED: 'info', REJECTED: 'danger',
            ALLOCATED: 'success', CANCELLED: '',
            AVAILABLE: 'success', OCCUPIED: 'info',
            PAID: 'success', UNPAID: 'warning', OVERDUE: 'danger'
        };
        const cls = map[status] || '';
        return `<span class="badge ${cls}"><span class="dot"></span>${PG.escape(status)}</span>`;
    };

    PG.roleLabel = function (role) {
        return ({ ADMIN: 'Administrator', OWNER: 'PG Owner', RECEPTIONIST: 'Receptionist', TENANT: 'Tenant' })[role] || role;
    };

    PG.sharingLabel = function (st) {
        return ({ ONE: '1 Sharing', TWO: '2 Sharing', THREE: '3 Sharing' })[st] || st;
    };
})(window.PG);
