(function (PG) {
    function render() {
        const app = PG.q('#app');
        if (!app) return;
        app.innerHTML = '';
        const wrap = PG.el('div', { class: 'home-shell' });
        const iframe = PG.el('iframe', {
            class: 'landing-iframe',
            title: 'StayEase PG — Home',
            src: '/landing.html',
            loading: 'eager'
        });
        wrap.appendChild(iframe);
        app.appendChild(wrap);
    }

    PG.Router.add('/home', [], render);
})(window.PG);
