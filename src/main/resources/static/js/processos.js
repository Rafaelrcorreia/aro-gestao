// Página de processos. Aceita ?clienteId=N na URL para já abrir filtrada por um cliente.
const parametrosUrlProcessos = new URLSearchParams(window.location.search);

let processoEmEdicaoId = null;
let processoParaExcluirId = null;
let versaoCarregamento = 0;
let versaoEdicao = 0;
let versaoImoveis = 0;
let temporizadorPesquisa;

const rotulosTipoProcesso = {
    GEORREFERENCIAMENTO: 'Georreferenciamento',
    CAR: 'CAR',
    CCIR: 'CCIR',
    ITR: 'ITR',
    RETIFICACAO_MATRICULA: 'Retificação de matrícula',
    REGULARIZACAO_FUNDIARIA: 'Regularização fundiária',
    USUCAPIAO: 'Usucapião',
    LICENCIAMENTO_AMBIENTAL: 'Licenciamento ambiental',
    OUTORGA_AGUA: 'Outorga de água',
    OUTRO: 'Outro'
};

const rotulosStatusProcesso = {
    EM_ELABORACAO: 'Em elaboração',
    PROTOCOLADO: 'Protocolado',
    EM_ANALISE: 'Em análise',
    EXIGENCIA: 'Exigência',
    DEFERIDO: 'Deferido',
    INDEFERIDO: 'Indeferido',
    ARQUIVADO: 'Arquivado'
};

// Nome do campo na API -> id do campo na tela
const camposProcesso = {
    tipo: 'selectTipo',
    status: 'selectStatus',
    clienteId: 'selectCliente',
    imovelId: 'selectImovel',
    numeroProtocolo: 'inputNumeroProtocolo',
    orgao: 'inputOrgao',
    dataAbertura: 'inputDataAbertura',
    dataProtocolo: 'inputDataProtocolo',
    prazoPrevisto: 'inputPrazoPrevisto',
    dataConclusao: 'inputDataConclusao',
    descricao: 'textareaDescricao'
};

// Nome do campo na API -> id do <span> de erro
const errosProcesso = {
    tipo: 'erroTipo',
    status: 'erroStatus',
    clienteId: 'erroClienteId',
    imovelId: 'erroImovelId',
    numeroProtocolo: 'erroNumeroProtocolo',
    orgao: 'erroOrgao',
    dataAbertura: 'erroDataAbertura',
    dataProtocolo: 'erroDataProtocolo',
    prazoPrevisto: 'erroPrazoPrevisto',
    dataConclusao: 'erroDataConclusao',
    descricao: 'erroDescricao'
};

function elemento(id) {
    return document.getElementById(id);
}

document.addEventListener('DOMContentLoaded', async function () {
    elemento('modalFormulario').addEventListener('close', function () {
        elemento('formularioProcesso').reset();
        limparErrosFormulario();
        preencherImoveis([]);
        processoEmEdicaoId = null;
        versaoEdicao++;
    });
    elemento('btnNovoProcesso').addEventListener('click', abrirModalNovoProcesso);
    elemento('btnFecharModalFormulario').addEventListener('click', fecharModalFormulario);
    elemento('btnCancelarFormulario').addEventListener('click', fecharModalFormulario);
    elemento('formularioProcesso').addEventListener('submit', salvarProcesso);
    elemento('btnFecharModalExclusao').addEventListener('click', fecharModalExclusao);
    elemento('btnCancelarExclusao').addEventListener('click', fecharModalExclusao);
    elemento('btnConfirmarExclusao').addEventListener('click', confirmarExclusao);

    // Ao trocar o cliente no formulário, a lista de imóveis passa a ser a desse cliente
    elemento('selectCliente').addEventListener('change', function () {
        carregarImoveisDoCliente(this.value, null);
    });

    elemento('selectFiltroCliente').addEventListener('change', function () {
        atualizarUrlComCliente(this.value);
        carregarProcessos();
    });
    elemento('selectFiltroStatus').addEventListener('change', carregarProcessos);
    elemento('inputPesquisa').addEventListener('input', function () {
        clearTimeout(temporizadorPesquisa);
        // Invalida também uma consulta que ainda esteja em andamento.
        versaoCarregamento++;
        temporizadorPesquisa = setTimeout(carregarProcessos, 300);
    });

    await carregarClientes();
    carregarProcessos();
});

/* ---------- Clientes e imóveis ---------- */

