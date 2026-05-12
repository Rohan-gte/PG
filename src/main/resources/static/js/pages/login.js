(function (PG) {
    function render() {
        const app = PG.q('#app');
        app.innerHTML = '';
        const wrap = PG.el('div', { class: 'auth-page' });

        const hero = PG.el('div', { class: 'auth-hero' });
        hero.innerHTML = `
            <div class="auth-brand"><span class="mark">PG</span> PG Manager</div>
            <div>
                <h1>Run your PG like a premium business.</h1>
                <p>End-to-end management for paying guest accommodations — buildings, rooms, beds, tenants, bookings, and payments — all in one place.</p>
                <div class="auth-stats">
                    <div class="auth-stat"><div class="v">24/7</div><div class="l">Tenant Portal</div></div>
                    <div class="auth-stat"><div class="v">Live</div><div class="l">Bed Availability</div></div>
                    <div class="auth-stat"><div class="v">Auto</div><div class="l">Rent Tracking</div></div>
                </div>
            </div>
            <div style="font-size:12px;color:rgba(255,255,255,0.7)">© ${new Date().getFullYear()} PG Manager</div>
        `;
        wrap.appendChild(hero);

        const formWrap = PG.el('div', { class: 'auth-form-wrap' });
        const card = PG.el('div', { class: 'auth-card' });
        card.innerHTML = `
            <h2>Welcome back</h2>
            <p class="lead">Sign in to access your dashboard.</p>
            <div id="alert"></div>
            <form id="login-form">
                <div class="field">
                    <label class="label">Email</label>
                    <input class="input" name="email" type="email" required autofocus />
                </div>
                <div class="field">
                    <label class="label">Password</label>
                    <input class="input" name="password" type="password" required />
                </div>
                <button class="btn primary lg block" type="submit" id="login-btn">Sign in</button>
            </form>
            <div class="switch"><a href="#/home" class="auth-back-home">← Back to home</a> · New here? <a href="#/register">Create an account</a></div>
            <div class="divider"></div>
            <div class="muted" style="font-size:12px;text-align:center">
                Default admin: <b>admin@pg.local</b> / <b>Admin@123</b>
            </div>
        `;
        formWrap.appendChild(card);
        wrap.appendChild(formWrap);
        app.appendChild(wrap);

        const form = PG.q('#login-form');
        const alertBox = PG.q('#alert');
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            alertBox.innerHTML = '';
            const data = PG.UI.serializeForm(form);
            const btn = PG.q('#login-btn');
            btn.disabled = true; btn.textContent = 'Signing in…';
            try {
                const r = await PG.Auth.login(data.email, data.password);
                const name = (r.user && r.user.fullName) ? r.user.fullName : 'there';
                PG.UI.successPopup('Signed in successfully', 'Welcome back, ' + name + '.', 1300, function () {
                    location.hash = PG.Auth.roleHome(r.user.role);
                });
            } catch (ex) {
                alertBox.innerHTML = `<div class="alert error">${PG.escape(ex.message || 'Login failed')}</div>`;
            } finally {
                btn.disabled = false; btn.textContent = 'Sign in';
            }
        });
    }

    PG.Router.add('/login', [], render);
})(window.PG);
