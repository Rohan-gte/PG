(function (PG) {

    const NAV = [
        { section: 'Tenant' },
        { href: '#/tenant/dashboard', label: 'Dashboard', icon: 'dashboard' },
        { href: '#/tenant/browse', label: 'Browse PGs', icon: 'search', match: /^#\/tenant\/(browse|pg)/ },
        { href: '#/tenant/bookings', label: 'My Bookings', icon: 'request' },
        { href: '#/tenant/payments', label: 'Payments', icon: 'money' }
    ];

    /* ---------- Dashboard ---------- */
    PG.Router.add('/tenant/dashboard', ['TENANT'], async function () {
        const view = PG.UI.renderShell({ nav: NAV, title: 'My Dashboard', subtitle: 'Your PG accommodation overview' });
        if (!view) return;
        let poll;
        async function refresh() {
            try {
                const d = await PG.api.get('/api/tenant/dashboard');
                view.innerHTML = '';
                if (!d.allocatedBedId) {
                    const c = PG.el('div', { class: 'card' });
                    c.innerHTML = `
                        <h2 class="mb-8">Welcome!</h2>
                        <p class="muted mb-16">You don't have a PG allocation yet. Browse available PGs and send a booking request.</p>
                        <a class="btn primary" href="#/tenant/browse">Browse PGs</a>
                    `;
                    view.appendChild(c);
                    return;
                }
                const top = PG.el('div', { class: 'card mb-20' });
                top.innerHTML = `
                    <div class="row between">
                        <div>
                            <div class="muted" style="font-size:12.5px">Currently staying at</div>
                            <h2 style="margin:4px 0 0">${PG.escape(d.allocatedBuildingName || '')}</h2>
                        </div>
                        <a class="btn" href="#/tenant/pg/${d.allocatedBuildingId}">View PG details</a>
                    </div>
                    <div class="grid grid-3 mt-16">
                        <div class="kpi info" style="padding:14px"><div class="label">Room</div><div class="value">${PG.escape(d.allocatedRoomNumber || '-')}</div></div>
                        <div class="kpi" style="padding:14px"><div class="label">Bed</div><div class="value">${PG.escape(d.allocatedBedNumber || '-')}</div></div>
                        <div class="kpi success" style="padding:14px"><div class="label">Total Paid</div><div class="value">${PG.fmtMoney(d.totalRevenue)}</div></div>
                    </div>
                `;
                view.appendChild(top);
                const k = PG.el('div', { class: 'grid grid-3' });
                k.innerHTML = `
                    ${PG.Shared.kpiCard('Pending Payments', d.pendingPayments, '', 'warning', 'money')}
                    ${PG.Shared.kpiCard('Overdue Payments', d.overduePayments, '', 'danger', 'money')}
                    ${PG.Shared.kpiCard('Total Paid', PG.fmtMoney(d.totalRevenue), 'Across all months', 'success', 'money')}
                `;
                view.appendChild(k);
            } catch (ex) {
                view.innerHTML = `<div class="empty">${PG.escape(ex.message || 'Failed')}</div>`;
            }
        }
        await refresh();
        poll = PG.Shared.poller(refresh, 12000);
        return () => poll.stop();
    });

    /* ---------- Browse PGs ---------- */
    PG.Router.add('/tenant/browse', ['TENANT'], async function () {
        const view = PG.UI.renderShell({ nav: NAV, title: 'Browse PGs', subtitle: 'Find your next home' });
        if (!view) return;
        view.innerHTML = '';

        const filters = { q: '', city: '', area: '', sharingType: '', maxRent: '', availableOnly: false };

        const toolbar = PG.el('div', { class: 'card mb-16' });
        toolbar.innerHTML = `
            <div class="grid grid-4">
                <div class="field" style="margin:0"><label class="label">Search</label><input class="input" id="f-q" placeholder="Name, area, city"></div>
                <div class="field" style="margin:0"><label class="label">City</label><input class="input" id="f-city" placeholder="e.g. Pune"></div>
                <div class="field" style="margin:0"><label class="label">Area</label><input class="input" id="f-area"></div>
                <div class="field" style="margin:0"><label class="label">Sharing</label>
                    <select class="select" id="f-share">
                        <option value="">Any</option>
                        <option value="ONE">1 Sharing</option>
                        <option value="TWO">2 Sharing</option>
                        <option value="THREE">3 Sharing</option>
                    </select>
                </div>
                <div class="field" style="margin:0"><label class="label">Max Rent</label><input class="input" id="f-rent" type="number" min="0" placeholder="₹"></div>
                <div class="field" style="margin:0;align-self:end"><label class="checkbox"><input type="checkbox" id="f-avail"> Available beds only</label></div>
            </div>
        `;
        view.appendChild(toolbar);

        const grid = PG.el('div', { class: 'grid grid-auto' });
        view.appendChild(grid);
        const pager = PG.el('div');
        view.appendChild(pager);

        const state = { page: 0, size: 12, total: 0 };
        const debounced = PG.debounce(refresh, 350);
        ['f-q','f-city','f-area','f-rent'].forEach(id => PG.q('#' + id).addEventListener('input', e => {
            const map = { 'f-q':'q', 'f-city':'city', 'f-area':'area', 'f-rent':'maxRent' };
            filters[map[id]] = e.target.value;
            state.page = 0; debounced();
        }));
        PG.q('#f-share').addEventListener('change', e => { filters.sharingType = e.target.value; state.page = 0; refresh(); });
        PG.q('#f-avail').addEventListener('change', e => { filters.availableOnly = e.target.checked; state.page = 0; refresh(); });

        async function refresh() {
            grid.innerHTML = '<div class="empty muted">Loading…</div>';
            pager.innerHTML = '';
            const qs = new URLSearchParams();
            qs.set('page', state.page); qs.set('size', state.size);
            ['q','city','area','sharingType','maxRent'].forEach(k => { if (filters[k]) qs.set(k, filters[k]); });
            if (filters.availableOnly) qs.set('availableOnly', 'true');
            try {
                const data = await PG.api.get('/api/public/buildings?' + qs.toString());
                state.total = data.totalElements;
                grid.innerHTML = '';
                if (!data.content.length) { grid.appendChild(PG.Shared.emptyEl('No PGs match your filters')); return; }
                data.content.forEach(b => grid.appendChild(buildingCard(b)));
                pager.appendChild(PG.UI.pager(state, state.total, refresh));
            } catch (ex) {
                grid.innerHTML = `<div class="empty">${PG.escape(ex.message || 'Failed')}</div>`;
            }
        }

        function buildingCard(b) {
            const av = b.availability || {};
            const img = (b.imagePaths && b.imagePaths[0]) ? `<img src="${PG.escape(b.imagePaths[0])}" />` : `<div class="placeholder">${PG.escape((b.name || 'PG').slice(0,2).toUpperCase())}</div>`;
            const minRent = (b.sharingConfigs || []).reduce((m, s) => Math.min(m, Number(s.monthlyRent) || Infinity), Infinity);
            const amenities = (b.amenities || []).slice(0, 4).map(a => `<span class="badge">${PG.escape(a)}</span>`).join(' ');
            const c = PG.el('div', { class: 'bcard', onclick: () => location.hash = '#/tenant/pg/' + b.id });
            c.innerHTML = `
                <div class="img">${img}</div>
                <div class="body">
                    <h3 class="title">${PG.escape(b.name)}</h3>
                    <div class="loc">${PG.escape(b.area || '')}, ${PG.escape(b.city || '')}</div>
                    <div class="amenities">${amenities}</div>
                    <div class="row-amts">
                        <div class="amt"><b>${minRent === Infinity ? '—' : PG.fmtMoney(minRent)}</b>Starting at</div>
                        <div class="amt"><b>${av.availableBeds || 0}</b>Available beds</div>
                    </div>
                </div>
            `;
            return c;
        }

        refresh();
    });

    /* ---------- Building detail ---------- */
    PG.Router.add('/tenant/pg/:id', ['TENANT'], async function (params) {
        const view = PG.UI.renderShell({ nav: NAV, title: 'PG Details', subtitle: '' });
        if (!view) return;
        let poll;
        async function refresh() {
            try {
                const b = await PG.api.get('/api/public/buildings/' + params.id);
                PG.UI.setTitle(b.name, `${b.area || ''}, ${b.city || ''}`);
                view.innerHTML = '';

                const top = PG.el('div', { class: 'card' });
                const av = b.availability || {};
                const images = (b.imagePaths || []);
                const galleryHtml = images.length ?
                    `<div class="gallery" style="grid-template-columns:${images.length >= 3 ? '2fr 1fr 1fr' : '1fr'}">
                        ${images.slice(0, 3).map((p, i) => `<img class="${i === 0 ? 'main' : ''}" src="${PG.escape(p)}" />`).join('')}
                    </div>` :
                    `<div class="img" style="aspect-ratio:16/6;background:linear-gradient(135deg,#dde3f5,#eef2ff);border-radius:10px;display:flex;align-items:center;justify-content:center;color:var(--primary);font-weight:700;font-size:36px;opacity:.4">${PG.escape((b.name || 'PG').slice(0,2).toUpperCase())}</div>`;
                top.innerHTML = galleryHtml + `
                    <div class="row between mt-16">
                        <div>
                            <h2 style="margin:0">${PG.escape(b.name)}</h2>
                            <div class="muted">${PG.escape(b.address || '')}, ${PG.escape(b.city || '')}</div>
                        </div>
                        <div class="row">
                            <span class="badge success"><span class="dot"></span>${av.availableBeds || 0} beds available</span>
                        </div>
                    </div>
                    <p class="muted mt-12">${PG.escape(b.description || '')}</p>
                    <div class="mt-12 row" style="gap:6px;flex-wrap:wrap">${(b.amenities || []).map(a => `<span class="badge">${PG.escape(a)}</span>`).join('')}</div>
                `;
                view.appendChild(top);

                const sharingHead = PG.el('div', { class: 'row between mt-20 mb-12' });
                sharingHead.appendChild(PG.el('h3', { text: 'Choose a sharing type', style: { margin: 0 } }));
                view.appendChild(sharingHead);

                const grid = PG.el('div', { class: 'grid grid-3' });
                (b.sharingConfigs || []).forEach(c => {
                    const ps = (av.perSharing && av.perSharing[c.sharingType]) || {};
                    const avail = ps.available || 0;
                    const card = PG.el('div', { class: 'card' });
                    card.innerHTML = `
                        <div class="row between">
                            <div><h3 style="margin:0">${PG.sharingLabel(c.sharingType)}</h3></div>
                            <span class="badge ${avail > 0 ? 'success' : 'warning'}">${avail} available</span>
                        </div>
                        <div class="muted" style="font-size:12.5px">${c.numRooms} rooms · ${c.bedsPerRoom} beds per room</div>
                        <div class="divider"></div>
                        <div class="row between"><span class="muted">Monthly Rent</span><b style="font-size:18px">${PG.fmtMoney(c.monthlyRent)}</b></div>
                        <div class="row between mt-8"><span class="muted">Deposit</span><b>${PG.fmtMoney(c.depositAmount)}</b></div>
                    `;
                    const btn = PG.el('button', { class: 'btn primary block mt-12', text: avail > 0 ? 'Send Booking Request' : 'No beds available',
                        disabled: avail <= 0,
                        onclick: () => requestBooking(b, c) });
                    card.appendChild(btn);
                    grid.appendChild(card);
                });
                view.appendChild(grid);
            } catch (ex) {
                view.innerHTML = `<div class="empty">${PG.escape(ex.message || 'Failed')}</div>`;
            }
        }

        function requestBooking(b, c) {
            const body = PG.el('div');
            body.innerHTML = `
                <div class="muted mb-12">You're requesting <b>${PG.sharingLabel(c.sharingType)}</b> at <b>${PG.escape(b.name)}</b>.</div>
                <div class="grid grid-2 mb-12">
                    <div class="kpi" style="padding:12px"><div class="label">Monthly Rent</div><div class="value" style="font-size:18px">${PG.fmtMoney(c.monthlyRent)}</div></div>
                    <div class="kpi info" style="padding:12px"><div class="label">Deposit</div><div class="value" style="font-size:18px">${PG.fmtMoney(c.depositAmount)}</div></div>
                </div>
                <div class="field"><label class="label">Notes for the receptionist (optional)</label><textarea class="textarea" id="bk-notes"></textarea></div>
            `;
            PG.UI.modal({
                title: 'Send Booking Request',
                bodyEl: body,
                actions: [
                    { label: 'Cancel' },
                    { label: 'Send Request', kind: 'primary', onClick: async ({ close }) => {
                        await PG.api.post('/api/tenant/bookings', {
                            buildingId: b.id, sharingType: c.sharingType, notes: PG.q('#bk-notes').value
                        });
                        PG.UI.toast('Booking request sent', 'success');
                        close();
                        location.hash = '#/tenant/bookings';
                    }}
                ]
            });
        }

        await refresh();
        poll = PG.Shared.poller(refresh, 12000);
        return () => poll.stop();
    });

    /* ---------- My Bookings ---------- */
    PG.Router.add('/tenant/bookings', ['TENANT'], async function () {
        const view = PG.UI.renderShell({ nav: NAV, title: 'My Bookings', subtitle: 'All your booking requests' });
        if (!view) return;
        view.innerHTML = '';
        PG.Shared.tablePager({
            container: view,
            fetchPage: (s) => PG.api.get(`/api/tenant/bookings?page=${s.page}&size=${s.size}&sort=requestedAt,desc`),
            emptyMessage: 'You haven\'t made any booking requests yet',
            columns: [
                { label: 'PG', render: r => `<b>${PG.escape(r.buildingName || '-')}</b>` },
                { label: 'Sharing', render: r => PG.sharingLabel(r.sharingType) },
                { label: 'Status', render: r => PG.statusBadge(r.status) },
                { label: 'Requested', render: r => PG.fmtDateTime(r.requestedAt) },
                { label: 'Allocated', render: r => r.allocatedBedNumber ? `Room ${PG.escape(r.allocatedRoomNumber || '-')} / ${PG.escape(r.allocatedBedNumber)}` : '<span class="soft">-</span>' },
                { label: 'Notes', render: r => r.decisionNote ? PG.escape(r.decisionNote) : '<span class="soft">-</span>' },
                { label: '', right: true, render: (r, _i, refresh) => {
                    if (r.status === 'PENDING' || r.status === 'APPROVED') {
                        return PG.el('button', { class: 'btn sm danger', text: 'Cancel', onclick: () =>
                            PG.UI.confirm('Cancel request', 'Are you sure?', async () => {
                                await PG.api.post(`/api/tenant/bookings/${r.id}/cancel`);
                                PG.UI.toast('Request cancelled', 'success');
                                refresh();
                            }, 'danger') });
                    }
                    return document.createTextNode('');
                }}
            ]
        });
    });

    /* ---------- Payments ---------- */
    PG.Router.add('/tenant/payments', ['TENANT'], async function () {
        const view = PG.UI.renderShell({ nav: NAV, title: 'My Payments', subtitle: 'Paid, pending and overdue rent' });
        if (!view) return;

        const tabsEl = PG.el('div', { class: 'tabs' });
        const tabs = [
            { k: '', l: 'All' },
            { k: 'UNPAID', l: 'Unpaid' },
            { k: 'OVERDUE', l: 'Overdue' },
            { k: 'PAID', l: 'Paid' }
        ];
        let activeKey = '';
        tabs.forEach(t => {
            const b = PG.el('button', { text: t.l, onclick: () => { activeKey = t.k; render(); } });
            if (t.k === activeKey) b.classList.add('active');
            tabsEl.appendChild(b);
        });
        view.innerHTML = '';
        view.appendChild(tabsEl);
        const container = PG.el('div');
        view.appendChild(container);

        async function render() {
            PG.qa('.tabs button', tabsEl).forEach((b, i) => b.classList.toggle('active', tabs[i].k === activeKey));
            container.innerHTML = '<div class="empty muted">Loading…</div>';
            try {
                const payments = await PG.api.get('/api/tenant/payments');
                const list = activeKey ? payments.filter(p => p.status === activeKey) : payments;
                container.innerHTML = '';
                if (!list.length) { container.appendChild(PG.Shared.emptyEl('Nothing to show here')); return; }
                const tw = PG.el('div', { class: 'table-wrap' });
                const t = PG.el('table', { class: 'table' });
                t.innerHTML = `
                    <thead><tr><th>Month</th><th>PG</th><th>Bed</th><th>Amount</th><th>Status</th><th>Paid On</th><th></th></tr></thead>
                `;
                const tbody = PG.el('tbody');
                list.forEach(p => {
                    const tr = PG.el('tr');
                    tr.innerHTML = `
                        <td><b>${PG.escape(PG.fmtMonth(p.monthYear))}</b></td>
                        <td>${PG.escape(p.buildingName || '-')}</td>
                        <td>${p.roomNumber ? `Room ${PG.escape(p.roomNumber)} / ${PG.escape(p.bedNumber || '-')}` : '<span class="soft">-</span>'}</td>
                        <td><b>${PG.fmtMoney(p.amount)}</b></td>
                        <td>${PG.statusBadge(p.status)}</td>
                        <td>${PG.fmtDateTime(p.paidAt)}</td>
                        <td class="right-cell"></td>
                    `;
                    const last = tr.lastElementChild;
                    if (p.status !== 'PAID') {
                        last.appendChild(PG.el('button', { class: 'btn sm primary', text: 'Pay now',
                            onclick: () => PG.UI.confirm('Pay rent',
                                `Pay ${PG.fmtMoney(p.amount)} for ${PG.fmtMonth(p.monthYear)}? (Demo: this marks the payment as PAID online.)`,
                                async () => {
                                    const r = await PG.api.post(`/api/tenant/payments/${p.id}/pay`);
                                    PG.UI.toast('Payment successful', 'success');
                                    if (r && r.receiptNumber) setTimeout(() => PG.Shared.printReceipt(r.receiptNumber), 200);
                                    render();
                                }) }));
                    } else if (p.receiptNumber) {
                        last.appendChild(PG.el('button', { class: 'btn sm', text: 'Receipt',
                            onclick: () => PG.Shared.printReceipt(p.receiptNumber) }));
                    }
                    tbody.appendChild(tr);
                });
                t.appendChild(tbody);
                tw.appendChild(t);
                container.appendChild(tw);
            } catch (ex) {
                container.innerHTML = `<div class="empty">${PG.escape(ex.message || 'Failed')}</div>`;
            }
        }
        render();
    });
})(window.PG);
