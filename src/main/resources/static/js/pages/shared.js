(function (PG) {
    PG.Shared = {};

    PG.Shared.kpiCard = function (label, value, sub, kind, icon) {
        return `
            <div class="kpi ${kind || ''}">
                <div class="ico">${PG.icon(icon || 'chart')}</div>
                <div class="label">${PG.escape(label)}</div>
                <div class="value">${PG.escape(String(value))}</div>
                ${sub ? `<div class="sub">${PG.escape(sub)}</div>` : ''}
            </div>
        `;
    };

    PG.Shared.emptyEl = function (msg, sub) {
        return PG.el('div', { class: 'empty', html: `<div style="font-weight:600;color:var(--text-muted)">${PG.escape(msg)}</div>${sub ? `<div class="mt-8 soft">${PG.escape(sub)}</div>` : ''}` });
    };

    PG.Shared.printReceipt = async function (receiptNumber) {
        try {
            const r = await PG.api.get('/api/receipts/' + encodeURIComponent(receiptNumber));
            const html = `
                <div class="receipt print-area">
                    <h2>Payment Receipt</h2>
                    <div class="muted">Receipt # ${PG.escape(r.receiptNumber)}</div>
                    <div class="meta">
                        <div>
                            <div class="l">Tenant</div>
                            <div><b>${PG.escape(r.tenantName || '')}</b></div>
                            <div>${PG.escape(r.tenantEmail || '')}</div>
                            <div>${PG.escape(r.tenantPhone || '')}</div>
                        </div>
                        <div>
                            <div class="l">PG</div>
                            <div><b>${PG.escape(r.buildingName || '')}</b></div>
                            <div>${PG.escape(r.buildingAddress || '')}</div>
                            <div>Room ${PG.escape(r.roomNumber || '-')} · Bed ${PG.escape(r.bedNumber || '-')}</div>
                        </div>
                    </div>
                    <div class="meta mt-16">
                        <div><div class="l">Month</div><b>${PG.escape(PG.fmtMonth(r.monthYear))}</b></div>
                        <div><div class="l">Payment Method</div><b>${PG.escape(r.paymentMethod || '-')}</b></div>
                        <div><div class="l">Paid At</div><b>${PG.escape(PG.fmtDateTime(r.paidAt))}</b></div>
                        <div><div class="l">Collected by</div><b>${PG.escape(r.collectedByName || '-')}</b></div>
                    </div>
                    <div class="total">Total Paid: ${PG.fmtMoney(r.amount)}</div>
                </div>
            `;
            PG.UI.modal({
                title: 'Payment Receipt',
                bodyHtml: html,
                large: true,
                actions: [
                    { label: 'Print', kind: 'primary', onClick: () => { window.print(); } },
                    { label: 'Close' }
                ]
            });
        } catch (ex) {
            PG.UI.toast(ex.message || 'Failed to load receipt', 'error');
        }
    };

    PG.Shared.tablePager = function ({ container, fetchPage, columns, emptyMessage, rowKey, search }) {
        const state = { page: 0, size: 10, q: '' };
        const wrap = PG.el('div');
        const body = PG.el('div');
        wrap.appendChild(body);
        container.appendChild(wrap);

        async function refresh() {
            body.innerHTML = '<div class="empty muted">Loading…</div>';
            try {
                const data = await fetchPage(state);
                renderTable(data);
            } catch (ex) {
                body.innerHTML = `<div class="empty">${PG.escape(ex.message || 'Failed to load')}</div>`;
            }
        }

        function renderTable(data) {
            body.innerHTML = '';
            const items = data && data.content ? data.content : (Array.isArray(data) ? data : []);
            const total = data && typeof data.totalElements === 'number' ? data.totalElements : items.length;

            if (!items.length) {
                body.appendChild(PG.Shared.emptyEl(emptyMessage || 'No records found'));
                if (data && typeof data.totalElements === 'number') {
                    body.appendChild(PG.UI.pager(state, total, refresh));
                }
                return;
            }
            const tw = PG.el('div', { class: 'table-wrap' });
            const tbl = PG.el('table', { class: 'table' });
            const thead = PG.el('thead');
            const trh = PG.el('tr');
            columns.forEach(c => trh.appendChild(PG.el('th', { text: c.label, style: c.right ? { textAlign: 'right' } : {} })));
            thead.appendChild(trh);
            tbl.appendChild(thead);
            const tbody = PG.el('tbody');
            items.forEach((row, i) => {
                const tr = PG.el('tr');
                if (rowKey) tr.dataset.key = row[rowKey];
                columns.forEach(c => {
                    const td = PG.el('td');
                    if (c.right) td.style.textAlign = 'right';
                    if (typeof c.render === 'function') {
                        const out = c.render(row, i, refresh);
                        if (out instanceof Node) td.appendChild(out);
                        else if (out != null) td.innerHTML = String(out);
                    } else {
                        td.textContent = row[c.key] == null ? '' : String(row[c.key]);
                    }
                    tr.appendChild(td);
                });
                tbody.appendChild(tr);
            });
            tbl.appendChild(tbody);
            tw.appendChild(tbl);
            body.appendChild(tw);
            if (data && typeof data.totalElements === 'number') {
                body.appendChild(PG.UI.pager(state, total, refresh));
            }
        }

        if (search) {
            const toolbar = PG.el('div', { class: 'toolbar' });
            const searchBox = PG.el('input', {
                class: 'input search', type: 'search', placeholder: search.placeholder || 'Search…',
                oninput: PG.debounce((e) => { state.q = e.target.value; state.page = 0; refresh(); }, 320)
            });
            toolbar.appendChild(searchBox);
            if (search.extra) toolbar.appendChild(search.extra);
            container.insertBefore(toolbar, wrap);
        }

        refresh();
        return { refresh, state };
    };

    PG.Shared.poller = function (callback, ms) {
        let id = null;
        function start() {
            stop();
            id = setInterval(() => { try { callback(); } catch (_) {} }, ms || 8000);
        }
        function stop() { if (id) { clearInterval(id); id = null; } }
        start();
        return { stop, restart: start };
    };
})(window.PG);
