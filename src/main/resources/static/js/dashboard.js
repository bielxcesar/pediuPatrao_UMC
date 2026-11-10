(() => {
    const painel = document.querySelector('#dashboard');
    const campo = (id) => painel.querySelector('#dashboard-' + id);
    const form = campo('filtros');
    const status = campo('status');
    const dados = campo('dados');
    const inicio = campo('inicio');
    const fim = campo('fim');
    const atualizar = campo('atualizar');
    const consultar = form.querySelector('button[type="submit"]');
    const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
    const numero = new Intl.NumberFormat('pt-BR');
    const rotulos = {
        RECEBIDO: 'Recebido', PREPARANDO: 'Em preparo', PRONTO: 'Pronto',
        A_CAMINHO: 'A caminho', FINALIZADO: 'Finalizado', CANCELADO: 'Cancelado'
    };
    let grafico;

    function dataBr(data) {
        return data.split('-').reverse().join('/');
    }

    function preencherTabela(tabela, linhas) {
        tabela.replaceChildren();
        for (const [nome, valor] of linhas) {
            const linha = tabela.insertRow();
            linha.insertCell().textContent = nome;
            const celula = linha.insertCell();
            celula.textContent = valor;
            if (tabela.id === 'dashboard-ranking') celula.className = 'text-end';
        }
    }

    function renderizar(resumo) {
        campo('faturamento').textContent = moeda.format(resumo.faturamento);
        campo('ticket').textContent = moeda.format(resumo.ticketMedio);
        campo('total').textContent = numero.format(resumo.totalPedidos);
        campo('finalizados').textContent = numero.format(resumo.pedidosFinalizados);

        const situacao = campo('situacao');
        situacao.replaceChildren();
        const contagens = Object.entries(resumo.situacao).map(([chave, valor]) => [rotulos[chave] ?? chave, valor]);
        if (resumo.pedidosSemStatus) contagens.push(['Sem status', resumo.pedidosSemStatus]);
        for (const [nome, quantidade] of contagens) {
            const linha = document.createElement('div');
            linha.className = 'd-flex justify-content-between gap-2 border-bottom py-2';
            const rotulo = document.createElement('dt');
            rotulo.textContent = nome;
            const valor = document.createElement('dd');
            valor.className = 'mb-0';
            valor.textContent = numero.format(quantidade);
            linha.append(rotulo, valor);
            situacao.append(linha);
        }

        const ranking = resumo.produtosMaisVendidos.map(({ nome, quantidade }) => [nome, numero.format(quantidade)]);
        preencherTabela(campo('ranking'), ranking.length ? ranking : [['Nenhuma venda registrada', '—']]);
        preencherTabela(campo('vendas-tabela'), resumo.vendasDiarias.map(({ data, faturamento }) =>
            [dataBr(data), moeda.format(faturamento)]));

        if (grafico) grafico.destroy();
        const canvas = campo('vendas');
        canvas.parentElement.hidden = typeof Chart !== 'function';
        if (canvas.parentElement.hidden) return;

        grafico = new Chart(canvas, {
            type: 'line',
            data: {
                labels: resumo.vendasDiarias.map(({ data }) => dataBr(data)),
                datasets: [{
                    label: 'Faturamento',
                    data: resumo.vendasDiarias.map(({ faturamento }) => faturamento),
                    borderColor: '#0d6efd',
                    backgroundColor: 'rgba(13, 110, 253, 0.12)',
                    pointRadius: resumo.vendasDiarias.length <= 31 ? 3 : 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: { yAxes: [{ ticks: { beginAtZero: true, callback: (valor) => moeda.format(valor) } }] },
                tooltips: { callbacks: { label: (ponto) => moeda.format(ponto.yLabel) } }
            }
        });
    }

    async function carregar() {
        consultar.disabled = true;
        atualizar.disabled = true;
        dados.hidden = true;
        status.className = 'text-muted';
        status.textContent = 'Carregando dados…';
        try {
            const url = new URL(painel.dataset.api, document.baseURI);
            if (inicio.value) url.searchParams.set('inicio', inicio.value);
            if (fim.value) url.searchParams.set('fim', fim.value);
            const resposta = await fetch(url, { headers: { Accept: 'application/json' } });
            if (resposta.redirected || resposta.status === 401) {
                throw new Error('Sua sessão expirou. Entre novamente para consultar o dashboard.');
            }
            if (resposta.status === 403) throw new Error('Seu perfil não tem acesso ao dashboard.');
            if (resposta.status === 400) {
                const problema = await resposta.json();
                throw new Error(problema.detail || 'Confira as datas do período.');
            }
            if (!resposta.ok) throw new Error();
            const resumo = await resposta.json();
            inicio.value = resumo.inicio;
            fim.value = resumo.fim;
            dados.hidden = false;
            renderizar(resumo);
            const periodo = dataBr(resumo.inicio) + ' a ' + dataBr(resumo.fim);
            status.textContent = resumo.totalPedidos
                ? 'Período consultado: ' + periodo
                : 'Nenhum pedido no período de ' + periodo + '.';
        } catch (erro) {
            dados.hidden = true;
            status.className = 'text-danger';
            status.textContent = erro instanceof TypeError || erro instanceof SyntaxError
                ? 'Não foi possível carregar os dados. Tente atualizar.'
                : erro.message || 'Não foi possível carregar os dados. Tente atualizar.';
        } finally {
            consultar.disabled = false;
            atualizar.disabled = false;
        }
    }

    form.addEventListener('submit', (evento) => {
        evento.preventDefault();
        carregar();
    });
    atualizar.addEventListener('click', () => {
        if ((!inicio.value && !fim.value) || form.reportValidity()) carregar();
    });
    carregar();
})();
