const rotulosPrioridadeDashboard = {
    BAIXA: 'Baixa',
    MEDIA: 'Média',
    ALTA: 'Alta'
};

const rotulosStatusTarefaDashboard = {
    PENDENTE: 'Pendente',
    EM_ANDAMENTO: 'Em andamento',
    AGUARDANDO_CLIENTE: 'Aguardando cliente',
    CONCLUIDA: 'Concluída',
    ATRASADA: 'Atrasada'
};

const rotulosCanalDashboard = {
    WHATSAPP: 'WhatsApp',
    TELEFONE: 'Telefone',
    PRESENCIAL: 'Presencial',
    EMAIL: 'E-mail'
};

document.addEventListener('DOMContentLoaded', carregarDashboard);

function carregarDashboard() {
    const indicadorCarregamento = document.getElementById('indicadorCarregamento');
    indicadorCarregamento.classList.remove('oculto');

    buscarDashboard()
        .then(function (resumo) {
            preencherCartoes(resumo);
            preencherProximosPrazos(resumo.proximosPrazos || [], resumo.dataReferencia);
            preencherUltimosAtendimentos(resumo.ultimosAtendimentos || []);
        })
        .catch(function (erro) {
            exibirMensagemDashboard(erro.mensagem || 'Não foi possível carregar o dashboard.', 'erro');
        })
        .finally(function () {
            indicadorCarregamento.classList.add('oculto');
        });
}

function preencherCartoes(resumo) {
    document.getElementById('valorClientesAtivos').textContent = resumo.clientesAtivos;
    document.getElementById('valorTarefasPendentes').textContent = resumo.tarefasPendentes;
    document.getElementById('valorTarefasAtrasadas').textContent = resumo.tarefasAtrasadas;
    document.getElementById('valorProcessosEmAndamento').textContent = resumo.processosEmAndamento;
    document.getElementById('valorProcessosAtrasados').textContent = resumo.processosAtrasados;
    document.getElementById('cartaoProcessosAtrasados')
        .classList.toggle('cartao-alerta', resumo.processosAtrasados > 0);

    // O cartão de atrasadas fica vermelho só quando existe alguma tarefa atrasada
    document.getElementById('cartaoTarefasAtrasadas')
        .classList.toggle('cartao-alerta', resumo.tarefasAtrasadas > 0);

    const proximos = resumo.proximosPrazos || [];
    document.getElementById('valorProximoPrazo').textContent =
        proximos.length > 0 ? formatarDataDashboard(proximos[0].prazo) : 'Nenhum';
}

function preencherProximosPrazos(tarefas, dataReferencia) {
    const corpo = document.getElementById('corpoProximosPrazos');
    const tabela = corpo.closest('.area-tabela');
    const vazio = document.getElementById('vazioProximosPrazos');

    corpo.textContent = '';
    tabela.classList.toggle('oculto', tarefas.length === 0);
    vazio.classList.toggle('oculto', tarefas.length > 0);

    tarefas.forEach(function (tarefa) {
        const linha = document.createElement('tr');

        const celulaPrazo = criarCelulaDashboard('Prazo', formatarDataDashboard(tarefa.prazo));
        if (diasEntreDatas(dataReferencia, tarefa.prazo) <= 3) {
            celulaPrazo.classList.add('prazo-proximo');
        }
        linha.appendChild(celulaPrazo);

        const celulaTitulo = criarCelulaDashboard('Tarefa', '');
        const link = document.createElement('a');
        link.href = 'pages/tarefas.html?clienteId=' + encodeURIComponent(tarefa.clienteId);
        link.textContent = tarefa.titulo;
        celulaTitulo.appendChild(link);
        linha.appendChild(celulaTitulo);

        linha.appendChild(criarCelulaDashboard('Cliente', tarefa.nomeCliente));

        const celulaPrioridade = criarCelulaDashboard('Prioridade', '');
        const seloPrioridade = document.createElement('span');
        seloPrioridade.classList.add('selo-prioridade', 'prioridade-' + String(tarefa.prioridade).toLowerCase());
        seloPrioridade.textContent = rotulosPrioridadeDashboard[tarefa.prioridade] || tarefa.prioridade;
        celulaPrioridade.appendChild(seloPrioridade);
        linha.appendChild(celulaPrioridade);

        const celulaStatus = criarCelulaDashboard('Status', '');
        const seloStatus = document.createElement('span');
        seloStatus.classList.add('selo-status-tarefa', 'status-tarefa-' + String(tarefa.status).toLowerCase());
        seloStatus.textContent = rotulosStatusTarefaDashboard[tarefa.status] || tarefa.status;
        celulaStatus.appendChild(seloStatus);
        linha.appendChild(celulaStatus);

        corpo.appendChild(linha);
    });
}