async function carregarClientes() {
    try {
        const clientes = await listarClientes('');
        const ordenados = (clientes || []).slice().sort(function (a, b) {
            return a.nomeCompleto.localeCompare(b.nomeCompleto, 'pt-BR');
        });
        ['selectFiltroCliente', 'selectCliente'].forEach(function (idSelect) {
            const select = elemento(idSelect);
            ordenados.forEach(function (cliente) {
                const opcao = document.createElement('option');
                opcao.value = cliente.id;
                opcao.textContent = cliente.nomeCompleto;
                select.appendChild(opcao);
            });
        });

        const clienteDaUrl = parametrosUrlProcessos.get('clienteId');
        if (clienteDaUrl && ordenados.some(function (c) { return String(c.id) === clienteDaUrl; })) {
            elemento('selectFiltroCliente').value = clienteDaUrl;
        }
    } catch (erro) {
        exibirMensagem('Não foi possível carregar a lista de clientes.', 'aviso');
    }
}

function preencherImoveis(imoveis) {
    const select = elemento('selectImovel');
    select.replaceChildren();
    const vazio = document.createElement('option');
    vazio.value = '';
    vazio.textContent = 'Sem imóvel';
    select.appendChild(vazio);
    imoveis.forEach(function (imovel) {
        const opcao = document.createElement('option');
        opcao.value = imovel.id;
        opcao.textContent = imovel.denominacao + ' — ' + imovel.municipio + '/' + imovel.estado;
        select.appendChild(opcao);
    });
}

// Carrega os imóveis do cliente e, se informado, seleciona um deles
async function carregarImoveisDoCliente(clienteId, imovelSelecionado) {
    const versaoAtual = ++versaoImoveis;
    preencherImoveis([]);
    if (!clienteId) return;
    try {
        const imoveis = await listarImoveis(clienteId, '');
        if (versaoAtual !== versaoImoveis) return;
        preencherImoveis(imoveis || []);
        if (imovelSelecionado) {
            elemento('selectImovel').value = String(imovelSelecionado);
        }
    } catch (erro) {
        if (versaoAtual === versaoImoveis) {
            exibirMensagem('Não foi possível carregar os imóveis do cliente.', 'aviso');
        }
    }
}

// Mantém o filtro de cliente na URL, para que voltar/recarregar mostre a mesma lista
function atualizarUrlComCliente(clienteId) {
    const url = new URL(window.location.href);
    if (clienteId) {
        url.searchParams.set('clienteId', clienteId);
    } else {
        url.searchParams.delete('clienteId');
    }
    history.replaceState(null, '', url);
}

/* ---------- Lista ---------- */

async function carregarProcessos() {
    const versaoAtual = ++versaoCarregamento;
    elemento('indicadorCarregamento').classList.remove('oculto');
    try {
        const lista = await listarProcessos(
            elemento('selectFiltroCliente').value,
            elemento('selectFiltroStatus').value,
            elemento('inputPesquisa').value);
        if (versaoAtual !== versaoCarregamento) return;
        if (!Array.isArray(lista)) throw new Error('Resposta inválida ao listar processos.');
        renderizarProcessos(lista);
    } catch (erro) {
        if (versaoAtual !== versaoCarregamento) return;
        elemento('corpoTabelaProcessos').replaceChildren();
        elemento('estadoVazioProcessos').classList.add('oculto');
        exibirMensagem(erro.mensagem || erro.message || 'Não foi possível carregar os processos.', 'erro');
    } finally {
        if (versaoAtual === versaoCarregamento) {
            elemento('indicadorCarregamento').classList.add('oculto');
        }
    }
}

function renderizarProcessos(lista) {
    const corpoTabela = elemento('corpoTabelaProcessos');
    corpoTabela.replaceChildren();
    elemento('estadoVazioProcessos').classList.toggle('oculto', lista.length > 0);

    lista.forEach(function (processo) {
        const linha = document.createElement('tr');

        linha.appendChild(criarCelula('Tipo', rotulosTipoProcesso[processo.tipo] || processo.tipo));
        linha.appendChild(criarCelulaDuasLinhas('Cliente / Imóvel', processo.nomeCliente, processo.denominacaoImovel));
        linha.appendChild(criarCelulaDuasLinhas('Protocolo / Órgão', processo.numeroProtocolo, processo.orgao));
        linha.appendChild(criarCelula('Abertura', formatarData(processo.dataAbertura)));

        const celulaPrazo = criarCelula('Prazo previsto', formatarData(processo.prazoPrevisto));
        if (processo.atrasado) {
            celulaPrazo.classList.add('prazo-processo-atrasado');
            celulaPrazo.title = 'Prazo vencido e processo ainda em andamento';
        }
        linha.appendChild(celulaPrazo);

        const celulaStatus = criarCelula('Status', '');
        const selo = document.createElement('span');
        selo.classList.add('selo-processo', 'processo-' + String(processo.status).toLowerCase());
        selo.textContent = rotulosStatusProcesso[processo.status] || processo.status;
        celulaStatus.replaceChildren(selo);
        linha.appendChild(celulaStatus);

        linha.appendChild(criarCelulaAcoes(processo));
        corpoTabela.appendChild(linha);
    });
}

