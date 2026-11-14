// Página de imóveis. Aceita ?clienteId=N na URL para já abrir filtrada por um cliente.
const parametrosUrlImoveis = new URLSearchParams(window.location.search);

let imovelEmEdicaoId = null;
let imovelParaExcluirId = null;
let versaoCarregamento = 0;
let versaoEdicao = 0;
let temporizadorPesquisa;

const rotulosGeorreferenciamento = {
    NAO_INICIADO: 'Não iniciado',
    EM_ANDAMENTO: 'Em andamento',
    CERTIFICADO: 'Certificado'
};

// Nome do campo na API -> id do campo na tela
const camposImovel = {
    denominacao: 'inputDenominacao',
    clienteId: 'selectCliente',
    municipio: 'inputMunicipio',
    estado: 'inputEstado',
    areaHectares: 'inputAreaHectares',
    situacaoGeorreferenciamento: 'selectSituacaoGeorreferenciamento',
    matricula: 'inputMatricula',
    cartorio: 'inputCartorio',
    codigoCar: 'inputCodigoCar',
    codigoCcir: 'inputCodigoCcir',
    nirf: 'inputNirf',
    observacoes: 'textareaObservacoes'
};

// Nome do campo na API -> id do <span> de erro
const errosImovel = {
    denominacao: 'erroDenominacao',
    clienteId: 'erroClienteId',
    municipio: 'erroMunicipio',
    estado: 'erroEstado',
    areaHectares: 'erroAreaHectares',
    situacaoGeorreferenciamento: 'erroSituacaoGeorreferenciamento',
    matricula: 'erroMatricula',
    cartorio: 'erroCartorio',
    codigoCar: 'erroCodigoCar',
    codigoCcir: 'erroCodigoCcir',
    nirf: 'erroNirf',
    observacoes: 'erroObservacoes'
};

function elemento(id) {
    return document.getElementById(id);
}

document.addEventListener('DOMContentLoaded', async function () {
    elemento('modalFormulario').addEventListener('close', function () {
        elemento('formularioImovel').reset();
        limparErrosFormulario();
        imovelEmEdicaoId = null;
        versaoEdicao++;
    });
    elemento('btnNovoImovel').addEventListener('click', abrirModalNovoImovel);
    elemento('btnFecharModalFormulario').addEventListener('click', fecharModalFormulario);
    elemento('btnCancelarFormulario').addEventListener('click', fecharModalFormulario);
    elemento('formularioImovel').addEventListener('submit', salvarImovel);
    elemento('btnFecharModalExclusao').addEventListener('click', fecharModalExclusao);
    elemento('btnCancelarExclusao').addEventListener('click', fecharModalExclusao);
    elemento('btnConfirmarExclusao').addEventListener('click', confirmarExclusao);

    elemento('selectFiltroCliente').addEventListener('change', function () {
        atualizarUrlComCliente(this.value);
        carregarImoveis();
    });
    elemento('inputPesquisa').addEventListener('input', function () {
        clearTimeout(temporizadorPesquisa);
        // Invalida também uma consulta que ainda esteja em andamento.
        versaoCarregamento++;
        temporizadorPesquisa = setTimeout(carregarImoveis, 300);
    });
    elemento('inputEstado').addEventListener('input', function () {
        this.value = this.value.replace(/[^a-z]/gi, '').toUpperCase().slice(0, 2);
    });
    elemento('inputAreaHectares').addEventListener('input', function () {
        // Só números e uma vírgula (ou ponto) decimal
        this.value = this.value.replace(/[^\d,.]/g, '');
    });

    await carregarClientes();
    carregarImoveis();
});

