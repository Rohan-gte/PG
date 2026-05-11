(function (PG) {

    const NAV = [
        { section: 'Owner' },
        { href: '#/owner/dashboard', label: 'Dashboard', icon: 'dashboard' },
        { href: '#/owner/buildings', label: 'My Buildings', icon: 'building', match: /^#\/owner\/(buildings|building)/ },
        { href: '#/owner/buildings/new', label: 'Add Building', icon: 'add' }
    ];

    /* ---------- Dashboard ---------- */
    PG.Router.add('/owner/dashboard', ['OWNER'], async function () {
        const view = PG.UI.renderShell({ nav: NAV, title: 'Owner Dashboard', subtitle: 'Performance overview of your PGs' });
        if (!view) return;
        let poll;
        async function refresh() {
            try {
                const d = await PG.api.get('/api/owner/dashboard');
                view.innerHTML = '';
                const kpis = PG.el('div', { class: 'grid grid-4' });
                kpis.innerHTML = `
                    ${PG.Shared.kpiCard('My Buildings', d.totalBuildings, 'PGs you operate', 'info', 'building')}
                    ${PG.Shared.kpiCard('Tenants', d.totalTenants, 'Active across PGs', 'success', 'users')}
                    ${PG.Shared.kpiCard('Occupancy', `${d.occupiedBeds}/${d.totalBeds}`, `${d.availableBeds} available`, 'warning', 'bed')}
                    ${PG.Shared.kpiCard('Pending Requests', d.pendingRequests, 'Across all buildings', 'info', 'request')}
                    ${PG.Shared.kpiCard('Total Revenue', PG.fmtMoney(d.totalRevenue), 'All-time collected', 'success', 'money')}
                    ${PG.Shared.kpiCard('This Month', PG.fmtMoney(d.monthRevenue), 'Collected this month', 'info', 'money')}
                `;
                view.appendChild(kpis);

                const buildings = d.buildings || [];
                if (!buildings.length) {
                    const empty = PG.el('div', { class: 'card mt-20' });
                    empty.innerHTML = `
                        <h2 class="mb-8">Welcome to PG Manager</h2>
                        <p class="muted mb-16">You haven't added any PG buildings yet. Get started by creating one.</p>
                        <a class="btn primary" href="#/owner/buildings/new">Add your first building</a>
                    `;
                    view.appendChild(empty);
                    return;
                }

                const head = PG.el('div', { class: 'row between mt-24 mb-12' });
                head.appendChild(PG.el('h3', { text: 'Buildings overview', style: { margin: 0 } }));
                head.appendChild(PG.el('a', { class: 'btn primary sm', href: '#/owner/buildings/new', text: '+ Add building' }));
                view.appendChild(head);

                const grid = PG.el('div', { class: 'grid grid-2' });
                buildings.forEach(b => {
                    const av = b.availability || {};
                    const occ = av.occupiedBeds || 0;
                    const tot = av.totalBeds || 0;
                    const card = PG.el('div', { class: 'card' });
                    card.innerHTML = `
                        <div class="row between">
                            <div>
                                <h3 style="margin:0">${PG.escape(b.name)}</h3>
                                <div class="soft" style="font-size:12.5px">${PG.escape(b.area || '')}, ${PG.escape(b.city || '')}</div>
                            </div>
                            <a class="btn sm" href="#/owner/buildings/${b.id}">Manage</a>
                        </div>
                        <div class="grid grid-3 mt-16">
                            <div class="kpi" style="padding:12px"><div class="label">Beds</div><div class="value" style="font-size:20px">${occ}/${tot}</div></div>
                            <div class="kpi success" style="padding:12px"><div class="label">Available</div><div class="value" style="font-size:20px">${av.availableBeds || 0}</div></div>
                            <div class="kpi info" style="padding:12px"><div class="label">Floors</div><div class="value" style="font-size:20px">${b.totalFloors || '-'}</div></div>
                        </div>
                    `;
                    grid.appendChild(card);
                });
                view.appendChild(grid);
            } catch (ex) {
                view.innerHTML = `<div class="empty">${PG.escape(ex.message || 'Failed to load')}</div>`;
            }
        }
        await refresh();
        poll = PG.Shared.poller(refresh, 10000);
        return () => poll.stop();
    });

    /* ---------- Buildings list ---------- */
    PG.Router.add('/owner/buildings', ['OWNER'], async function () {
        const view = PG.UI.renderShell({ nav: NAV, title: 'My Buildings', subtitle: 'Manage all PGs you own' });
        if (!view) return;
        view.innerHTML = '<div class="empty muted">Loading…</div>';
        try {
            const items = await PG.api.get('/api/owner/buildings');
            view.innerHTML = '';
            const head = PG.el('div', { class: 'row between mb-16' });
            head.appendChild(PG.el('div'));
            head.appendChild(PG.el('a', { class: 'btn primary', href: '#/owner/buildings/new', text: '+ New building' }));
            view.appendChild(head);
            if (!items.length) {
                view.appendChild(PG.Shared.emptyEl('No buildings yet', 'Add a new building to get started.'));
                return;
            }
            const grid = PG.el('div', { class: 'grid grid-auto' });
            items.forEach(b => {
                const av = b.availability || {};
                const img = (b.imagePaths && b.imagePaths[0]) ? `<img src="${PG.escape(b.imagePaths[0])}" />` : `<div class="placeholder">${PG.escape((b.name || 'PG').slice(0,2).toUpperCase())}</div>`;
                const c = PG.el('div', { class: 'bcard', onclick: () => location.hash = '#/owner/buildings/' + b.id });
                c.innerHTML = `
                    <div class="img">${img}</div>
                    <div class="body">
                        <h3 class="title">${PG.escape(b.name)}</h3>
                        <div class="loc">${PG.escape(b.area || '')}, ${PG.escape(b.city || '')}</div>
                        <div class="row-amts">
                            <div class="amt"><b>${av.totalBeds || 0}</b>Total beds</div>
                            <div class="amt"><b>${av.occupiedBeds || 0}</b>Occupied</div>
                            <div class="amt"><b>${av.availableBeds || 0}</b>Available</div>
                        </div>
                    </div>
                `;
                grid.appendChild(c);
            });
            view.appendChild(grid);
        } catch (ex) {
            view.innerHTML = `<div class="empty">${PG.escape(ex.message || 'Failed to load')}</div>`;
        }
    });

    /* ---------- Building detail ---------- */
    PG.Router.add('/owner/buildings/:id', ['OWNER'], async function (params) {
        const view = PG.UI.renderShell({ nav: NAV, title: 'Building', subtitle: '' });
        if (!view) return;
        view.innerHTML = '<div class="empty muted">Loading…</div>';
        const id = params.id;
        let activeTab = 'overview';
        let poll;

        async function load() {
            const [b, rooms] = await Promise.all([
                PG.api.get('/api/owner/buildings/' + id),
                PG.api.get('/api/owner/buildings/' + id + '/rooms')
            ]);
            PG.UI.setTitle(b.name, `${b.area || ''}, ${b.city || ''}`);
            view.innerHTML = '';

            const actions = PG.el('div', { class: 'row between mb-16' });
            actions.appendChild(PG.el('a', { class: 'btn ghost', href: '#/owner/buildings', text: '← Back' }));
            const btns = PG.el('div', { class: 'row' });
            btns.appendChild(PG.el('button', { class: 'btn', text: 'Edit', onclick: () => editBuilding(b) }));
            btns.appendChild(PG.el('button', { class: 'btn danger', text: 'Delete', onclick: () => {
                PG.UI.confirm('Delete building', 'This permanently removes the building, rooms, beds, and the receptionist user. Occupied beds will block deletion.',
                    async () => {
                        await PG.api.del('/api/owner/buildings/' + id);
                        PG.UI.toast('Building deleted', 'success');
                        location.hash = '#/owner/buildings';
                    }, 'danger');
            } }));
            actions.appendChild(btns);
            view.appendChild(actions);

            const tabsEl = PG.el('div', { class: 'tabs' });
            const tabsDef = [
                { k: 'overview', l: 'Overview' },
                { k: 'rooms', l: 'Rooms & Beds' },
                { k: 'sharings', l: 'Sharing & Pricing' }
            ];
            tabsDef.forEach(t => {
                const btn = PG.el('button', { text: t.l, onclick: () => { activeTab = t.k; renderTab(); } });
                if (t.k === activeTab) btn.classList.add('active');
                tabsEl.appendChild(btn);
            });
            view.appendChild(tabsEl);
            const tabBody = PG.el('div');
            view.appendChild(tabBody);

            function renderTab() {
                PG.qa('.tabs button', tabsEl).forEach((b, i) => { b.classList.toggle('active', tabsDef[i].k === activeTab); });
                tabBody.innerHTML = '';
                if (activeTab === 'overview') tabBody.appendChild(renderOverview(b));
                else if (activeTab === 'rooms') tabBody.appendChild(renderRooms(b, rooms));
                else if (activeTab === 'sharings') tabBody.appendChild(renderSharings(b));
            }
            renderTab();
        }

        function renderOverview(b) {
            const wrap = PG.el('div');
            const av = b.availability || {};
            const kpis = PG.el('div', { class: 'grid grid-4 mb-16' });
            kpis.innerHTML = `
                ${PG.Shared.kpiCard('Total Beds', av.totalBeds || 0, '', 'info', 'bed')}
                ${PG.Shared.kpiCard('Occupied', av.occupiedBeds || 0, '', 'warning', 'bed')}
                ${PG.Shared.kpiCard('Available', av.availableBeds || 0, '', 'success', 'bed')}
                ${PG.Shared.kpiCard('Floors', b.totalFloors || 0, '', '', 'building')}
            `;
            wrap.appendChild(kpis);

            const info = PG.el('div', { class: 'card' });
            const amenities = (b.amenities || []).map(a => `<span class="badge">${PG.escape(a)}</span>`).join(' ');
            const images = (b.imagePaths || []).map(p => `<img class="t" src="${PG.escape(p)}" />`).join('');
            info.innerHTML = `
                <div class="grid grid-2">
                    <div>
                        <h3 style="margin:0 0 8px">Address</h3>
                        <div class="muted">${PG.escape(b.address || '')}</div>
                        <div class="muted">${PG.escape(b.area || '')}, ${PG.escape(b.city || '')}</div>
                        <div class="mt-12 muted" style="font-size:12.5px">${PG.escape(b.contactPhone || '')} · ${PG.escape(b.contactEmail || '')}</div>
                    </div>
                    <div>
                        <h3 style="margin:0 0 8px">Receptionist</h3>
                        <div><b>${PG.escape(b.receptionistName || '-')}</b></div>
                        <div class="muted">${PG.escape(b.receptionistEmail || '')}</div>
                    </div>
                </div>
                <div class="divider"></div>
                <h3 style="margin:0 0 8px">Description</h3>
                <p class="muted">${PG.escape(b.description || '—')}</p>
                <h3 style="margin:16px 0 8px">Amenities</h3>
                <div class="row" style="gap:6px;flex-wrap:wrap">${amenities || '<span class="soft">None listed</span>'}</div>
                <h3 style="margin:16px 0 8px">Images</h3>
                <div class="thumbs">${images || '<span class="soft">None uploaded</span>'}</div>
            `;
            wrap.appendChild(info);
            return wrap;
        }

        function renderRooms(b, rooms) {
            const wrap = PG.el('div');
            if (!rooms || !rooms.length) {
                wrap.appendChild(PG.Shared.emptyEl('No rooms generated yet'));
                return wrap;
            }
            const groups = { ONE: [], TWO: [], THREE: [] };
            rooms.forEach(r => groups[r.sharingType].push(r));
            ['ONE','TWO','THREE'].forEach(st => {
                if (!groups[st].length) return;
                const card = PG.el('div', { class: 'card mb-16' });
                card.innerHTML = `<div class="card-header"><h2>${PG.sharingLabel(st)} (${groups[st].length} rooms)</h2></div>`;
                const tw = PG.el('div', { class: 'table-wrap' });
                const t = PG.el('table', { class: 'table' });
                t.innerHTML = `
                    <thead><tr>
                        <th>Room</th><th>Floor</th><th>Beds</th><th>Rent</th><th>Deposit</th><th>Occupancy</th><th></th>
                    </tr></thead>
                `;
                const tbody = PG.el('tbody');
                groups[st].forEach(r => {
                    const tr = PG.el('tr');
                    tr.innerHTML = `
                        <td><b>${PG.escape(r.roomNumber)}</b></td>
                        <td>${r.floorNumber}</td>
                        <td>${r.totalBeds}</td>
                        <td>${PG.fmtMoney(r.monthlyRent)}</td>
                        <td>${PG.fmtMoney(r.depositAmount)}</td>
                        <td><b>${r.occupiedBeds || 0}</b><span class="soft">/${r.totalBeds}</span></td>
                        <td class="right-cell"></td>
                    `;
                    const actCell = tr.lastElementChild;
                    actCell.appendChild(PG.el('button', { class: 'btn sm', text: 'Edit',
                        onclick: () => editRoom(r) }));
                    actCell.appendChild(PG.el('button', { class: 'btn sm danger', style: { marginLeft: '6px' }, text: 'Delete',
                        onclick: () => PG.UI.confirm('Delete room', `Delete room ${r.roomNumber}? Occupied beds will block deletion.`,
                            async () => {
                                await PG.api.del('/api/owner/rooms/' + r.id);
                                PG.UI.toast('Room deleted', 'success');
                                await load();
                            }, 'danger') }));
                    tbody.appendChild(tr);
                });
                t.appendChild(tbody);
                tw.appendChild(t);
                card.appendChild(tw);
                wrap.appendChild(card);
            });
            return wrap;
        }

        function renderSharings(b) {
            const wrap = PG.el('div');
            const cfgs = b.sharingConfigs || [];
            if (!cfgs.length) {
                wrap.appendChild(PG.Shared.emptyEl('No sharing configurations'));
                return wrap;
            }
            const grid = PG.el('div', { class: 'grid grid-3' });
            cfgs.forEach(c => {
                const card = PG.el('div', { class: 'card' });
                card.innerHTML = `
                    <h3 style="margin:0 0 8px">${PG.sharingLabel(c.sharingType)}</h3>
                    <div class="muted" style="font-size:12.5px">${c.numRooms} rooms · ${c.bedsPerRoom} beds per room</div>
                    <div class="divider"></div>
                    <div class="row between"><span class="muted">Monthly rent</span><b>${PG.fmtMoney(c.monthlyRent)}</b></div>
                    <div class="row between mt-8"><span class="muted">Deposit</span><b>${PG.fmtMoney(c.depositAmount)}</b></div>
                    <div class="row between mt-8"><span class="muted">Beds</span><b>${c.availableBeds || 0}<span class="soft">/${c.totalBeds || 0}</span> available</b></div>
                `;
                grid.appendChild(card);
            });
            wrap.appendChild(grid);
            return wrap;
        }

        function editRoom(r) {
            const body = PG.el('div');
            body.innerHTML = `
                <form id="room-form">
                    <div class="field-row-2">
                        <div class="field"><label class="label">Monthly Rent</label><input class="input" name="monthlyRent" type="number" step="1" min="0" required value="${r.monthlyRent}"/></div>
                        <div class="field"><label class="label">Deposit</label><input class="input" name="depositAmount" type="number" step="1" min="0" required value="${r.depositAmount}"/></div>
                    </div>
                    <div class="field"><label class="label">Floor</label><input class="input" name="floorNumber" type="number" min="1" value="${r.floorNumber}"/></div>
                </form>
            `;
            PG.UI.modal({
                title: 'Edit Room ' + r.roomNumber,
                bodyEl: body,
                actions: [
                    { label: 'Cancel' },
                    { label: 'Save', kind: 'primary', onClick: async ({ close }) => {
                        const form = PG.q('#room-form', body);
                        const data = PG.UI.serializeForm(form);
                        await PG.api.put('/api/owner/rooms/' + r.id, data);
                        PG.UI.toast('Room updated', 'success');
                        close();
                        await load();
                    }}
                ]
            });
        }

        function editBuilding(b) {
            const body = PG.el('div');
            body.innerHTML = renderBuildingForm({ basic: b, withReceptionist: false, withSharings: false });
            PG.UI.modal({
                title: 'Edit Building',
                bodyEl: body, large: true,
                actions: [
                    { label: 'Cancel' },
                    { label: 'Save', kind: 'primary', onClick: async ({ close }) => {
                        const data = collectBuildingForm(body);
                        await PG.api.put('/api/owner/buildings/' + b.id, data);
                        PG.UI.toast('Building updated', 'success');
                        close();
                        await load();
                    }}
                ]
            });
        }

        try { await load(); } catch (ex) {
            view.innerHTML = `<div class="empty">${PG.escape(ex.message || 'Failed to load')}</div>`;
        }
        poll = PG.Shared.poller(load, 12000);
        return () => poll && poll.stop();
    });

    /* ---------- New Building (multi-step) ---------- */
    PG.Router.add('/owner/buildings/new', ['OWNER'], async function () {
        const view = PG.UI.renderShell({ nav: NAV, title: 'Add Building', subtitle: 'Create a new PG with rooms, beds and a receptionist' });
        if (!view) return;
        view.innerHTML = '';

        const state = {
            step: 0,
            basic: { totalFloors: 2, amenities: [], imagePaths: [] },
            receptionist: {},
            sharings: { ONE: null, TWO: null, THREE: null }
        };

        const steps = [
            { l: 'Building Info', icon: 'building' },
            { l: 'Receptionist', icon: 'user' },
            { l: 'Sharing & Rooms', icon: 'bed' },
            { l: 'Review', icon: 'check' }
        ];

        const stepperEl = PG.el('div', { class: 'stepper' });
        const card = PG.el('div', { class: 'card' });
        view.appendChild(stepperEl);
        view.appendChild(card);

        function renderStepper() {
            stepperEl.innerHTML = '';
            steps.forEach((s, i) => {
                const d = PG.el('div', { class: 'step' + (i === state.step ? ' active' : (i < state.step ? ' done' : '')) });
                d.innerHTML = `<span class="n">${i + 1}</span><span>${PG.escape(s.l)}</span>`;
                stepperEl.appendChild(d);
            });
        }

        function nav() {
            const r = PG.el('div', { class: 'row between mt-16' });
            const back = PG.el('button', { class: 'btn', text: '← Back', disabled: state.step === 0,
                onclick: () => { state.step--; renderStep(); } });
            const isLast = state.step === steps.length - 1;
            const next = PG.el('button', { class: 'btn primary', text: isLast ? 'Create Building' : 'Next →',
                onclick: async () => {
                    if (!validate()) return;
                    if (isLast) {
                        try {
                            const payload = buildPayload();
                            const res = await PG.api.post('/api/owner/buildings', payload);
                            PG.UI.toast('Building created', 'success');
                            location.hash = '#/owner/buildings/' + res.id;
                        } catch (ex) {
                            PG.UI.toast(ex.message || 'Failed to create building', 'error');
                        }
                    } else {
                        state.step++;
                        renderStep();
                    }
                } });
            r.appendChild(back);
            r.appendChild(next);
            return r;
        }

        function renderStep() {
            renderStepper();
            card.innerHTML = '';
            if (state.step === 0) renderBasic();
            else if (state.step === 1) renderReceptionist();
            else if (state.step === 2) renderSharings();
            else renderReview();
            card.appendChild(nav());
            window.scrollTo(0, 0);
        }

        function renderBasic() {
            const wrap = PG.el('div');
            wrap.innerHTML = renderBuildingForm({ basic: state.basic, withReceptionist: false, withSharings: false });
            card.appendChild(wrap);
        }

        function renderReceptionist() {
            const wrap = PG.el('div');
            wrap.innerHTML = `
                <h2 class="mb-8">Receptionist Account</h2>
                <p class="muted mb-16">This person will manage bookings, allocations, and payments for this building. They will get a login.</p>
                <form id="rec-form">
                    <div class="field-row-2">
                        <div class="field"><label class="label">Full name</label><input class="input" name="fullName" required value="${PG.escape(state.receptionist.fullName || '')}"></div>
                        <div class="field"><label class="label">Phone</label><input class="input" name="phone" required value="${PG.escape(state.receptionist.phone || '')}"></div>
                    </div>
                    <div class="field"><label class="label">Email (login)</label><input class="input" type="email" name="email" required value="${PG.escape(state.receptionist.email || '')}"></div>
                    <div class="field"><label class="label">Password</label><input class="input" type="password" name="password" minlength="6" required value="${PG.escape(state.receptionist.password || '')}"></div>
                </form>
            `;
            card.appendChild(wrap);
        }

        function renderSharings() {
            const wrap = PG.el('div');
            wrap.innerHTML = `
                <h2 class="mb-8">Sharing Types & Auto-generated Rooms</h2>
                <p class="muted mb-16">Configure one or more sharing types. We will auto-generate the rooms and beds.</p>
            `;
            ['ONE','TWO','THREE'].forEach(st => {
                const enabled = state.sharings[st] !== null;
                const cfg = state.sharings[st] || { numRooms: '', bedsPerRoom: st === 'ONE' ? 1 : (st === 'TWO' ? 2 : 3), monthlyRent: '', depositAmount: '' };
                const box = PG.el('div', { class: 'card mb-12', style: { background: 'var(--surface-2)' } });
                box.innerHTML = `
                    <label class="checkbox"><input type="checkbox" data-share="${st}" ${enabled ? 'checked' : ''}><b>${PG.sharingLabel(st)}</b></label>
                    <div class="grid grid-4 mt-12" data-content="${st}" style="${enabled ? '' : 'display:none'}">
                        <div class="field"><label class="label"># Rooms</label><input class="input" type="number" min="1" data-f="numRooms" value="${cfg.numRooms}"></div>
                        <div class="field"><label class="label">Beds / Room</label><input class="input" type="number" min="1" data-f="bedsPerRoom" value="${cfg.bedsPerRoom}"></div>
                        <div class="field"><label class="label">Monthly Rent</label><input class="input" type="number" min="0" data-f="monthlyRent" value="${cfg.monthlyRent}"></div>
                        <div class="field"><label class="label">Deposit</label><input class="input" type="number" min="0" data-f="depositAmount" value="${cfg.depositAmount}"></div>
                    </div>
                `;
                wrap.appendChild(box);
                const cb = PG.q(`[data-share="${st}"]`, box);
                const content = PG.q(`[data-content="${st}"]`, box);
                cb.addEventListener('change', () => {
                    content.style.display = cb.checked ? '' : 'none';
                    if (!cb.checked) state.sharings[st] = null;
                    else state.sharings[st] = state.sharings[st] || { numRooms: 1, bedsPerRoom: st === 'ONE' ? 1 : (st === 'TWO' ? 2 : 3), monthlyRent: 0, depositAmount: 0 };
                });
            });
            card.appendChild(wrap);
        }

        function renderReview() {
            const sharingHtml = ['ONE','TWO','THREE'].filter(k => state.sharings[k]).map(k => {
                const c = state.sharings[k];
                return `<li><b>${PG.sharingLabel(k)}</b>: ${c.numRooms} rooms × ${c.bedsPerRoom} beds — ${PG.fmtMoney(c.monthlyRent)}/mo, deposit ${PG.fmtMoney(c.depositAmount)}</li>`;
            }).join('') || '<li class="soft">No sharing configured</li>';
            const wrap = PG.el('div');
            wrap.innerHTML = `
                <h2 class="mb-8">Review & Create</h2>
                <p class="muted mb-16">Everything below will be created in one step.</p>
                <div class="card mb-12"><b>Building</b><div class="muted">${PG.escape(state.basic.name || '')} · ${PG.escape(state.basic.area || '')}, ${PG.escape(state.basic.city || '')}</div></div>
                <div class="card mb-12"><b>Receptionist</b><div class="muted">${PG.escape(state.receptionist.fullName || '')} · ${PG.escape(state.receptionist.email || '')}</div></div>
                <div class="card"><b>Sharing & Pricing</b><ul style="margin:8px 0 0 18px;padding:0">${sharingHtml}</ul></div>
            `;
            card.appendChild(wrap);
        }

        function validate() {
            if (state.step === 0) {
                const form = PG.q('#bf-form');
                if (!form) return true;
                if (!form.checkValidity()) { form.reportValidity(); return false; }
                const data = collectBuildingForm(card);
                Object.assign(state.basic, data);
                return true;
            }
            if (state.step === 1) {
                const form = PG.q('#rec-form');
                if (!form.checkValidity()) { form.reportValidity(); return false; }
                Object.assign(state.receptionist, PG.UI.serializeForm(form));
                return true;
            }
            if (state.step === 2) {
                ['ONE','TWO','THREE'].forEach(st => {
                    const content = PG.q(`[data-content="${st}"]`);
                    if (!content || content.style.display === 'none') { state.sharings[st] = null; return; }
                    const cfg = {};
                    PG.qa('[data-f]', content).forEach(el => cfg[el.dataset.f] = Number(el.value));
                    state.sharings[st] = cfg;
                });
                const enabled = Object.values(state.sharings).filter(Boolean);
                if (!enabled.length) {
                    PG.UI.toast('Configure at least one sharing type', 'warning');
                    return false;
                }
                for (const c of enabled) {
                    if (!c.numRooms || c.numRooms < 1) { PG.UI.toast('Number of rooms must be at least 1', 'warning'); return false; }
                    if (!c.bedsPerRoom || c.bedsPerRoom < 1) { PG.UI.toast('Beds per room must be at least 1', 'warning'); return false; }
                    if (c.monthlyRent == null || c.monthlyRent < 0) { PG.UI.toast('Monthly rent is required', 'warning'); return false; }
                    if (c.depositAmount == null || c.depositAmount < 0) { PG.UI.toast('Deposit amount is required', 'warning'); return false; }
                }
                return true;
            }
            return true;
        }

        function buildPayload() {
            const sharingConfigs = Object.entries(state.sharings)
                .filter(([_, v]) => v)
                .map(([k, v]) => ({
                    sharingType: k,
                    numRooms: Number(v.numRooms),
                    bedsPerRoom: Number(v.bedsPerRoom),
                    monthlyRent: Number(v.monthlyRent),
                    depositAmount: Number(v.depositAmount)
                }));
            return {
                ...state.basic,
                receptionist: state.receptionist,
                sharingConfigs
            };
        }

        renderStep();
    });

    /* ---------- shared form ---------- */
    function renderBuildingForm({ basic, withReceptionist, withSharings }) {
        basic = basic || {};
        const amenities = (basic.amenities || []).join(', ');
        const images = (basic.imagePaths || []).map(p => `<img class="t" data-img="${PG.escape(p)}" src="${PG.escape(p)}" />`).join('');
        return `
            <form id="bf-form">
                <div class="field"><label class="label">Building name</label><input class="input" name="name" required value="${PG.escape(basic.name || '')}"></div>
                <div class="field"><label class="label">Address</label><input class="input" name="address" required value="${PG.escape(basic.address || '')}"></div>
                <div class="field-row-2">
                    <div class="field"><label class="label">Area / Locality</label><input class="input" name="area" required value="${PG.escape(basic.area || '')}"></div>
                    <div class="field"><label class="label">City</label><input class="input" name="city" required value="${PG.escape(basic.city || '')}"></div>
                </div>
                <div class="field"><label class="label">Description</label><textarea class="textarea" name="description">${PG.escape(basic.description || '')}</textarea></div>
                <div class="field"><label class="label">Amenities (comma separated)</label><input class="input" id="amenities-input" value="${PG.escape(amenities)}" placeholder="WiFi, AC, Hot Water, Laundry, Food"></div>
                <div class="field-row-3">
                    <div class="field"><label class="label">Total floors</label><input class="input" type="number" min="1" max="50" name="totalFloors" required value="${basic.totalFloors || 2}"></div>
                    <div class="field"><label class="label">Contact phone</label><input class="input" name="contactPhone" value="${PG.escape(basic.contactPhone || '')}"></div>
                    <div class="field"><label class="label">Contact email</label><input class="input" type="email" name="contactEmail" value="${PG.escape(basic.contactEmail || '')}"></div>
                </div>
                <div class="field">
                    <label class="label">Images</label>
                    <input class="input" type="file" id="img-input" accept="image/*" multiple />
                    <div class="field-hint">Upload one or more images of the building.</div>
                    <div id="img-thumbs" class="thumbs mt-12">${images}</div>
                </div>
            </form>
        `;
    }

    function collectBuildingForm(root) {
        const form = PG.q('#bf-form', root);
        const data = PG.UI.serializeForm(form);
        data.amenities = (PG.q('#amenities-input', root).value || '').split(',').map(s => s.trim()).filter(Boolean);
        data.imagePaths = PG.qa('#img-thumbs [data-img]', root).map(el => el.dataset.img);
        data.totalFloors = Number(data.totalFloors);
        return data;
    }

    document.addEventListener('change', async function (e) {
        if (e.target && e.target.id === 'img-input') {
            const files = Array.from(e.target.files || []);
            const thumbs = PG.q('#img-thumbs');
            if (!thumbs) return;
            for (const f of files) {
                try {
                    const r = await PG.api.upload(f, 'building');
                    const img = PG.el('img', { class: 't', src: r.path });
                    img.dataset.img = r.path;
                    img.title = 'Click to remove';
                    img.addEventListener('click', () => img.remove());
                    thumbs.appendChild(img);
                } catch (ex) {
                    PG.UI.toast('Upload failed: ' + (ex.message || ''), 'error');
                }
            }
            e.target.value = '';
        }
    });
})(window.PG);