function preencherUltimosAtendimentos(atendimentos) {
    const corpo = document.getElementById('corpoUltimosAtendimentos');
    const tabela = corpo.closest('.area-tabela');
    const vazio = document.getElementById('vazioUltimosAtendimentos');

    corpo.textContent = '';
    tabela.classList.toggle('oculto', atendimentos.length === 0);
    vazio.classList.toggle('oculto', atendimentos.length > 0);

    atendimentos.forEach(function (atendimento) {
        const linha = document.createElement('tr');

        linha.appendChild(criarCelulaDashboard('Data', formatarDataHoraDashboard(atendimento.dataHora)));

        const celulaCliente = criarCelulaDashboard('Cliente', '');
        const link = document.createElement('a');
        link.href = 'pages/atendimentos.html?clienteId=' + encodeURIComponent(atendimento.clienteId);
        link.textContent = atendimento.nomeCliente;
        celulaCliente.appendChild(link);
        linha.appendChild(celulaCliente);

        const celulaCanal = criarCelulaDashboard('Canal', '');
        const seloCanal = document.createElement('span');
        seloCanal.classList.add('selo-canal', 'canal-' + String(atendimento.canal).toLowerCase());
        seloCanal.textContent = rotulosCanalDashboard[atendimento.canal] || atendimento.canal;
        celulaCanal.appendChild(seloCanal);
        linha.appendChild(celulaCanal);

        linha.appendChild(criarCelulaDashboard('Assunto', atendimento.assunto));

        corpo.appendChild(linha);
    });
}

// data-rotulo é o texto que aparece ao lado do valor quando a tabela vira cartão no celular
function criarCelulaDashboard(rotulo, texto) {
    const celula = document.createElement('td');
    celula.setAttribute('data-rotulo', rotulo);
    celula.textContent = texto || '';
    return celula;
}

// Recebe "AAAA-MM-DD" e devolve "DD/MM/AAAA" sem usar new Date(texto),
// que erraria um dia por causa do fuso horário
function formatarDataDashboard(valor) {
    if (!valor) {
        return '';
    }
    const partes = valor.split('-');
    return partes[2] + '/' + partes[1] + '/' + partes[0];
}

// Recebe "AAAA-MM-DDTHH:MM:SS" e devolve "DD/MM/AAAA HH:MM"
function formatarDataHoraDashboard(valor) {
    if (!valor) {
        return '';
    }
    const pedacos = valor.split('T');
    const hora = pedacos[1] ? ' ' + pedacos[1].substring(0, 5) : '';
    return formatarDataDashboard(pedacos[0]) + hora;
}

// Quantos dias vão de uma data "AAAA-MM-DD" até outra
function diasEntreDatas(inicio, fim) {
    if (!inicio || !fim) {
        return Infinity;
    }
    const a = inicio.split('-').map(Number);
    const b = fim.split('-').map(Number);
    const milissegundos = Date.UTC(b[0], b[1] - 1, b[2]) - Date.UTC(a[0], a[1] - 1, a[2]);
    return Math.round(milissegundos / 86400000);
}

function exibirMensagemDashboard(texto, tipo) {
    const container = document.getElementById('containerMensagens');

    const toast = document.createElement('div');
    toast.classList.add('mensagem-toast', tipo || 'sucesso');
    toast.textContent = texto;

    container.appendChild(toast);

    setTimeout(function () {
        toast.remove();
    }, 4000);
}
