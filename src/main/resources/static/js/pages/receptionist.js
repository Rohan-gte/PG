(function (PG) {

    const NAV = [
        { section: 'Receptionist' },
        { href: '#/receptionist/dashboard', label: 'Dashboard', icon: 'dashboard' },
        { href: '#/receptionist/requests', label: 'Booking Requests', icon: 'request' },
        { href: '#/receptionist/rooms', label: 'Rooms & Beds', icon: 'bed' },
        { href: '#/receptionist/tenants', label: 'Tenants', icon: 'users' },
        { href: '#/receptionist/payments', label: 'Payments', icon: 'money' }
    ];

    /* ---------- Dashboard ---------- */
    PG.Router.add('/receptionist/dashboard', ['RECEPTIONIST'], async function () {
        const view = PG.UI.renderShell({ nav: NAV, title: 'Receptionist Dashboard', subtitle: 'Manage bookings, allocations and payments' });
        if (!view) return;
        let poll;
        async function refresh() {
            try {
                const d = await PG.api.get('/api/receptionist/dashboard');
                view.innerHTML = '';
                const building = (d.buildings && d.buildings[0]) || null;
                if (!building) {
                    view.appendChild(PG.Shared.emptyEl('No building assigned', 'Please contact your PG owner.'));
                    return;
                }
                PG.UI.setTitle(building.name + ' — Dashboard', `${building.area || ''}, ${building.city || ''}`);
                const kpis = PG.el('div', { class: 'grid grid-4' });
                kpis.innerHTML = `
                    ${PG.Shared.kpiCard('Pending Requests', d.pendingRequests, 'Awaiting your action', 'warning', 'request')}
                    ${PG.Shared.kpiCard('Occupied Beds', d.occupiedBeds, `of ${d.totalBeds}`, 'info', 'bed')}
                    ${PG.Shared.kpiCard('Available Beds', d.availableBeds, 'Ready to allocate', 'success', 'bed')}
                    ${PG.Shared.kpiCard('Active Tenants', d.totalTenants, '', '', 'users')}
                    ${PG.Shared.kpiCard('Total Revenue', PG.fmtMoney(d.totalRevenue), 'All-time collected', 'success', 'money')}
                    ${PG.Shared.kpiCard('This Month', PG.fmtMoney(d.monthRevenue), 'Collected', 'info', 'money')}
                    ${PG.Shared.kpiCard('Unpaid', d.pendingPayments, 'Awaiting payment', 'warning', 'money')}
                    ${PG.Shared.kpiCard('Overdue', d.overduePayments, 'More than 35 days', 'danger', 'money')}
                `;
                view.appendChild(kpis);

                const charts = PG.el('div', { class: 'grid grid-2 mt-20' });
                const donut = PG.el('div', { class: 'chart-card' });
                const av = building.availability || {};
                donut.innerHTML = `
                    <div class="head"><h3>Bed Occupancy</h3><span class="sub">Live</span></div>
                    ${PG.Charts.donut([
                        { label: 'Occupied', value: av.occupiedBeds || 0, color: '#3858f5' },
                        { label: 'Available', value: av.availableBeds || 0, color: '#15a363' }
                    ], { centerLabel: 'Total Beds', centerValue: av.totalBeds || 0 })}
                `;
                charts.appendChild(donut);

                const sharing = PG.el('div', { class: 'chart-card' });
                const ps = av.perSharing || {};
                const data = ['ONE','TWO','THREE'].map(k => ({ label: PG.sharingLabel(k), value: (ps[k] && ps[k].available) || 0, color: '#15a363' }));
                sharing.innerHTML = `
                    <div class="head"><h3>Available beds by Sharing</h3><span class="sub">Live</span></div>
                    ${PG.Charts.bar(data)}
                `;
                charts.appendChild(sharing);
                view.appendChild(charts);

                const link = PG.el('div', { class: 'row mt-20' });
                link.appendChild(PG.el('a', { class: 'btn primary', href: '#/receptionist/requests', text: 'Handle booking requests →' }));
                view.appendChild(link);
            } catch (ex) {
                view.innerHTML = `<div class="empty">${PG.escape(ex.message || 'Failed to load')}</div>`;
            }
        }
        await refresh();
        poll = PG.Shared.poller(refresh, 8000);
        return () => poll.stop();
    });

    /* ---------- Booking Requests ---------- */
    PG.Router.add('/receptionist/requests', ['RECEPTIONIST'], async function () {
        const view = PG.UI.renderShell({ nav: NAV, title: 'Booking Requests', subtitle: 'Review, approve, allocate or reject' });
        if (!view) return;
        view.innerHTML = '';
        const tabs = [
            { k: 'PENDING', l: 'Pending' },
            { k: 'APPROVED', l: 'Approved' },
            { k: 'ALLOCATED', l: 'Allocated' },
            { k: 'REJECTED', l: 'Rejected' },
            { k: '', l: 'All' }
        ];
        let activeKey = 'PENDING';
        const tabsEl = PG.el('div', { class: 'tabs' });
        tabs.forEach(t => {
            const b = PG.el('button', { text: t.l, onclick: () => { activeKey = t.k; renderList(); } });
            if (t.k === activeKey) b.classList.add('active');
            tabsEl.appendChild(b);
        });
        view.appendChild(tabsEl);
        const container = PG.el('div');
        view.appendChild(container);
        let poll;

        function renderList() {
            PG.qa('.tabs button', tabsEl).forEach((b, i) => b.classList.toggle('active', tabs[i].k === activeKey));
            container.innerHTML = '';
            PG.Shared.tablePager({
                container,
                fetchPage: (s) => PG.api.get(`/api/receptionist/requests?page=${s.page}&size=${s.size}&sort=requestedAt,desc` + (activeKey ? `&status=${activeKey}` : '')),
                emptyMessage: 'No requests in this category',
                columns: [
                    { label: 'Tenant', render: r => `<b>${PG.escape(r.tenantName)}</b><div class="soft" style="font-size:12px">${PG.escape(r.tenantEmail)} · ${PG.escape(r.tenantPhone || '')}</div>` },
                    { label: 'Sharing', render: r => PG.sharingLabel(r.sharingType) },
                    { label: 'Status', render: r => PG.statusBadge(r.status) },
                    { label: 'Requested', render: r => PG.fmtDateTime(r.requestedAt) },
                    { label: 'Allocated', render: r => r.allocatedBedNumber ? `Room ${PG.escape(r.allocatedRoomNumber || '-')} / ${PG.escape(r.allocatedBedNumber)}` : '<span class="soft">-</span>' },
                    { label: '', right: true, render: (r, _i, refresh) => actionsCell(r, refresh) }
                ]
            });
        }

        function actionsCell(r, refresh) {
            const cell = PG.el('div', { class: 'actions' });
            if (r.status === 'PENDING') {
                cell.appendChild(PG.el('button', { class: 'btn sm success', text: 'Approve',
                    onclick: async () => {
                        try { await PG.api.post(`/api/receptionist/requests/${r.id}/approve`); PG.UI.toast('Approved','success'); refresh(); }
                        catch (e) { PG.UI.toast(e.message || 'Failed', 'error'); }
                    } }));
                cell.appendChild(PG.el('button', { class: 'btn sm danger', text: 'Reject',
                    onclick: () => rejectModal(r, refresh) }));
            }
            if (r.status === 'PENDING' || r.status === 'APPROVED') {
                cell.appendChild(PG.el('button', { class: 'btn sm primary', text: 'Allocate',
                    onclick: () => allocateModal(r, refresh) }));
            }
            return cell;
        }

        function rejectModal(r, refresh) {
            PG.UI.modal({
                title: 'Reject booking',
                lead: `Reject ${r.tenantName}'s request? Provide an optional reason.`,
                bodyHtml: `<div class="field"><textarea class="textarea" id="r-reason"></textarea></div>`,
                actions: [
                    { label: 'Cancel' },
                    { label: 'Reject', kind: 'danger', onClick: async ({ close }) => {
                        const reason = PG.q('#r-reason').value;
                        await PG.api.post(`/api/receptionist/requests/${r.id}/reject`, { reason });
                        PG.UI.toast('Rejected', 'success');
                        close(); refresh();
                    }}
                ]
            });
        }

        async function allocateModal(r, refresh) {
            let beds = [];
            try {
                beds = await PG.api.get(`/api/receptionist/available-beds?buildingId=${r.buildingId}&sharingType=${r.sharingType}`);
            } catch (ex) {
                PG.UI.toast(ex.message || 'Failed to load beds', 'error');
                return;
            }
            if (!beds.length) {
                PG.UI.toast(`No available ${PG.sharingLabel(r.sharingType)} beds`, 'warning');
                return;
            }
            const body = PG.el('div');
            body.innerHTML = `
                <p class="muted mb-12">Pick an available <b>${PG.sharingLabel(r.sharingType)}</b> bed to assign to <b>${PG.escape(r.tenantName)}</b>.</p>
                <div class="bed-grid" id="bed-grid">
                    ${beds.map(b => `<div class="bed-pill" data-id="${b.id}"><div class="bn">${PG.escape(b.bedNumber)}</div></div>`).join('')}
                </div>
            `;
            let selected = null;
            const grid = body.querySelector('#bed-grid');
            grid.addEventListener('click', e => {
                const pill = e.target.closest('.bed-pill');
                if (!pill) return;
                PG.qa('.bed-pill', grid).forEach(p => p.classList.remove('selected'));
                pill.classList.add('selected');
                selected = Number(pill.dataset.id);
            });
            PG.UI.modal({
                title: 'Allocate Bed',
                bodyEl: body, large: true,
                actions: [
                    { label: 'Cancel' },
                    { label: 'Allocate', kind: 'primary', onClick: async ({ close }) => {
                        if (!selected) { PG.UI.toast('Select a bed', 'warning'); return; }
                        await PG.api.post('/api/receptionist/allocate', { bookingRequestId: r.id, bedId: selected });
                        PG.UI.toast('Bed allocated successfully', 'success');
                        close(); refresh();
                    }}
                ]
            });
        }

        renderList();
        poll = PG.Shared.poller(renderList, 12000);
        return () => poll.stop();
    });

    /* ---------- Rooms & Beds ---------- */
    PG.Router.add('/receptionist/rooms', ['RECEPTIONIST'], async function () {
        const view = PG.UI.renderShell({ nav: NAV, title: 'Rooms & Beds', subtitle: 'Live room and bed status' });
        if (!view) return;
        let poll;
        async function refresh() {
            try {
                const rooms = await PG.api.get('/api/receptionist/rooms');
                view.innerHTML = '';
                if (!rooms.length) { view.appendChild(PG.Shared.emptyEl('No rooms configured')); return; }
                const groups = { ONE: [], TWO: [], THREE: [] };
                rooms.forEach(r => groups[r.sharingType].push(r));
                ['ONE','TWO','THREE'].forEach(st => {
                    if (!groups[st].length) return;
                    const card = PG.el('div', { class: 'card mb-16' });
                    card.innerHTML = `<div class="card-header"><h2>${PG.sharingLabel(st)} (${groups[st].length} rooms)</h2></div>`;
                    const grid = PG.el('div', { class: 'grid grid-3' });
                    groups[st].forEach(r => {
                        const bedsHtml = (r.beds || []).map(b => `
                            <div class="bed-pill ${b.status === 'OCCUPIED' ? 'occupied' : ''}">
                                <div class="bn">${PG.escape(b.bedNumber)}</div>
                                <div class="rn">${b.status === 'OCCUPIED' ? (b.tenantName ? PG.escape(b.tenantName) : 'Occupied') : 'Available'}</div>
                            </div>
                        `).join('');
                        const box = PG.el('div', { class: 'card flat', style: { background: 'var(--surface-2)' } });
                        box.innerHTML = `
                            <div class="row between">
                                <div><b>Room ${PG.escape(r.roomNumber)}</b><div class="soft" style="font-size:12px">Floor ${r.floorNumber} · ${PG.fmtMoney(r.monthlyRent)}</div></div>
                                <span class="badge ${r.availableBeds > 0 ? 'success' : 'warning'}">${r.availableBeds}/${r.totalBeds} free</span>
                            </div>
                            <div class="bed-grid mt-12">${bedsHtml}</div>
                        `;
                        grid.appendChild(box);
                    });
                    card.appendChild(grid);
                    view.appendChild(card);
                });
            } catch (ex) {
                view.innerHTML = `<div class="empty">${PG.escape(ex.message || 'Failed')}</div>`;
            }
        }
        await refresh();
        poll = PG.Shared.poller(refresh, 8000);
        return () => poll.stop();
    });

    /* ---------- Tenants ---------- */
    PG.Router.add('/receptionist/tenants', ['RECEPTIONIST'], async function () {
        const view = PG.UI.renderShell({ nav: NAV, title: 'Tenants', subtitle: 'Active tenants in your building' });
        if (!view) return;
        view.innerHTML = '<div class="empty muted">Loading…</div>';
        try {
            const tenants = await PG.api.get('/api/receptionist/tenants');
            view.innerHTML = '';
            if (!tenants.length) { view.appendChild(PG.Shared.emptyEl('No tenants yet')); return; }
            const tw = PG.el('div', { class: 'table-wrap' });
            const t = PG.el('table', { class: 'table' });
            t.innerHTML = `
                <thead><tr><th>Name</th><th>Email</th><th>Phone</th><th>Joined</th><th></th></tr></thead>
            `;
            const tbody = PG.el('tbody');
            tenants.forEach(u => {
                const tr = PG.el('tr');
                tr.innerHTML = `
                    <td><b>${PG.escape(u.fullName)}</b></td>
                    <td>${PG.escape(u.email)}</td>
                    <td>${PG.escape(u.phone || '-')}</td>
                    <td>${PG.fmtDate(u.createdAt)}</td>
                    <td class="right-cell"></td>
                `;
                const last = tr.lastElementChild;
                last.appendChild(PG.el('button', { class: 'btn sm danger', text: 'Checkout',
                    onclick: () => PG.UI.confirm('Checkout tenant', `Check out ${u.fullName}? Their bed will be marked AVAILABLE.`,
                        async () => {
                            await PG.api.post('/api/receptionist/checkout/' + u.id);
                            PG.UI.toast('Tenant checked out', 'success');
                            location.reload();
                        }, 'danger') }));
                tbody.appendChild(tr);
            });
            t.appendChild(tbody);
            tw.appendChild(t);
            view.appendChild(tw);
        } catch (ex) {
            view.innerHTML = `<div class="empty">${PG.escape(ex.message || 'Failed')}</div>`;
        }
    });

    /* ---------- Payments ---------- */
    PG.Router.add('/receptionist/payments', ['RECEPTIONIST'], async function () {
        const view = PG.UI.renderShell({ nav: NAV, title: 'Payments', subtitle: 'Collect rent, generate receipts, track dues' });
        if (!view) return;
        view.innerHTML = '';

        const tabs = [
            { k: 'UNPAID', l: 'Unpaid' },
            { k: 'OVERDUE', l: 'Overdue' },
            { k: 'PAID', l: 'Paid' },
            { k: '', l: 'All' }
        ];
        let activeKey = 'UNPAID';

        const head = PG.el('div', { class: 'row between mb-16' });
        const tabsEl = PG.el('div', { class: 'tabs' });
        tabs.forEach(t => {
            const b = PG.el('button', { text: t.l, onclick: () => { activeKey = t.k; renderList(); } });
            if (t.k === activeKey) b.classList.add('active');
            tabsEl.appendChild(b);
        });
        head.appendChild(tabsEl);
        head.appendChild(PG.el('button', { class: 'btn primary', text: 'Generate this month',
            onclick: async () => {
                try {
                    const r = await PG.api.post('/api/receptionist/payments/generate');
                    PG.UI.toast(r.message, 'success');
                    renderList();
                } catch (ex) { PG.UI.toast(ex.message || 'Failed', 'error'); }
            } }));
        view.appendChild(head);
        const container = PG.el('div');
        view.appendChild(container);

        function renderList() {
            PG.qa('.tabs button', tabsEl).forEach((b, i) => b.classList.toggle('active', tabs[i].k === activeKey));
            container.innerHTML = '';
            PG.Shared.tablePager({
                container,
                fetchPage: (s) => PG.api.get(`/api/receptionist/payments?page=${s.page}&size=${s.size}` + (activeKey ? `&status=${activeKey}` : '')),
                emptyMessage: 'No payments in this category',
                columns: [
                    { label: 'Tenant', render: r => `<b>${PG.escape(r.tenantName || '-')}</b><div class="soft" style="font-size:12px">${PG.escape(r.tenantEmail || '')}</div>` },
                    { label: 'Bed', render: r => r.roomNumber ? `Room ${PG.escape(r.roomNumber)} / ${PG.escape(r.bedNumber || '-')}` : '<span class="soft">-</span>' },
                    { label: 'Month', render: r => PG.fmtMonth(r.monthYear) },
                    { label: 'Amount', right: true, render: r => `<b>${PG.fmtMoney(r.amount)}</b>` },
                    { label: 'Status', render: r => PG.statusBadge(r.status) },
                    { label: '', right: true, render: (r, _i, refresh) => {
                        const cell = PG.el('div', { class: 'actions' });
                        if (r.status !== 'PAID') {
                            cell.appendChild(PG.el('button', { class: 'btn sm success', text: 'Mark Paid',
                                onclick: () => collectModal(r, refresh) }));
                        } else if (r.receiptNumber) {
                            cell.appendChild(PG.el('button', { class: 'btn sm', text: 'Receipt',
                                onclick: () => PG.Shared.printReceipt(r.receiptNumber) }));
                        }
                        return cell;
                    } }
                ]
            });
        }

        function collectModal(p, refresh) {
            const body = PG.el('div');
            body.innerHTML = `
                <form id="pay-form">
                    <div class="field-row-2">
                        <div class="field"><label class="label">Amount</label><input class="input" name="amount" type="number" min="0" value="${p.amount}" required></div>
                        <div class="field"><label class="label">Method</label>
                            <select class="select" name="paymentMethod">
                                <option>CASH</option><option>UPI</option><option>CARD</option><option>BANK_TRANSFER</option><option>OTHER</option>
                            </select>
                        </div>
                    </div>
                    <div class="field"><label class="label">Notes</label><input class="input" name="notes"></div>
                </form>
            `;
            PG.UI.modal({
                title: `Collect rent — ${PG.fmtMonth(p.monthYear)}`,
                lead: `Tenant: ${p.tenantName || '-'} · Bed: ${p.roomNumber || '-'} / ${p.bedNumber || '-'}`,
                bodyEl: body,
                actions: [
                    { label: 'Cancel' },
                    { label: 'Mark Paid', kind: 'success', onClick: async ({ close }) => {
                        const data = PG.UI.serializeForm(PG.q('#pay-form', body));
                        data.paymentId = p.id;
                        const r = await PG.api.post('/api/receptionist/payments/collect', data);
                        PG.UI.toast('Payment collected', 'success');
                        close(); refresh();
                        if (r && r.receiptNumber) {
                            setTimeout(() => PG.Shared.printReceipt(r.receiptNumber), 200);
                        }
                    }}
                ]
            });
        }

        renderList();
    });
})(window.PG);
