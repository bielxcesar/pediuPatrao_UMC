(() => {
    const chave = 'pediu-patrao-tema';
    const temas = ['light', 'dark'];
    let tema = 'light';

    try {
        const preferencia = localStorage.getItem(chave);
        if (temas.includes(preferencia)) tema = preferencia;
    } catch {
        // A interface continua disponível quando o navegador bloqueia o armazenamento.
    }

    document.documentElement.dataset.bsTheme = tema;
    document.addEventListener('DOMContentLoaded', () => {
        const seletor = document.querySelector('#preferencia-tema');
        if (!seletor) return;

        seletor.value = tema;
        seletor.addEventListener('change', () => {
            if (!temas.includes(seletor.value)) return;
            document.documentElement.dataset.bsTheme = seletor.value;
            try {
                localStorage.setItem(chave, seletor.value);
            } catch {
                document.querySelector('#preferencia-tema-status').textContent =
                    'Tema aplicado. O navegador não permitiu salvar a preferência.';
            }
        });
    });
})();
