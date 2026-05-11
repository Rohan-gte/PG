(function (PG) {
    function render() {
        const app = PG.q('#app');
        app.innerHTML = '';

        let role = 'TENANT';

        const wrap = PG.el('div', { class: 'auth-page' });
        const hero = PG.el('div', { class: 'auth-hero' });
        hero.innerHTML = `
            <div class="auth-brand"><span class="mark">PG</span> PG Manager</div>
            <div>
                <h1>Join the PG Manager community.</h1>
                <p>Whether you're looking for a comfortable PG to stay in, or you run a PG and want to digitise operations — get started in under a minute.</p>
            </div>
            <div style="font-size:12px;color:rgba(255,255,255,0.7)">© ${new Date().getFullYear()} PG Manager</div>
        `;
        wrap.appendChild(hero);

        const formWrap = PG.el('div', { class: 'auth-form-wrap' });
        const card = PG.el('div', { class: 'auth-card' });
        card.innerHTML = `
            <h2>Create account</h2>
            <p class="lead">Pick a role to get started.</p>
            <div class="role-tabs">
                <button type="button" data-role="TENANT" class="active">I'm a Tenant</button>
                <button type="button" data-role="OWNER">I'm a PG Owner</button>
            </div>
            <div id="alert"></div>
            <form id="reg-form"></form>
            <div class="switch">Already have an account? <a href="#/login">Sign in</a></div>
        `;
        formWrap.appendChild(card);
        wrap.appendChild(formWrap);
        app.appendChild(wrap);

        const tabs = PG.qa('.role-tabs button', card);
        tabs.forEach(b => b.addEventListener('click', () => {
            tabs.forEach(x => x.classList.remove('active'));
            b.classList.add('active');
            role = b.getAttribute('data-role');
            renderForm();
        }));
        renderForm();

        function renderForm() {
            const form = PG.q('#reg-form', card);
            const alertBox = PG.q('#alert', card);
            alertBox.innerHTML = '';
            form.innerHTML = role === 'TENANT' ? `
                <div class="field-row-2">
                    <div class="field"><label class="label">Full name</label><input class="input" name="fullName" required></div>
                    <div class="field"><label class="label">Phone</label><input class="input" name="phone" required></div>
                </div>
                <div class="field"><label class="label">Email</label><input class="input" type="email" name="email" required></div>
                <div class="field-row-2">
                    <div class="field"><label class="label">Password</label><input class="input" type="password" name="password" minlength="6" required></div>
                    <div class="field"><label class="label">Gender</label>
                        <select class="select" name="gender" required>
                            <option value="">Select…</option>
                            <option value="MALE">Male</option>
                            <option value="FEMALE">Female</option>
                            <option value="OTHER">Other</option>
                        </select>
                    </div>
                </div>
                <div class="field">
                    <label class="label">ID Proof (Aadhaar / PAN / Driving Licence)</label>
                    <input class="input" type="file" id="id-proof" accept="image/*,.pdf" />
                    <div class="field-hint">Optional but recommended. Max 10 MB.</div>
                </div>
                <button class="btn primary lg block" type="submit" id="reg-btn">Create tenant account</button>
            ` : `
                <div class="field-row-2">
                    <div class="field"><label class="label">Full name</label><input class="input" name="fullName" required></div>
                    <div class="field"><label class="label">Phone</label><input class="input" name="phone" required></div>
                </div>
                <div class="field"><label class="label">Email</label><input class="input" type="email" name="email" required></div>
                <div class="field"><label class="label">Password</label><input class="input" type="password" name="password" minlength="6" required></div>
                <div class="field"><label class="label">Address</label><textarea class="textarea" name="address" required></textarea></div>
                <button class="btn primary lg block" type="submit" id="reg-btn">Register as PG Owner</button>
                <div class="field-hint mt-12">Owner accounts require admin approval before you can log in.</div>
            `;

            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                alertBox.innerHTML = '';
                const data = PG.UI.serializeForm(form);
                const btn = PG.q('#reg-btn', form);
                btn.disabled = true; btn.textContent = 'Submitting…';
                try {
                    if (role === 'TENANT') {
                        const idEl = PG.q('#id-proof', form);
                        if (idEl && idEl.files && idEl.files[0]) {
                            try {
                                const r = await PG.api.publicUpload(idEl.files[0]);
                                data.idProofPath = r.path;
                            } catch (ex) {
                                throw new Error('ID upload failed: ' + ex.message);
                            }
                        }
                        await PG.Auth.registerTenant(data);
                        alertBox.innerHTML = `<div class="alert success">Account created. You can sign in now.</div>`;
                        setTimeout(() => location.hash = '#/login', 900);
                    } else {
                        await PG.Auth.registerOwner(data);
                        alertBox.innerHTML = `<div class="alert info">Registration submitted. An administrator will approve your account before you can sign in.</div>`;
                        setTimeout(() => location.hash = '#/login', 1500);
                    }
                } catch (ex) {
                    alertBox.innerHTML = `<div class="alert error">${PG.escape(ex.message || 'Registration failed')}</div>`;
                } finally {
                    btn.disabled = false; btn.textContent = role === 'TENANT' ? 'Create tenant account' : 'Register as PG Owner';
                }
            }, { once: false });
        }
    }

    PG.Router.add('/register', [], render);
})(window.PG);
