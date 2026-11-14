// Página de documentos. Aceita ?clienteId=N na URL para já abrir filtrada por um cliente.
const parametrosUrlDocumentos = new URLSearchParams(window.location.search);

const TAMANHO_MAXIMO_ARQUIVO = 20 * 1024 * 1024; // 20 MB, igual ao limite do servidor

let documentosCarregados = [];
let documentoEmEdicaoId = null;
let documentoParaExcluirId = null;
let versaoCarregamento = 0;
let versaoEdicao = 0;
let versaoVinculos = 0;
let temporizadorPesquisa;

const rotulosTipoDocumento = {
    MATRICULA: 'Matrícula',
    ESCRITURA: 'Escritura',
    CCIR: 'CCIR',
    ITR: 'ITR',
    CAR: 'CAR',
    CERTIDAO: 'Certidão',
    PLANTA_MEMORIAL: 'Planta e memorial descritivo',
    DOCUMENTO_PESSOAL: 'Documento pessoal (RG, CPF, CNH)',
    COMPROVANTE_RESIDENCIA: 'Comprovante de residência',
    CONTRATO: 'Contrato',
    PROCURACAO: 'Procuração',
    LICENCA: 'Licença / autorização',
    OUTRO: 'Outro'
};

const rotulosTipoProcessoDoc = {
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

const rotulosValidade = {
    VENCIDO: 'Vencido',
    VENCENDO: 'Vence em breve',
    VALIDO: 'Válido'
};

// Nome do campo na API -> id do campo na tela
const camposDocumento = {
    tipo: 'selectTipo',
    numero: 'inputNumero',
    titulo: 'inputTitulo',
    clienteId: 'selectCliente',
    imovelId: 'selectImovel',
    processoId: 'selectProcesso',
    dataEmissao: 'inputDataEmissao',
    dataValidade: 'inputDataValidade',
    observacoes: 'textareaObservacoes'
};

// Nome do campo na API -> id do <span> de erro
const errosDocumento = {
    tipo: 'erroTipo',
    numero: 'erroNumero',
    titulo: 'erroTitulo',
    clienteId: 'erroClienteId',
    imovelId: 'erroImovelId',
    processoId: 'erroProcessoId',
    dataEmissao: 'erroDataEmissao',
    dataValidade: 'erroDataValidade',
    arquivo: 'erroArquivo',
    observacoes: 'erroObservacoes'
};

function elemento(id) {
    return document.getElementById(id);
}

document.addEventListener('DOMContentLoaded', async function () {
    preencherTipos();

    elemento('modalFormulario').addEventListener('close', function () {
        elemento('formularioDocumento').reset();
        limparErrosFormulario();
        preencherVinculos([], []);
        mostrarArquivoAtual(null);
        documentoEmEdicaoId = null;
        versaoEdicao++;
    });
    elemento('btnNovoDocumento').addEventListener('click', abrirModalNovoDocumento);
    elemento('btnFecharModalFormulario').addEventListener('click', fecharModalFormulario);
    elemento('btnCancelarFormulario').addEventListener('click', fecharModalFormulario);
    elemento('formularioDocumento').addEventListener('submit', salvarDocumento);
    elemento('btnFecharModalExclusao').addEventListener('click', fecharModalExclusao);
    elemento('btnCancelarExclusao').addEventListener('click', fecharModalExclusao);
    elemento('btnConfirmarExclusao').addEventListener('click', confirmarExclusao);

    // Ao trocar o cliente no formulário, imóveis e processos passam a ser os desse cliente
    elemento('selectCliente').addEventListener('change', function () {
        carregarVinculosDoCliente(this.value, null, null);
    });

    elemento('selectFiltroCliente').addEventListener('change', function () {
        atualizarUrlComCliente(this.value);
        carregarDocumentos();
    });
    elemento('selectFiltroTipo').addEventListener('change', carregarDocumentos);
    // A validade é filtrada aqui mesmo, sem nova consulta
    elemento('selectFiltroValidade').addEventListener('change', renderizarDocumentos);
    elemento('inputPesquisa').addEventListener('input', function () {
        clearTimeout(temporizadorPesquisa);
        // Invalida também uma consulta que ainda esteja em andamento.
        versaoCarregamento++;
        temporizadorPesquisa = setTimeout(carregarDocumentos, 300);
    });

    await carregarClientes();
    carregarDocumentos();
});

function preencherTipos() {
    ['selectFiltroTipo', 'selectTipo'].forEach(function (idSelect) {
        const select = elemento(idSelect);
        Object.entries(rotulosTipoDocumento).forEach(function ([valor, rotulo]) {
            const opcao = document.createElement('option');
            opcao.value = valor;
            opcao.textContent = rotulo;
            select.appendChild(opcao);
        });
    });
}

/* ---------- Clientes, imóveis e processos ---------- */

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

        const clienteDaUrl = parametrosUrlDocumentos.get('clienteId');
        if (clienteDaUrl && ordenados.some(function (c) { return String(c.id) === clienteDaUrl; })) {
            elemento('selectFiltroCliente').value = clienteDaUrl;
        }
    } catch (erro) {
        exibirMensagem('Não foi possível carregar a lista de clientes.', 'aviso');
    }
}

