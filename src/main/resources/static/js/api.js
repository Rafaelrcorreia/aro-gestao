const URL_BASE_CLIENTES = '/api/clientes';
const URL_BASE_ATENDIMENTOS = '/api/atendimentos';
const URL_BASE_TAREFAS = '/api/tarefas';
const URL_BASE_CALENDARIO = '/api/calendario';
const URL_BASE_DASHBOARD = '/api/dashboard';
const URL_BASE_COMPROMISSOS = '/api/compromissos';
const URL_BASE_IMOVEIS = '/api/imoveis';
const URL_BASE_PROCESSOS = '/api/processos';
const URL_BASE_DOCUMENTOS = '/api/documentos';
const URL_BASE_LANCAMENTOS = '/api/lancamentos';
const URL_BASE_AUTH = '/api/auth';
const URL_BASE_USUARIOS = '/api/usuarios';

const METODOS_SEM_CSRF = ['GET', 'HEAD', 'OPTIONS'];

function lerCookie(nome) {
    const item = document.cookie.split('; ').find(function (par) { return par.startsWith(nome + '='); });
    return item ? decodeURIComponent(item.slice(nome.length + 1)) : null;
}

// O servidor guarda o token CSRF no cookie XSRF-TOKEN; toda alteração (POST, PUT, PATCH, DELETE)
// precisa devolvê-lo no cabeçalho X-XSRF-TOKEN. Se o cookie ainda não existe, pedimos um.
async function obterTokenCsrf() {
    let token = lerCookie('XSRF-TOKEN');
    if (!token) {
        try {
            await fetch(URL_BASE_AUTH + '/status');
        } catch (erro) {
            // sem conexão: a requisição principal vai mostrar o erro
        }
        token = lerCookie('XSRF-TOKEN');
    }
    return token;
}

function estaNaPaginaDeLogin() {
    return window.location.pathname.endsWith('/login.html');
}

// Sessão expirada ou inexistente: volta para o login e, depois de entrar, retorna para cá
function irParaLogin() {
    const voltar = window.location.pathname + window.location.search;
    window.location.href = '/login.html?voltar=' + encodeURIComponent(voltar);
}

async function requisitar(url, opcoes) {
    let resposta;
    const configuracao = Object.assign({}, opcoes || {});
    const metodo = (configuracao.method || 'GET').toUpperCase();

    if (!METODOS_SEM_CSRF.includes(metodo)) {
        const token = await obterTokenCsrf();
        configuracao.headers = Object.assign({}, configuracao.headers || {});
        if (token) {
            configuracao.headers['X-XSRF-TOKEN'] = token;
        }
    }

    try {
        resposta = await fetch(url, configuracao);
    } catch (erroDeRede) {
        throw {
            tipo: 'CONEXAO',
            mensagem: 'Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.'
        };
    }

    if (resposta.status === 401 && !estaNaPaginaDeLogin()) {
        irParaLogin();
        // Não resolve: a página está sendo trocada pela de login
        return new Promise(function () {});
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

// Todos os filtros são opcionais
function listarDocumentos(clienteId, tipo, busca) {
    const parametros = new URLSearchParams();

    if (clienteId) {
        parametros.set('clienteId', clienteId);
    }
    if (tipo) {
        parametros.set('tipo', tipo);
    }
    if (busca && busca.trim() !== '') {
        parametros.set('busca', busca.trim());
    }

    const consulta = parametros.toString();
    return requisitar(URL_BASE_DOCUMENTOS + (consulta ? '?' + consulta : ''), {
        method: 'GET'
    });
}

function buscarDocumento(id) {
    return requisitar(URL_BASE_DOCUMENTOS + '/' + id, {
        method: 'GET'
    });
}

// Documentos vão como multipart: "dados" (JSON) + "arquivo" (opcional).
// Não definimos Content-Type: o navegador monta o cabeçalho com o "boundary" certo.
function montarEnvioDocumento(dadosDocumento, arquivo) {
    const envio = new FormData();
    envio.append('dados', new Blob([JSON.stringify(dadosDocumento)], { type: 'application/json' }));
    if (arquivo) {
        envio.append('arquivo', arquivo);
    }
    return envio;
}

function criarDocumento(dadosDocumento, arquivo) {
    return requisitar(URL_BASE_DOCUMENTOS, {
        method: 'POST',
        body: montarEnvioDocumento(dadosDocumento, arquivo)
    });
}

function atualizarDocumento(id, dadosDocumento, arquivo) {
    return requisitar(URL_BASE_DOCUMENTOS + '/' + id, {
        method: 'PUT',
        body: montarEnvioDocumento(dadosDocumento, arquivo)
    });
}

function excluirDocumento(id) {
    return requisitar(URL_BASE_DOCUMENTOS + '/' + id, {
        method: 'DELETE'
    });
}

// mes no formato "AAAA-MM" (obrigatório); tipo, clienteId e busca são opcionais
function listarLancamentos(mes, tipo, clienteId, busca) {
    const parametros = new URLSearchParams();
    parametros.set('mes', mes);
    if (tipo) {
        parametros.set('tipo', tipo);
    }
    if (clienteId) {
        parametros.set('clienteId', clienteId);
    }
    if (busca && busca.trim() !== '') {
        parametros.set('busca', busca.trim());
    }
    return requisitar(URL_BASE_LANCAMENTOS + '?' + parametros.toString(), {
        method: 'GET'
    });
}

function buscarResumoFinanceiro(mes) {
    return requisitar(URL_BASE_LANCAMENTOS + '/resumo?mes=' + encodeURIComponent(mes), {
        method: 'GET'
    });
}

function buscarLancamento(id) {
    return requisitar(URL_BASE_LANCAMENTOS + '/' + id, {
        method: 'GET'
    });
}

function criarLancamento(dadosLancamento) {
    return requisitar(URL_BASE_LANCAMENTOS, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(dadosLancamento)
    });
}

function atualizarLancamento(id, dadosLancamento) {
    return requisitar(URL_BASE_LANCAMENTOS + '/' + id, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(dadosLancamento)
    });
}

