(function (PG) {

    const NAV = [
        { section: 'Admin' },
        { href: '#/admin/dashboard', label: 'Dashboard', icon: 'dashboard' },
        { href: '#/admin/owners', label: 'PG Owners', icon: 'user' },
        { href: '#/admin/buildings', label: 'Buildings', icon: 'building' },
        { href: '#/admin/receptionists', label: 'Receptionists', icon: 'users' },
        { href: '#/admin/tenants', label: 'Tenants', icon: 'users' }
    ];

    /* ---------- Dashboard ---------- */
    PG.Router.add('/admin/dashboard', ['ADMIN'], async function () {
        const view = PG.UI.renderShell({ nav: NAV, title: 'Admin Dashboard', subtitle: 'Overview across the entire system' });
        if (!view) return;

        view.innerHTML = '<div class="empty muted">Loading…</div>';
        let poll;
        async function refresh() {
            try {
                const d = await PG.api.get('/api/admin/dashboard');
                view.innerHTML = '';
                const kpis = PG.el('div', { class: 'grid grid-4' });
                kpis.innerHTML = `
                    ${PG.Shared.kpiCard('Total Buildings', d.totalBuildings, 'PGs onboarded', 'info', 'building')}
                    ${PG.Shared.kpiCard('Total Tenants', d.totalTenants, 'Across all PGs', 'success', 'users')}
                    ${PG.Shared.kpiCard('Occupied Beds', d.occupiedBeds, `of ${d.totalBeds} total`, 'warning', 'bed')}
                    ${PG.Shared.kpiCard('Available Beds', d.availableBeds, 'Ready to allocate', '', 'bed')}
                    ${PG.Shared.kpiCard('Total Revenue', PG.fmtMoney(d.totalRevenue), 'All-time collected', 'success', 'money')}
                    ${PG.Shared.kpiCard('This Month', PG.fmtMoney(d.monthRevenue), 'Rent collected', 'info', 'money')}
                    ${PG.Shared.kpiCard('Pending Owners', d.pendingOwners, 'Awaiting approval', 'warning', 'user')}
                    ${PG.Shared.kpiCard('Receptionists', d.totalReceptionists, 'Active staff', '', 'users')}
                `;
                view.appendChild(kpis);

                const charts = PG.el('div', { class: 'grid grid-2 mt-20' });
                const donut = PG.el('div', { class: 'chart-card' });
                donut.innerHTML = `
                    <div class="head"><h3>Bed Occupancy</h3><span class="sub">${d.occupiedBeds || 0}/${d.totalBeds || 0}</span></div>
                    ${PG.Charts.donut([
                        { label: 'Occupied', value: d.occupiedBeds || 0, color: '#3858f5' },
                        { label: 'Available', value: d.availableBeds || 0, color: '#15a363' }
                    ], { centerLabel: 'Total Beds', centerValue: d.totalBeds || 0 })}
                `;
                charts.appendChild(donut);

                const bar = PG.el('div', { class: 'chart-card' });
                const bs = d.bedsBySharing || {};
                const os = d.occupancyBySharing || {};
                const bData = ['ONE','TWO','THREE'].map(k => ({ label: PG.sharingLabel(k), value: bs[k] || 0, color: '#7c5cff' }));
                const oData = ['ONE','TWO','THREE'].map(k => ({ label: PG.sharingLabel(k), value: os[k] || 0, color: '#3858f5' }));
                bar.innerHTML = `
                    <div class="head"><h3>Beds by Sharing Type</h3><span class="sub">Total vs Occupied</span></div>
                    ${PG.Charts.bar(bData)}
                    <div class="legend">
                        <span><span class="sw" style="background:#7c5cff"></span>Total</span>
                        <span><span class="sw" style="background:#3858f5"></span>Occupied</span>
                    </div>
                    <div class="mt-12">${PG.Charts.bar(oData)}</div>
                `;
                charts.appendChild(bar);
                view.appendChild(charts);
            } catch (ex) {
                view.innerHTML = `<div class="empty">${PG.escape(ex.message || 'Failed to load')}</div>`;
            }
        }
        await refresh();
        poll = PG.Shared.poller(refresh, 10000);
        return () => poll.stop();
    });

    /* ---------- Owners ---------- */
    PG.Router.add('/admin/owners', ['ADMIN'], async function () {
        const view = PG.UI.renderShell({ nav: NAV, title: 'PG Owners', subtitle: 'Approve and manage owners' });
        if (!view) return;

        view.innerHTML = '';
        const tabsEl = PG.el('div', { class: 'tabs' });
        const tabs = [
            { key: 'PENDING', label: 'Pending' },
            { key: 'APPROVED', label: 'Approved' },
            { key: 'REJECTED', label: 'Rejected' },
            { key: '', label: 'All' }
        ];
        let activeKey = 'PENDING';
        tabs.forEach(t => {
            const b = PG.el('button', { text: t.label, onclick: () => { activeKey = t.key; renderList(); }});
            if (t.key === activeKey) b.classList.add('active');
            tabsEl.appendChild(b);
        });
        view.appendChild(tabsEl);
        const container = PG.el('div');
        view.appendChild(container);

        function renderList() {
            PG.qa('.tabs button', tabsEl).forEach(b => b.classList.remove('active'));
            const idx = tabs.findIndex(t => t.key === activeKey);
            PG.qa('.tabs button', tabsEl)[idx].classList.add('active');
            container.innerHTML = '';
            PG.Shared.tablePager({
                container,
                fetchPage: (s) => {
                    const params = `page=${s.page}&size=${s.size}` + (activeKey ? `&status=${activeKey}` : '');
                    return PG.api.get('/api/admin/owners?' + params);
                },
                emptyMessage: 'No owners in this category',
                columns: [
                    { label: 'Name', render: r => `<b>${PG.escape(r.fullName)}</b><div class="soft" style="font-size:12px">${PG.escape(r.email)}</div>` },
                    { label: 'Phone', key: 'phone' },
                    { label: 'Status', render: r => PG.statusBadge(r.ownerStatus || '-') },
                    { label: 'Registered', render: r => PG.fmtDate(r.createdAt) },
                    { label: '', right: true, render: (r, _i, refresh) => {
                        const actions = PG.el('div', { class: 'actions' });
                        if (r.ownerStatus === 'PENDING' || r.ownerStatus === 'REJECTED') {
                            actions.appendChild(PG.el('button', { class: 'btn sm success', text: 'Approve',
                                onclick: () => PG.UI.confirm('Approve owner',
                                    `Approve ${r.fullName}? They will be able to log in and create buildings.`,
                                    async () => {
                                        await PG.api.post(`/api/admin/owners/${r.id}/approve`);
                                        PG.UI.toast('Owner approved', 'success');
                                        refresh();
                                    }) }));
                        }
                        if (r.ownerStatus !== 'REJECTED') {
                            actions.appendChild(PG.el('button', { class: 'btn sm danger', text: 'Reject',
                                onclick: () => {
                                    PG.UI.modal({
                                        title: 'Reject owner',
                                        lead: `Reject ${r.fullName}? Provide an optional reason.`,
                                        bodyHtml: `<div class="field"><textarea class="textarea" id="reject-reason" placeholder="Reason (optional)"></textarea></div>`,
                                        actions: [
                                            { label: 'Cancel' },
                                            { label: 'Reject', kind: 'danger', onClick: async ({ close }) => {
                                                const reason = PG.q('#reject-reason').value;
                                                await PG.api.post(`/api/admin/owners/${r.id}/reject`, { reason });
                                                PG.UI.toast('Owner rejected', 'success');
                                                close();
                                                refresh();
                                            }}
                                        ]
                                    });
                                } }));
                        }
                        return actions;
                    } }
                ]
            });
        }
        renderList();
    });

    /* ---------- Buildings ---------- */
    PG.Router.add('/admin/buildings', ['ADMIN'], async function () {
        const view = PG.UI.renderShell({ nav: NAV, title: 'Buildings', subtitle: 'All PG buildings in the system' });
        if (!view) return;
        view.innerHTML = '';
        PG.Shared.tablePager({
            container: view,
            search: { placeholder: 'Search buildings by name, city or area…' },
            fetchPage: (s) => PG.api.get(`/api/admin/buildings?page=${s.page}&size=${s.size}` + (s.q ? `&q=${encodeURIComponent(s.q)}` : '')),
            columns: [
                { label: 'Name', render: r => `<b>${PG.escape(r.name)}</b><div class="soft" style="font-size:12px">${PG.escape(r.area || '')}, ${PG.escape(r.city || '')}</div>` },
                { label: 'Owner', render: r => PG.escape(r.ownerName || '-') },
                { label: 'Receptionist', render: r => r.receptionistName ? `${PG.escape(r.receptionistName)}<div class="soft" style="font-size:12px">${PG.escape(r.receptionistEmail)}</div>` : '<span class="soft">-</span>' },
                { label: 'Beds', right: true, render: r => r.availability ? `<b>${r.availability.occupiedBeds}</b><span class="soft">/${r.availability.totalBeds}</span>` : '-' },
                { label: 'Floors', right: true, key: 'totalFloors' },
                { label: 'Created', right: true, render: r => PG.fmtDate(r.createdAt) }
            ]
        });
    });

    /* ---------- Receptionists ---------- */
    PG.Router.add('/admin/receptionists', ['ADMIN'], async function () {
        const view = PG.UI.renderShell({ nav: NAV, title: 'Receptionists', subtitle: 'All receptionists across PGs' });
        if (!view) return;
        view.innerHTML = '';
        PG.Shared.tablePager({
            container: view,
            search: { placeholder: 'Search receptionists…' },
            fetchPage: (s) => PG.api.get(`/api/admin/receptionists?page=${s.page}&size=${s.size}` + (s.q ? `&q=${encodeURIComponent(s.q)}` : '')),
            columns: [
                { label: 'Name', render: r => `<b>${PG.escape(r.fullName)}</b><div class="soft" style="font-size:12px">${PG.escape(r.email)}</div>` },
                { label: 'Phone', key: 'phone' },
                { label: 'Building', render: r => r.buildingId ? '#' + r.buildingId : '<span class="soft">-</span>' },
                { label: 'Joined', right: true, render: r => PG.fmtDate(r.createdAt) }
            ]
        });
    });

    /* ---------- Tenants ---------- */
    PG.Router.add('/admin/tenants', ['ADMIN'], async function () {
        const view = PG.UI.renderShell({ nav: NAV, title: 'Tenants', subtitle: 'All registered tenants' });
        if (!view) return;
        view.innerHTML = '';
        PG.Shared.tablePager({
            container: view,
            search: { placeholder: 'Search tenants…' },
            fetchPage: (s) => PG.api.get(`/api/admin/tenants?page=${s.page}&size=${s.size}` + (s.q ? `&q=${encodeURIComponent(s.q)}` : '')),
            columns: [
                { label: 'Name', render: r => `<b>${PG.escape(r.fullName)}</b><div class="soft" style="font-size:12px">${PG.escape(r.email)}</div>` },
                { label: 'Phone', key: 'phone' },
                { label: 'Status', render: r => r.enabled ? PG.statusBadge('ACTIVE') : '<span class="badge">DISABLED</span>' },
                { label: 'Joined', right: true, render: r => PG.fmtDate(r.createdAt) }
            ]
        });
    });
})(window.PG);