function criarCelula(rotulo, valor) {
    const celula = document.createElement('td');
    celula.setAttribute('data-rotulo', rotulo);
    celula.textContent = valor || '—';
    return celula;
}

// Texto principal com um complemento menor embaixo (ex.: cliente e imóvel)
function criarCelulaDuasLinhas(rotulo, principal, complemento) {
    const celula = criarCelula(rotulo, principal);
    if (complemento) {
        const detalhe = document.createElement('span');
        detalhe.classList.add('imovel-codigos');
        detalhe.textContent = complemento;
        celula.appendChild(detalhe);
    }
    return celula;
}

function criarCelulaAcoes(processo) {
    const celula = document.createElement('td');
    celula.setAttribute('data-rotulo', 'Ações');
    const container = document.createElement('div');
    container.classList.add('acoes-tabela');
    const descricao = (rotulosTipoProcesso[processo.tipo] || processo.tipo) + ' de ' + processo.nomeCliente;
    const acoes = [
        ['Editar', function () { abrirModalEdicaoProcesso(processo.id); }],
        ['Excluir', function () { abrirModalExclusao(processo.id, descricao); }]
    ];
    acoes.forEach(function (acao) {
        const botao = document.createElement('button');
        botao.type = 'button';
        botao.classList.add('botao-icone');
        if (acao[0] === 'Excluir') botao.classList.add('excluir');
        botao.textContent = acao[0];
        botao.addEventListener('click', acao[1]);
        container.appendChild(botao);
    });
    celula.appendChild(container);
    return celula;
}

// Recebe "AAAA-MM-DD" e devolve "DD/MM/AAAA" sem usar new Date(texto), que erraria por causa do fuso
function formatarData(valor) {
    if (!valor) return '';
    const partes = valor.split('-');
    return partes[2] + '/' + partes[1] + '/' + partes[0];
}

function dataDeHojeTexto() {
    const hoje = new Date();
    return hoje.getFullYear() + '-' + String(hoje.getMonth() + 1).padStart(2, '0')
        + '-' + String(hoje.getDate()).padStart(2, '0');
}

/* ---------- Formulário ---------- */

function abrirModalNovoProcesso() {
    versaoEdicao++;
    processoEmEdicaoId = null;
    elemento('formularioProcesso').reset();
    limparErrosFormulario();
    elemento('tituloModalFormulario').textContent = 'Novo processo';
    elemento('selectStatus').value = 'EM_ELABORACAO';
    elemento('inputDataAbertura').value = dataDeHojeTexto();
    // Se a lista está filtrada por um cliente, já deixa ele selecionado
    const clienteFiltrado = elemento('selectFiltroCliente').value;
    elemento('selectCliente').value = clienteFiltrado;
    carregarImoveisDoCliente(clienteFiltrado, null);
    elemento('modalFormulario').showModal();
    elemento('selectTipo').focus();
}

async function abrirModalEdicaoProcesso(id) {
    const versaoAtual = ++versaoEdicao;
    try {
        const processo = await buscarProcesso(id);
        if (versaoAtual !== versaoEdicao) return;
        processoEmEdicaoId = id;
        elemento('formularioProcesso').reset();
        limparErrosFormulario();
        Object.entries(camposProcesso).forEach(function ([campo, idCampo]) {
            if (campo === 'imovelId') return;
            elemento(idCampo).value = processo[campo] ?? '';
        });
        elemento('selectCliente').value = String(processo.clienteId);
        carregarImoveisDoCliente(processo.clienteId, processo.imovelId);
        elemento('tituloModalFormulario').textContent = 'Editar processo';
        elemento('modalFormulario').showModal();
    } catch (erro) {
        if (versaoAtual === versaoEdicao) {
            exibirMensagem(erro.mensagem || 'Não foi possível abrir o processo.', 'erro');
        }
    }
}

