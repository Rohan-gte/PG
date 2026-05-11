(function (PG) {
    const palette = ['#3858f5', '#7c5cff', '#15a363', '#d97706', '#dc2626', '#0891b2', '#8892a6'];

    PG.Charts = {};

    PG.Charts.donut = function (data, opts) {
        opts = opts || {};
        const total = data.reduce((a, d) => a + (d.value || 0), 0) || 1;
        const size = 180, r = 70, stroke = 22, cx = size / 2, cy = size / 2;
        const circ = 2 * Math.PI * r;
        let offset = 0;
        const segments = data.map((d, i) => {
            const frac = (d.value || 0) / total;
            const len = frac * circ;
            const seg = `<circle cx="${cx}" cy="${cy}" r="${r}" fill="none"
                stroke="${d.color || palette[i % palette.length]}"
                stroke-width="${stroke}" stroke-dasharray="${len} ${circ - len}"
                stroke-dashoffset="${-offset}" transform="rotate(-90 ${cx} ${cy})"/>`;
            offset += len;
            return seg;
        }).join('');
        const centerLabel = opts.centerLabel || '';
        const centerValue = opts.centerValue != null ? String(opts.centerValue) : String(total);
        const svg = `<svg viewBox="0 0 ${size} ${size}" preserveAspectRatio="xMidYMid meet" style="max-width:220px;margin:0 auto">
            <circle cx="${cx}" cy="${cy}" r="${r}" fill="none" stroke="#eef0f6" stroke-width="${stroke}"/>
            ${segments}
            <text x="${cx}" y="${cy - 4}" text-anchor="middle" font-family="${getFont()}" font-size="22" font-weight="700" fill="#0b1020">${PG.escape(centerValue)}</text>
            <text x="${cx}" y="${cy + 16}" text-anchor="middle" font-family="${getFont()}" font-size="11" fill="#8892a6">${PG.escape(centerLabel)}</text>
        </svg>`;
        const legend = `<div class="legend">${data.map((d, i) => `<span><span class="sw" style="background:${d.color || palette[i % palette.length]}"></span>${PG.escape(d.label)} · ${d.value || 0}</span>`).join('')}</div>`;
        return svg + legend;
    };

    PG.Charts.bar = function (data, opts) {
        opts = opts || {};
        const w = 520, h = 200, padL = 36, padR = 12, padT = 16, padB = 30;
        const max = Math.max(1, ...data.map(d => d.value || 0));
        const innerW = w - padL - padR, innerH = h - padT - padB;
        const bw = data.length ? Math.min(48, innerW / data.length - 8) : 0;
        const ySteps = 4;
        let grid = '';
        for (let i = 0; i <= ySteps; i++) {
            const y = padT + (innerH * i) / ySteps;
            const val = Math.round(max - (max * i) / ySteps);
            grid += `<line x1="${padL}" y1="${y}" x2="${w - padR}" y2="${y}" stroke="#eef0f6" stroke-width="1"/>`;
            grid += `<text x="${padL - 6}" y="${y + 4}" text-anchor="end" font-size="10" fill="#8892a6" font-family="${getFont()}">${val}</text>`;
        }
        const bars = data.map((d, i) => {
            const v = d.value || 0;
            const bh = (v / max) * innerH;
            const x = padL + 8 + i * (innerW / data.length);
            const y = padT + innerH - bh;
            const color = d.color || palette[i % palette.length];
            const label = PG.escape(d.label);
            return `
                <rect x="${x}" y="${y}" width="${bw}" height="${bh}" rx="4" fill="${color}"/>
                <text x="${x + bw / 2}" y="${y - 4}" text-anchor="middle" font-size="10" fill="#0b1020" font-family="${getFont()}" font-weight="600">${v}</text>
                <text x="${x + bw / 2}" y="${h - 10}" text-anchor="middle" font-size="11" fill="#5b6478" font-family="${getFont()}">${label}</text>
            `;
        }).join('');
        return `<svg viewBox="0 0 ${w} ${h}" preserveAspectRatio="xMidYMid meet">${grid}${bars}</svg>`;
    };

    PG.Charts.miniLine = function (values, color) {
        if (!values || !values.length) values = [0, 0];
        const w = 220, h = 60, pad = 4;
        const max = Math.max(1, ...values);
        const step = (w - pad * 2) / Math.max(1, values.length - 1);
        const points = values.map((v, i) => {
            const x = pad + i * step;
            const y = h - pad - (v / max) * (h - pad * 2);
            return `${x},${y}`;
        }).join(' ');
        const last = values[values.length - 1] || 0;
        const lx = pad + (values.length - 1) * step;
        const ly = h - pad - (last / max) * (h - pad * 2);
        const c = color || '#3858f5';
        return `<svg viewBox="0 0 ${w} ${h}" preserveAspectRatio="none" style="width:100%;height:60px">
            <polyline points="${points}" fill="none" stroke="${c}" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="${lx}" cy="${ly}" r="3.5" fill="${c}"/>
        </svg>`;
    };

    function getFont() {
        return "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif";
    }
})(window.PG);