function preencherSelect(idSelect, itens, textoDe) {
    const select = elemento(idSelect);
    select.replaceChildren();
    const vazio = document.createElement('option');
    vazio.value = '';
    vazio.textContent = 'Nenhum';
    select.appendChild(vazio);
    itens.forEach(function (item) {
        const opcao = document.createElement('option');
        opcao.value = item.id;
        opcao.textContent = textoDe(item);
        select.appendChild(opcao);
    });
}

function preencherVinculos(imoveis, processos) {
    preencherSelect('selectImovel', imoveis, function (imovel) {
        return imovel.denominacao + ' — ' + imovel.municipio + '/' + imovel.estado;
    });
    preencherSelect('selectProcesso', processos, function (processo) {
        const tipo = rotulosTipoProcessoDoc[processo.tipo] || processo.tipo;
        return processo.numeroProtocolo ? tipo + ' — ' + processo.numeroProtocolo : tipo + ' (aberto em ' + formatarData(processo.dataAbertura) + ')';
    });
}

// Carrega imóveis e processos do cliente e, se informado, seleciona os do documento
async function carregarVinculosDoCliente(clienteId, imovelSelecionado, processoSelecionado) {
    const versaoAtual = ++versaoVinculos;
    preencherVinculos([], []);
    if (!clienteId) return;
    try {
        const [imoveis, processos] = await Promise.all([
            listarImoveis(clienteId, ''),
            listarProcessos(clienteId, '', '')
        ]);
        if (versaoAtual !== versaoVinculos) return;
        preencherVinculos(imoveis || [], processos || []);
        if (imovelSelecionado) elemento('selectImovel').value = String(imovelSelecionado);
        if (processoSelecionado) elemento('selectProcesso').value = String(processoSelecionado);
    } catch (erro) {
        if (versaoAtual === versaoVinculos) {
            exibirMensagem('Não foi possível carregar os imóveis e processos do cliente.', 'aviso');
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

async function carregarDocumentos() {
    const versaoAtual = ++versaoCarregamento;
    elemento('indicadorCarregamento').classList.remove('oculto');
    try {
        const lista = await listarDocumentos(
            elemento('selectFiltroCliente').value,
            elemento('selectFiltroTipo').value,
            elemento('inputPesquisa').value);
        if (versaoAtual !== versaoCarregamento) return;
        if (!Array.isArray(lista)) throw new Error('Resposta inválida ao listar documentos.');
        documentosCarregados = lista;
        renderizarDocumentos();
    } catch (erro) {
        if (versaoAtual !== versaoCarregamento) return;
        documentosCarregados = [];
        elemento('corpoTabelaDocumentos').replaceChildren();
        elemento('estadoVazioDocumentos').classList.add('oculto');
        exibirMensagem(erro.mensagem || erro.message || 'Não foi possível carregar os documentos.', 'erro');
    } finally {
        if (versaoAtual === versaoCarregamento) {
            elemento('indicadorCarregamento').classList.add('oculto');
        }
    }
}

function renderizarDocumentos() {
    const filtroValidade = elemento('selectFiltroValidade').value;
    const lista = documentosCarregados.filter(function (documento) {
        return !filtroValidade || documento.situacaoValidade === filtroValidade;
    });

    const corpoTabela = elemento('corpoTabelaDocumentos');
    corpoTabela.replaceChildren();
    elemento('estadoVazioDocumentos').classList.toggle('oculto', lista.length > 0);

    lista.forEach(function (documento) {
        const linha = document.createElement('tr');

        const complementoDoc = [rotulosTipoDocumento[documento.tipo] || documento.tipo,
            documento.numero ? 'nº ' + documento.numero : null].filter(Boolean).join(' · ');
        linha.appendChild(criarCelulaDuasLinhas('Documento', documento.titulo, complementoDoc));

        const vinculos = [documento.denominacaoImovel, documento.descricaoProcesso].filter(Boolean).join(' · ');
        linha.appendChild(criarCelulaDuasLinhas('Cliente / Vínculo', documento.nomeCliente, vinculos));

        linha.appendChild(criarCelula('Emissão', formatarData(documento.dataEmissao)));

        const celulaValidade = criarCelula('Validade', formatarData(documento.dataValidade));
        if (rotulosValidade[documento.situacaoValidade]) {
            const selo = document.createElement('span');
            selo.classList.add('selo-validade', 'validade-' + documento.situacaoValidade.toLowerCase());
            selo.textContent = rotulosValidade[documento.situacaoValidade];
            celulaValidade.appendChild(document.createElement('br'));
            celulaValidade.appendChild(selo);
        }
        linha.appendChild(celulaValidade);

        linha.appendChild(criarCelulaArquivo(documento));
        linha.appendChild(criarCelulaAcoes(documento));
        corpoTabela.appendChild(linha);
    });
}

function criarCelula(rotulo, valor) {
    const celula = document.createElement('td');
    celula.setAttribute('data-rotulo', rotulo);
    celula.textContent = valor || '—';
    return celula;
}

// Texto principal com um complemento menor embaixo
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

function criarCelulaArquivo(documento) {
    const celula = criarCelula('Arquivo', '');
    if (!documento.possuiArquivo) {
        celula.textContent = 'Sem arquivo';
        return celula;
    }
    celula.textContent = '';
    const link = document.createElement('a');
    link.classList.add('link-arquivo');
    link.href = urlArquivoDocumento(documento.id, false);
    link.target = '_blank';
    link.rel = 'noopener';
    link.textContent = documento.arquivoNome || 'Abrir arquivo';
    link.title = 'Abrir em nova aba';
    celula.appendChild(link);

    const detalhe = document.createElement('span');
    detalhe.classList.add('imovel-codigos');
    detalhe.textContent = formatarTamanho(documento.arquivoTamanho) + ' · ';
    const baixar = document.createElement('a');
    baixar.href = urlArquivoDocumento(documento.id, true);
    baixar.textContent = 'baixar';
    detalhe.appendChild(baixar);
    celula.appendChild(detalhe);
    return celula;
}

function criarCelulaAcoes(documento) {
    const celula = document.createElement('td');
    celula.setAttribute('data-rotulo', 'Ações');
    const container = document.createElement('div');
    container.classList.add('acoes-tabela');
    const acoes = [
        ['Editar', function () { abrirModalEdicaoDocumento(documento.id); }],
        ['Excluir', function () { abrirModalExclusao(documento.id, documento.titulo, documento.possuiArquivo); }]
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

function formatarTamanho(bytes) {
    if (!bytes && bytes !== 0) return '';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toLocaleString('pt-BR', { maximumFractionDigits: 0 }) + ' KB';
    return (bytes / (1024 * 1024)).toLocaleString('pt-BR', { maximumFractionDigits: 1 }) + ' MB';
}

/* ---------- Formulário ---------- */

function mostrarArquivoAtual(documento) {
    const texto = elemento('textoArquivoAtual');
    if (documento && documento.possuiArquivo) {
        texto.textContent = 'Arquivo atual: ' + documento.arquivoNome + ' (' + formatarTamanho(documento.arquivoTamanho)
            + '). Escolha outro arquivo só se quiser substituí-lo.';
        texto.classList.remove('oculto');
    } else {
        texto.textContent = '';
        texto.classList.add('oculto');
    }
}

function abrirModalNovoDocumento() {
    versaoEdicao++;
    documentoEmEdicaoId = null;
    elemento('formularioDocumento').reset();
    limparErrosFormulario();
    mostrarArquivoAtual(null);
    elemento('tituloModalFormulario').textContent = 'Novo documento';
    // Se a lista está filtrada por um cliente, já deixa ele selecionado
    const clienteFiltrado = elemento('selectFiltroCliente').value;
    elemento('selectCliente').value = clienteFiltrado;
    carregarVinculosDoCliente(clienteFiltrado, null, null);
    elemento('modalFormulario').showModal();
    elemento('selectTipo').focus();
}

async function abrirModalEdicaoDocumento(id) {
    const versaoAtual = ++versaoEdicao;
    try {
        const documento = await buscarDocumento(id);
        if (versaoAtual !== versaoEdicao) return;
        documentoEmEdicaoId = id;
        elemento('formularioDocumento').reset();
        limparErrosFormulario();
        Object.entries(camposDocumento).forEach(function ([campo, idCampo]) {
            if (campo === 'imovelId' || campo === 'processoId') return;
            elemento(idCampo).value = documento[campo] ?? '';
        });
        elemento('selectCliente').value = String(documento.clienteId);
        carregarVinculosDoCliente(documento.clienteId, documento.imovelId, documento.processoId);
        mostrarArquivoAtual(documento);
        elemento('tituloModalFormulario').textContent = 'Editar documento';
        elemento('modalFormulario').showModal();
    } catch (erro) {
        if (versaoAtual === versaoEdicao) {
            exibirMensagem(erro.mensagem || 'Não foi possível abrir o documento.', 'erro');
        }
    }
}

function fecharModalFormulario() {
    elemento('modalFormulario').close();
}

function montarDados() {
    const dados = {};
    Object.entries(camposDocumento).forEach(function ([campo, idCampo]) {
        const valor = elemento(idCampo).value.trim();
        dados[campo] = valor === '' ? null : valor;
    });
    dados.titulo = dados.titulo || '';
    dados.clienteId = dados.clienteId ? Number(dados.clienteId) : null;
    dados.imovelId = dados.imovelId ? Number(dados.imovelId) : null;
    dados.processoId = dados.processoId ? Number(dados.processoId) : null;
    return dados;
}

function validarFormulario(dados, arquivo) {
    let valido = true;
    if (!dados.tipo) {
        marcarErro('tipo', 'Selecione o tipo do documento');
        valido = false;
    }
    if (!dados.titulo) {
        marcarErro('titulo', 'Informe o título');
        valido = false;
    }
    if (!dados.clienteId) {
        marcarErro('clienteId', 'Selecione o cliente');
        valido = false;
    }
    // Datas "AAAA-MM-DD" podem ser comparadas como texto
    if (dados.dataEmissao && dados.dataValidade && dados.dataValidade < dados.dataEmissao) {
        marcarErro('dataValidade', 'Não pode ser antes da emissão');
        valido = false;
    }
    if (arquivo && arquivo.size > TAMANHO_MAXIMO_ARQUIVO) {
        marcarErro('arquivo', 'O arquivo tem ' + formatarTamanho(arquivo.size) + '. O máximo é 20 MB.');
        valido = false;
    }
    if (arquivo && arquivo.size === 0) {
        marcarErro('arquivo', 'O arquivo escolhido está vazio.');
        valido = false;
    }
    return valido;
}

async function salvarDocumento(evento) {
    evento.preventDefault();
    const botao = elemento('btnSalvarDocumento');
    if (botao.disabled) return;
    limparErrosFormulario();

    const dados = montarDados();
    const arquivo = elemento('inputArquivo').files[0] || null;
    if (!validarFormulario(dados, arquivo)) return;

    botao.disabled = true;
    const textoOriginal = botao.textContent;
    if (arquivo) botao.textContent = 'Enviando...';
    try {
        if (documentoEmEdicaoId !== null) {
            await atualizarDocumento(documentoEmEdicaoId, dados, arquivo);
        } else {
            await criarDocumento(dados, arquivo);
        }
        fecharModalFormulario();
        exibirMensagem('Documento salvo com sucesso.', 'sucesso');
        // Mostra o registro salvo mesmo quando havia pesquisa ou filtros ativos.
        elemento('inputPesquisa').value = '';
        elemento('selectFiltroTipo').value = '';
        elemento('selectFiltroValidade').value = '';
        clearTimeout(temporizadorPesquisa);
        await carregarDocumentos();
    } catch (erro) {
        let marcouCampo = false;
        Object.entries(erro.erros || {}).forEach(function ([campo, mensagem]) {
            if (marcarErro(campo, mensagem)) marcouCampo = true;
        });
        if (!marcouCampo) {
            exibirMensagem(erro.mensagem || 'Não foi possível salvar o documento.', 'erro');
        }
    } finally {
        botao.disabled = false;
        botao.textContent = textoOriginal;
    }
}

function marcarErro(campo, mensagem) {
    if (!errosDocumento[campo]) return false;
    elemento(errosDocumento[campo]).textContent = mensagem;
    return true;
}

function limparErrosFormulario() {
    document.querySelectorAll('#formularioDocumento .mensagem-erro-campo').forEach(function (campo) {
        campo.textContent = '';
    });
}

/* ---------- Exclusão ---------- */

function abrirModalExclusao(id, titulo, possuiArquivo) {
    documentoParaExcluirId = id;
    elemento('textoConfirmarExclusao').textContent =
        'Deseja excluir o documento "' + titulo + '"?'
        + (possuiArquivo ? ' O arquivo anexado também será apagado.' : '')
        + ' Essa ação não pode ser desfeita.';
    elemento('modalConfirmarExclusao').showModal();
}

function fecharModalExclusao() {
    elemento('modalConfirmarExclusao').close();
}

async function confirmarExclusao() {
    const botao = elemento('btnConfirmarExclusao');
    if (botao.disabled || documentoParaExcluirId === null) return;
    botao.disabled = true;
    try {
        await excluirDocumento(documentoParaExcluirId);
        fecharModalExclusao();
        documentoParaExcluirId = null;
        exibirMensagem('Documento excluído com sucesso.', 'sucesso');
        await carregarDocumentos();
    } catch (erro) {
        exibirMensagem(erro.mensagem || 'Não foi possível excluir o documento.', 'erro');
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