function fecharModalFormulario() {
    elemento('modalFormulario').close();
}

function montarDados() {
    const dados = {};
    Object.entries(camposProcesso).forEach(function ([campo, idCampo]) {
        const valor = elemento(idCampo).value.trim();
        dados[campo] = valor === '' ? null : valor;
    });
    dados.clienteId = dados.clienteId ? Number(dados.clienteId) : null;
    dados.imovelId = dados.imovelId ? Number(dados.imovelId) : null;
    return dados;
}

function validarFormulario(dados) {
    let valido = true;
    if (!dados.tipo) {
        marcarErro('tipo', 'Selecione o tipo do processo');
        valido = false;
    }
    if (!dados.clienteId) {
        marcarErro('clienteId', 'Selecione o cliente');
        valido = false;
    }
    if (!dados.dataAbertura) {
        marcarErro('dataAbertura', 'Informe a data de abertura');
        valido = false;
    } else {
        // Datas "AAAA-MM-DD" podem ser comparadas como texto
        if (dados.dataProtocolo && dados.dataProtocolo < dados.dataAbertura) {
            marcarErro('dataProtocolo', 'Não pode ser antes da abertura');
            valido = false;
        }
        if (dados.dataConclusao && dados.dataConclusao < dados.dataAbertura) {
            marcarErro('dataConclusao', 'Não pode ser antes da abertura');
            valido = false;
        }
    }
    return valido;
}

async function salvarProcesso(evento) {
    evento.preventDefault();
    const botao = elemento('btnSalvarProcesso');
    if (botao.disabled) return;
    limparErrosFormulario();

    const dados = montarDados();
    if (!validarFormulario(dados)) return;

    botao.disabled = true;
    try {
        if (processoEmEdicaoId !== null) {
            await atualizarProcesso(processoEmEdicaoId, dados);
        } else {
            await criarProcesso(dados);
        }
        fecharModalFormulario();
        exibirMensagem('Processo salvo com sucesso.', 'sucesso');
        // Mostra o registro salvo mesmo quando havia pesquisa ou filtro de status ativo.
        elemento('inputPesquisa').value = '';
        elemento('selectFiltroStatus').value = '';
        clearTimeout(temporizadorPesquisa);
        await carregarProcessos();
    } catch (erro) {
        let marcouCampo = false;
        Object.entries(erro.erros || {}).forEach(function ([campo, mensagem]) {
            if (marcarErro(campo, mensagem)) marcouCampo = true;
        });
        if (!marcouCampo) {
            exibirMensagem(erro.mensagem || 'Não foi possível salvar o processo.', 'erro');
        }
    } finally {
        botao.disabled = false;
    }
}

function marcarErro(campo, mensagem) {
    if (!errosProcesso[campo]) return false;
    elemento(errosProcesso[campo]).textContent = mensagem;
    return true;
}

function limparErrosFormulario() {
    document.querySelectorAll('#formularioProcesso .mensagem-erro-campo').forEach(function (campo) {
        campo.textContent = '';
    });
}

/* ---------- Exclusão ---------- */

function abrirModalExclusao(id, descricao) {
    processoParaExcluirId = id;
    elemento('textoConfirmarExclusao').textContent =
        'Deseja excluir o processo "' + descricao + '"? Essa ação não pode ser desfeita.';
    elemento('modalConfirmarExclusao').showModal();
}

function fecharModalExclusao() {
    elemento('modalConfirmarExclusao').close();
}

async function confirmarExclusao() {
    const botao = elemento('btnConfirmarExclusao');
    if (botao.disabled || processoParaExcluirId === null) return;
    botao.disabled = true;
    try {
        await excluirProcesso(processoParaExcluirId);
        fecharModalExclusao();
        processoParaExcluirId = null;
        exibirMensagem('Processo excluído com sucesso.', 'sucesso');
        await carregarProcessos();
    } catch (erro) {
        exibirMensagem(erro.mensagem || 'Não foi possível excluir o processo.', 'erro');
    } finally {
        botao.disabled = false;
    }
}

function exibirMensagem(texto, tipo) {
    const mensagem = document.createElement('div');
    mensagem.classList.add('mensagem-toast', tipo);
    mensagem.textContent = texto;
    elemento('containerMensagens').appendChild(mensagem);
    setTimeout(function () { mensagem.remove(); }, 5000);
}
