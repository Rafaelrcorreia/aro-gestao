const URL_BASE_CLIENTES = '/api/clientes';
const URL_BASE_ATENDIMENTOS = '/api/atendimentos';
const URL_BASE_TAREFAS = '/api/tarefas';
const URL_BASE_CALENDARIO = '/api/calendario';
const URL_BASE_DASHBOARD = '/api/dashboard';
const URL_BASE_COMPROMISSOS = '/api/compromissos';
const URL_BASE_IMOVEIS = '/api/imoveis';
const URL_BASE_PROCESSOS = '/api/processos';

async function requisitar(url, opcoes) {
    let resposta;

    try {
        resposta = await fetch(url, opcoes);
    } catch (erroDeRede) {
        throw {
            tipo: 'CONEXAO',
            mensagem: 'Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.'
        };
    }

    if (resposta.status === 204) {
        return null;
    }

    let corpo = null;
    const temConteudo = resposta.headers.get('content-length') !== '0';

    if (temConteudo) {
        try {
            corpo = await resposta.json();
        } catch (erroDeParse) {
            corpo = null;
        }
    }

    if (!resposta.ok) {
        throw {
            tipo: 'API',
            status: resposta.status,
            mensagem: (corpo && corpo.mensagem) ? corpo.mensagem : 'Ocorreu um erro ao processar a solicitação.',
            erros: (corpo && corpo.erros) ? corpo.erros : null
        };
    }

    return corpo;
}

function listarClientes(busca) {
    let url = URL_BASE_CLIENTES;

    if (busca && busca.trim() !== '') {
        url += '?busca=' + encodeURIComponent(busca.trim());
    }

    return requisitar(url, {
        method: 'GET'
    });
}

function buscarCliente(id) {
    return requisitar(URL_BASE_CLIENTES + '/' + id, {
        method: 'GET'
    });
}

function criarCliente(dadosCliente) {
    return requisitar(URL_BASE_CLIENTES, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(dadosCliente)
    });
}

function atualizarCliente(id, dadosCliente) {
    return requisitar(URL_BASE_CLIENTES + '/' + id, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(dadosCliente)
    });
}

function excluirCliente(id) {
    return requisitar(URL_BASE_CLIENTES + '/' + id, {
        method: 'DELETE'
    });
}

function listarAtendimentosPorCliente(clienteId) {
    return requisitar(URL_BASE_ATENDIMENTOS + '?clienteId=' + clienteId, {
        method: 'GET'
    });
}

function buscarAtendimento(id) {
    return requisitar(URL_BASE_ATENDIMENTOS + '/' + id, {
        method: 'GET'
    });
}

function criarAtendimento(dadosAtendimento) {
    return requisitar(URL_BASE_ATENDIMENTOS, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(dadosAtendimento)
    });
}

function atualizarAtendimento(id, dadosAtendimento) {
    return requisitar(URL_BASE_ATENDIMENTOS + '/' + id, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(dadosAtendimento)
    });
}

function excluirAtendimento(id) {
    return requisitar(URL_BASE_ATENDIMENTOS + '/' + id, {
        method: 'DELETE'
    });
}

function listarTarefasPorCliente(clienteId) {
    return requisitar(URL_BASE_TAREFAS + '?clienteId=' + clienteId, {
        method: 'GET'
    });
}

function buscarTarefa(id) {
    return requisitar(URL_BASE_TAREFAS + '/' + id, {
        method: 'GET'
    });
}

function criarTarefa(dadosTarefa) {
    return requisitar(URL_BASE_TAREFAS, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(dadosTarefa)
    });
}

function atualizarTarefa(id, dadosTarefa) {
    return requisitar(URL_BASE_TAREFAS + '/' + id, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(dadosTarefa)
    });
}

function excluirTarefa(id) {
    return requisitar(URL_BASE_TAREFAS + '/' + id, {
        method: 'DELETE'
    });
}

function adicionarItemChecklist(tarefaId, dadosItem) {
    return requisitar(URL_BASE_TAREFAS + '/' + tarefaId + '/checklist', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(dadosItem)
    });
}

function alternarItemChecklist(tarefaId, itemId) {
    return requisitar(URL_BASE_TAREFAS + '/' + tarefaId + '/checklist/' + itemId, {
        method: 'PATCH'
    });
}

function removerItemChecklist(tarefaId, itemId) {
    return requisitar(URL_BASE_TAREFAS + '/' + tarefaId + '/checklist/' + itemId, {
        method: 'DELETE'
    });
}

// mes no formato "AAAA-MM" (ex.: "2026-11")
function buscarCalendario(mes) {
    return requisitar(URL_BASE_CALENDARIO + '?mes=' + encodeURIComponent(mes), {
        method: 'GET'
    });
}

function buscarDashboard() {
    return requisitar(URL_BASE_DASHBOARD, {
        method: 'GET'
    });
}

function buscarCompromisso(id) {
    return requisitar(URL_BASE_COMPROMISSOS + '/' + id, {
        method: 'GET'
    });
}

function criarCompromisso(dadosCompromisso) {
    return requisitar(URL_BASE_COMPROMISSOS, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(dadosCompromisso)
    });
}

function atualizarCompromisso(id, dadosCompromisso) {
    return requisitar(URL_BASE_COMPROMISSOS + '/' + id, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(dadosCompromisso)
    });
}

function excluirCompromisso(id) {
    return requisitar(URL_BASE_COMPROMISSOS + '/' + id, {
        method: 'DELETE'
    });
}

// clienteId e busca são opcionais
function listarImoveis(clienteId, busca) {
    const parametros = new URLSearchParams();

    if (clienteId) {
        parametros.set('clienteId', clienteId);
    }
    if (busca && busca.trim() !== '') {
        parametros.set('busca', busca.trim());
    }

    const consulta = parametros.toString();
    return requisitar(URL_BASE_IMOVEIS + (consulta ? '?' + consulta : ''), {
        method: 'GET'
    });
}

function buscarImovel(id) {
    return requisitar(URL_BASE_IMOVEIS + '/' + id, {
        method: 'GET'
    });
}

function criarImovel(dadosImovel) {
    return requisitar(URL_BASE_IMOVEIS, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(dadosImovel)
    });
}

function atualizarImovel(id, dadosImovel) {
    return requisitar(URL_BASE_IMOVEIS + '/' + id, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(dadosImovel)
    });
}

function excluirImovel(id) {
    return requisitar(URL_BASE_IMOVEIS + '/' + id, {
        method: 'DELETE'
    });
}

// Todos os filtros são opcionais
function listarProcessos(clienteId, status, busca) {
    const parametros = new URLSearchParams();

    if (clienteId) {
        parametros.set('clienteId', clienteId);
    }
    if (status) {
        parametros.set('status', status);
    }
    if (busca && busca.trim() !== '') {
        parametros.set('busca', busca.trim());
    }

    const consulta = parametros.toString();
    return requisitar(URL_BASE_PROCESSOS + (consulta ? '?' + consulta : ''), {
        method: 'GET'
    });
}

function buscarProcesso(id) {
    return requisitar(URL_BASE_PROCESSOS + '/' + id, {
        method: 'GET'
    });
}

function criarProcesso(dadosProcesso) {
    return requisitar(URL_BASE_PROCESSOS, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(dadosProcesso)
    });
}

function atualizarProcesso(id, dadosProcesso) {
    return requisitar(URL_BASE_PROCESSOS + '/' + id, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(dadosProcesso)
    });
}

function excluirProcesso(id) {
    return requisitar(URL_BASE_PROCESSOS + '/' + id, {
        method: 'DELETE'
    });
}