/* ---------- Clientes (filtro e formulário) ---------- */

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

        const clienteDaUrl = parametrosUrlImoveis.get('clienteId');
        if (clienteDaUrl && ordenados.some(function (c) { return String(c.id) === clienteDaUrl; })) {
            elemento('selectFiltroCliente').value = clienteDaUrl;
        }
    } catch (erro) {
        exibirMensagem('Não foi possível carregar a lista de clientes.', 'aviso');
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

async function carregarImoveis() {
    const versaoAtual = ++versaoCarregamento;
    elemento('indicadorCarregamento').classList.remove('oculto');
    try {
        const lista = await listarImoveis(elemento('selectFiltroCliente').value, elemento('inputPesquisa').value);
        if (versaoAtual !== versaoCarregamento) return;
        if (!Array.isArray(lista)) throw new Error('Resposta inválida ao listar imóveis.');
        renderizarImoveis(lista);
    } catch (erro) {
        if (versaoAtual !== versaoCarregamento) return;
        elemento('corpoTabelaImoveis').replaceChildren();
        elemento('estadoVazioImoveis').classList.add('oculto');
        exibirMensagem(erro.mensagem || erro.message || 'Não foi possível carregar os imóveis.', 'erro');
    } finally {
        if (versaoAtual === versaoCarregamento) {
            elemento('indicadorCarregamento').classList.add('oculto');
        }
    }
}

function renderizarImoveis(lista) {
    const corpoTabela = elemento('corpoTabelaImoveis');
    corpoTabela.replaceChildren();
    elemento('estadoVazioImoveis').classList.toggle('oculto', lista.length > 0);

    lista.forEach(function (imovel) {
        const linha = document.createElement('tr');

        // Nome do imóvel com os códigos principais logo abaixo
        const celulaImovel = criarCelula('Imóvel', imovel.denominacao);
        const codigos = [
            imovel.codigoCar ? 'CAR ' + imovel.codigoCar : null,
            imovel.codigoCcir ? 'CCIR ' + imovel.codigoCcir : null,
            imovel.nirf ? 'NIRF ' + imovel.nirf : null
        ].filter(Boolean);
        if (codigos.length > 0) {
            const detalhe = document.createElement('span');
            detalhe.classList.add('imovel-codigos');
            detalhe.textContent = codigos.join(' · ');
            celulaImovel.appendChild(detalhe);
        }
        linha.appendChild(celulaImovel);

        linha.appendChild(criarCelula('Cliente', imovel.nomeCliente));
        linha.appendChild(criarCelula('Município/UF', [imovel.municipio, imovel.estado].filter(Boolean).join('/')));
        linha.appendChild(criarCelula('Área (ha)', formatarArea(imovel.areaHectares)));
        linha.appendChild(criarCelula('Matrícula', imovel.matricula));

        const celulaGeo = criarCelula('Georreferenciamento', '');
        const selo = document.createElement('span');
        selo.classList.add('selo-geo', 'geo-' + String(imovel.situacaoGeorreferenciamento).toLowerCase());
        selo.textContent = rotulosGeorreferenciamento[imovel.situacaoGeorreferenciamento] || imovel.situacaoGeorreferenciamento;
        celulaGeo.replaceChildren(selo);
        linha.appendChild(celulaGeo);

        linha.appendChild(criarCelulaAcoes(imovel));
        corpoTabela.appendChild(linha);
    });
}

function criarCelula(rotulo, valor) {
    const celula = document.createElement('td');
    celula.setAttribute('data-rotulo', rotulo);
    celula.textContent = valor || '—';
    return celula;
}

function criarCelulaAcoes(imovel) {
    const celula = document.createElement('td');
    celula.setAttribute('data-rotulo', 'Ações');
    const container = document.createElement('div');
    container.classList.add('acoes-tabela');
    const acoes = [
        ['Editar', function () { abrirModalEdicaoImovel(imovel.id); }],
        ['Excluir', function () { abrirModalExclusao(imovel.id, imovel.denominacao); }]
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

/* ---------- Área em hectares ---------- */

// 125.5 -> "125,5" (até 4 casas, sem zeros sobrando)
function formatarArea(valor) {
    if (valor === null || valor === undefined || valor === '') return '';
    return Number(valor).toLocaleString('pt-BR', { maximumFractionDigits: 4 });
}

// Para o campo do formulário: 125.5 -> "125,5" sem separador de milhar
function areaParaCampo(valor) {
    if (valor === null || valor === undefined) return '';
    return String(Number(valor)).replace('.', ',');
}

// "1.250,5" ou "1250,5" ou "1250.5" -> 1250.5. Devolve null se vazio e NaN se inválido.
function lerArea(texto) {
    let limpo = String(texto || '').trim();
    if (!limpo) return null;
    if (limpo.includes(',')) {
        limpo = limpo.replace(/\./g, '').replace(',', '.');
    }
    if (!/^\d+(\.\d{1,4})?$/.test(limpo)) return NaN;
    return Number(limpo);
}

/* ---------- Formulário ---------- */

function abrirModalNovoImovel() {
    versaoEdicao++;
    imovelEmEdicaoId = null;
    elemento('formularioImovel').reset();
    limparErrosFormulario();
    elemento('tituloModalFormulario').textContent = 'Novo imóvel';
    elemento('selectSituacaoGeorreferenciamento').value = 'NAO_INICIADO';
    // Se a lista está filtrada por um cliente, já deixa ele selecionado
    elemento('selectCliente').value = elemento('selectFiltroCliente').value;
    elemento('modalFormulario').showModal();
    elemento('inputDenominacao').focus();
}

async function abrirModalEdicaoImovel(id) {
    const versaoAtual = ++versaoEdicao;
    try {
        const imovel = await buscarImovel(id);
        if (versaoAtual !== versaoEdicao) return;
        imovelEmEdicaoId = id;
        elemento('formularioImovel').reset();
        limparErrosFormulario();
        Object.entries(camposImovel).forEach(function ([campo, idCampo]) {
            elemento(idCampo).value = imovel[campo] ?? '';
        });
        elemento('selectCliente').value = String(imovel.clienteId);
        elemento('inputAreaHectares').value = areaParaCampo(imovel.areaHectares);
        elemento('tituloModalFormulario').textContent = 'Editar imóvel';
        elemento('modalFormulario').showModal();
    } catch (erro) {
        if (versaoAtual === versaoEdicao) {
            exibirMensagem(erro.mensagem || 'Não foi possível abrir o imóvel.', 'erro');
        }
    }
}

function fecharModalFormulario() {
    elemento('modalFormulario').close();
}

function validarFormulario(dados) {
    let valido = true;
    if (!dados.denominacao) {
        marcarErro('denominacao', 'Informe a denominação do imóvel');
        valido = false;
    }
    if (!dados.clienteId) {
        marcarErro('clienteId', 'Selecione o cliente');
        valido = false;
    }
    if (!dados.municipio) {
        marcarErro('municipio', 'Informe o município');
        valido = false;
    }
    if (!/^[A-Z]{2}$/.test(dados.estado)) {
        marcarErro('estado', 'Informe a UF');
        valido = false;
    }
    if (Number.isNaN(dados.areaHectares) || dados.areaHectares === 0) {
        marcarErro('areaHectares', 'Informe um número maior que zero, com até 4 casas decimais');
        valido = false;
    }
    return valido;
}

function montarDados() {
    const dados = {};
    Object.entries(camposImovel).forEach(function ([campo, idCampo]) {
        const valor = elemento(idCampo).value.trim();
        dados[campo] = valor === '' ? null : valor;
    });
    dados.denominacao = dados.denominacao || '';
    dados.municipio = dados.municipio || '';
    dados.estado = (dados.estado || '').toUpperCase();
    dados.clienteId = dados.clienteId ? Number(dados.clienteId) : null;
    dados.areaHectares = lerArea(elemento('inputAreaHectares').value);
    return dados;
}

async function salvarImovel(evento) {
    evento.preventDefault();
    const botao = elemento('btnSalvarImovel');
    if (botao.disabled) return;
    limparErrosFormulario();

    const dados = montarDados();
    if (!validarFormulario(dados)) return;

    botao.disabled = true;
    try {
        if (imovelEmEdicaoId !== null) {
            await atualizarImovel(imovelEmEdicaoId, dados);
        } else {
            await criarImovel(dados);
        }
        fecharModalFormulario();
        exibirMensagem('Imóvel salvo com sucesso.', 'sucesso');
        // Mostra o registro salvo mesmo quando havia pesquisa ativa.
        elemento('inputPesquisa').value = '';
        clearTimeout(temporizadorPesquisa);
        await carregarImoveis();
    } catch (erro) {
        let marcouCampo = false;
        Object.entries(erro.erros || {}).forEach(function ([campo, mensagem]) {
            if (marcarErro(campo, mensagem)) marcouCampo = true;
        });
        if (!marcouCampo) {
            exibirMensagem(erro.mensagem || 'Não foi possível salvar o imóvel.', 'erro');
        }
    } finally {
        botao.disabled = false;
    }
}

function marcarErro(campo, mensagem) {
    if (!errosImovel[campo]) return false;
    elemento(errosImovel[campo]).textContent = mensagem;
    return true;
}

function limparErrosFormulario() {
    document.querySelectorAll('#formularioImovel .mensagem-erro-campo').forEach(function (campo) {
        campo.textContent = '';
    });
}

/* ---------- Exclusão ---------- */

function abrirModalExclusao(id, denominacao) {
    imovelParaExcluirId = id;
    elemento('textoConfirmarExclusao').textContent =
        'Deseja excluir o imóvel "' + denominacao + '"? Essa ação não pode ser desfeita.';
    elemento('modalConfirmarExclusao').showModal();
}

function fecharModalExclusao() {
    elemento('modalConfirmarExclusao').close();
}

async function confirmarExclusao() {
    const botao = elemento('btnConfirmarExclusao');
    if (botao.disabled || imovelParaExcluirId === null) return;
    botao.disabled = true;
    try {
        await excluirImovel(imovelParaExcluirId);
        fecharModalExclusao();
        imovelParaExcluirId = null;
        exibirMensagem('Imóvel excluído com sucesso.', 'sucesso');
        await carregarImoveis();
    } catch (erro) {
        exibirMensagem(erro.mensagem || 'Não foi possível excluir o imóvel.', 'erro');
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