function registrarPagamentoLancamento(id, dadosPagamento) {
    return requisitar(URL_BASE_LANCAMENTOS + '/' + id + '/pagamento', {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(dadosPagamento)
    });
}

function desfazerPagamentoLancamento(id) {
    return requisitar(URL_BASE_LANCAMENTOS + '/' + id + '/pagamento', {
        method: 'DELETE'
    });
}

function excluirLancamento(id) {
    return requisitar(URL_BASE_LANCAMENTOS + '/' + id, {
        method: 'DELETE'
    });
}

// Endereço para abrir (PDF/imagem) ou baixar o arquivo do documento
function urlArquivoDocumento(id, baixar) {
    return URL_BASE_DOCUMENTOS + '/' + id + '/arquivo' + (baixar ? '?download=true' : '');
}

/* ---------- Login, sessão e usuários ---------- */

function buscarStatusAutenticacao() {
    return requisitar(URL_BASE_AUTH + '/status', { method: 'GET' });
}

function buscarUsuarioLogado() {
    return requisitar(URL_BASE_AUTH + '/me', { method: 'GET' });
}

// O login vai como formulário (login e senha), que é o formato que o Spring Security espera
function entrar(login, senha) {
    const corpo = new URLSearchParams();
    corpo.set('login', login);
    corpo.set('senha', senha);
    return requisitar(URL_BASE_AUTH + '/login', { method: 'POST', body: corpo });
}

function sair() {
    return requisitar(URL_BASE_AUTH + '/logout', { method: 'POST' });
}

function criarPrimeiroAdministrador(dados) {
    return requisitar(URL_BASE_AUTH + '/primeiro-acesso', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dados)
    });
}

function alterarMinhaSenha(senhaAtual, novaSenha) {
    return requisitar(URL_BASE_AUTH + '/senha', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ senhaAtual: senhaAtual, novaSenha: novaSenha })
    });
}

function listarUsuarios() {
    return requisitar(URL_BASE_USUARIOS, { method: 'GET' });
}

function criarUsuario(dados) {
    return requisitar(URL_BASE_USUARIOS, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dados)
    });
}

function atualizarUsuario(id, dados) {
    return requisitar(URL_BASE_USUARIOS + '/' + id, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dados)
    });
}

function redefinirSenhaUsuario(id, novaSenha) {
    return requisitar(URL_BASE_USUARIOS + '/' + id + '/senha', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ novaSenha: novaSenha })
    });
}
