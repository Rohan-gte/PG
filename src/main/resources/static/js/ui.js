(function (PG) {
    PG.UI = {};

    /* -------- Toast -------- */
    PG.UI.toast = function (msg, kind) {
        const root = PG.q('#toast-root');
        if (!root) return;
        const t = PG.el('div', { class: 'toast ' + (kind || ''), text: msg });
        root.appendChild(t);
        requestAnimationFrame(() => t.classList.add('show'));
        setTimeout(() => {
            t.classList.remove('show');
            setTimeout(() => t.remove(), 250);
        }, 3200);
    };

    /* -------- Modal -------- */
    PG.UI.modal = function (opts) {
        const root = PG.q('#modal-root');
        if (!root) return;
        root.innerHTML = '';
        const box = PG.el('div', { class: 'modal' + (opts.large ? ' lg' : '') });
        if (opts.title) box.appendChild(PG.el('h3', { text: opts.title }));
        if (opts.lead) box.appendChild(PG.el('p', { class: 'lead', text: opts.lead }));
        if (opts.bodyEl) box.appendChild(opts.bodyEl);
        else if (opts.bodyHtml) {
            const wrap = PG.el('div');
            wrap.innerHTML = opts.bodyHtml;
            box.appendChild(wrap);
        }
        const actions = PG.el('div', { class: 'modal-actions' });
        const close = () => { root.classList.remove('open'); root.innerHTML = ''; };
        if (opts.actions) {
            opts.actions.forEach(a => {
                actions.appendChild(PG.el('button', {
                    class: 'btn ' + (a.kind || ''),
                    text: a.label,
                    onclick: () => {
                        if (a.onClick) {
                            const r = a.onClick({ close });
                            if (r && typeof r.then === 'function') r.then(close).catch(() => {});
                        } else close();
                    }
                }));
            });
        } else {
            actions.appendChild(PG.el('button', { class: 'btn', text: 'Close', onclick: close }));
        }
        box.appendChild(actions);
        root.appendChild(box);
        root.classList.add('open');
        root.onclick = e => { if (e.target === root) close(); };
        return { close };
    };

    PG.UI.confirm = function (title, message, onConfirm, kind) {
        PG.UI.modal({
            title,
            lead: message,
            actions: [
                { label: 'Cancel', kind: '' },
                {
                    label: 'Confirm', kind: kind || 'primary',
                    onClick: async ({ close }) => {
                        try { await onConfirm(); close(); }
                        catch (e) { PG.UI.toast(e.message || 'Failed', 'error'); }
                    }
                }
            ]
        });
    };

    /* -------- Form helper -------- */
    PG.UI.serializeForm = function (form) {
        const data = {};
        for (const el of form.elements) {
            if (!el.name) continue;
            if (el.type === 'checkbox') data[el.name] = el.checked;
            else if (el.type === 'number') data[el.name] = el.value === '' ? null : Number(el.value);
            else data[el.name] = el.value;
        }
        return data;
    };

    /* -------- Topbar / Shell rendering -------- */
    PG.UI.renderShell = function (rolePage) {
        const user = PG.Auth.currentUser();
        if (!user) { location.hash = '#/login'; return null; }

        const app = PG.q('#app');
        app.innerHTML = '';

        const shell = PG.el('div', { class: 'app-shell' });

        const sidebar = PG.el('aside', { class: 'sidebar', id: 'sidebar' });
        sidebar.appendChild(PG.el('div', {
            class: 'sidebar-brand', html: '<span class="mark">PG</span><span>PG Manager</span>'
        }));
        const navItems = (rolePage.nav || []).filter(n => n);
        navItems.forEach(item => {
            if (item.section) {
                sidebar.appendChild(PG.el('div', { class: 'sidebar-section', text: item.section }));
            } else {
                const a = PG.el('a', { class: 'sidebar-link', href: item.href });
                a.innerHTML = `<span class="ico">${PG.icon(item.icon)}</span><span>${PG.escape(item.label)}</span>`;
                if (location.hash === item.href || (item.match && item.match.test(location.hash))) a.classList.add('active');
                sidebar.appendChild(a);
            }
        });
        sidebar.appendChild(PG.el('div', { style: { flex: '1' } }));
        const logoutLink = PG.el('a', { class: 'sidebar-link', href: '#/logout' });
        logoutLink.innerHTML = `<span class="ico">${PG.icon('logout')}</span><span>Sign out</span>`;
        sidebar.appendChild(logoutLink);
        shell.appendChild(sidebar);

        const topbar = PG.el('header', { class: 'topbar' });
        const left = PG.el('div', { class: 'row', style: { gap: '12px' } });
        const menuBtn = PG.el('button', { class: 'mobile-menu-btn', html: PG.icon('menu'),
            onclick: () => sidebar.classList.toggle('open') });
        left.appendChild(menuBtn);
        const titleEl = PG.el('div', { class: 'topbar-title' });
        titleEl.appendChild(PG.el('h1', { id: 'page-title', text: rolePage.title || '' }));
        titleEl.appendChild(PG.el('p', { id: 'page-subtitle', text: rolePage.subtitle || '' }));
        left.appendChild(titleEl);

        const right = PG.el('div', { class: 'topbar-actions' });
        const menu = PG.el('div', { class: 'user-menu', onclick: () => {
            PG.UI.modal({
                title: user.fullName,
                lead: user.email + ' · ' + PG.roleLabel(user.role),
                actions: [
                    { label: 'Sign out', kind: 'danger', onClick: () => PG.Auth.logout() },
                    { label: 'Close' }
                ]
            });
        } });
        menu.innerHTML = `
            <div class="user-avatar">${PG.escape(PG.initials(user.fullName))}</div>
            <div class="user-meta"><span class="n">${PG.escape(user.fullName)}</span><span class="r">${PG.escape(PG.roleLabel(user.role))}</span></div>
        `;
        right.appendChild(menu);

        topbar.appendChild(left);
        topbar.appendChild(right);
        shell.appendChild(topbar);

        const main = PG.el('main', { id: 'view' });
        shell.appendChild(main);

        app.appendChild(shell);

        document.addEventListener('click', function (e) {
            if (window.innerWidth > 980) return;
            if (sidebar.classList.contains('open') && !sidebar.contains(e.target) && e.target !== menuBtn && !menuBtn.contains(e.target)) {
                sidebar.classList.remove('open');
            }
        });

        return main;
    };

    PG.UI.setTitle = function (title, subtitle) {
        const t = PG.q('#page-title'); if (t) t.textContent = title || '';
        const s = PG.q('#page-subtitle'); if (s) s.textContent = subtitle || '';
    };

    /* -------- Polling helper -------- */
    PG.UI.poll = function (fn, ms) {
        let id = setInterval(() => { try { fn(); } catch (_) {} }, ms || 8000);
        return { stop: () => clearInterval(id) };
    };

    /* -------- Simple paged data table for arrays + spring page -------- */
    PG.UI.pager = function (state, total, onPage) {
        const totalPages = Math.max(1, Math.ceil(total / state.size));
        const wrap = PG.el('div', { class: 'pager' });
        wrap.appendChild(PG.el('span', { class: 'info', text: `${total} total · Page ${state.page + 1}/${totalPages}` }));
        const prev = PG.el('button', { class: 'btn sm', text: 'Prev', disabled: state.page === 0,
            onclick: () => { state.page = Math.max(0, state.page - 1); onPage(); } });
        const next = PG.el('button', { class: 'btn sm', text: 'Next', disabled: state.page + 1 >= totalPages,
            onclick: () => { state.page = state.page + 1; onPage(); } });
        wrap.appendChild(prev); wrap.appendChild(next);
        return wrap;
    };
})(window.PG);
