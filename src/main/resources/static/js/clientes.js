let clientesCarregados = [];
let clienteEmEdicaoId = null;
let clienteParaExcluirId = null;
let versaoCarregamento = 0;
let versaoEdicao = 0;
let temporizadorPesquisa;

const rotulosStatusCliente = {
    ATIVO: 'Ativo',
    INATIVO: 'Inativo',
    AGUARDANDO_RETORNO: 'Aguardando retorno'
};

const camposCliente = {
    nomeCompleto: 'inputNomeCompleto',
    tipoPessoa: 'selectTipoPessoa',
    cpfCnpj: 'inputCpfCnpj',
    telefone: 'inputTelefone',
    whatsapp: 'inputWhatsapp',
    email: 'inputEmail',
    endereco: 'inputEndereco',
    cidade: 'inputCidade',
    estado: 'inputEstado',
    observacoes: 'textareaObservacoes',
    status: 'selectStatus'
};

function elemento(id) {
    return document.getElementById(id);
}

document.addEventListener('DOMContentLoaded', function () {
    elemento('modalFormulario').addEventListener('close', function () {
        elemento('formularioCliente').reset();
        limparErrosFormulario();
        clienteEmEdicaoId = null;
        versaoEdicao++;
    });
    elemento('btnNovoCliente').addEventListener('click', abrirModalNovoCliente);
    elemento('btnFecharModalFormulario').addEventListener('click', fecharModalFormulario);
    elemento('btnCancelarFormulario').addEventListener('click', fecharModalFormulario);
    elemento('formularioCliente').addEventListener('submit', salvarCliente);
    elemento('btnFecharModalExclusao').addEventListener('click', fecharModalExclusao);
    elemento('btnCancelarExclusao').addEventListener('click', fecharModalExclusao);
    elemento('btnConfirmarExclusao').addEventListener('click', confirmarExclusao);
    elemento('selectFiltroStatus').addEventListener('change', renderizarClientes);
    elemento('inputPesquisa').addEventListener('input', function () {
        clearTimeout(temporizadorPesquisa);
        // Invalida também uma consulta que ainda esteja em andamento.
        versaoCarregamento++;
        temporizadorPesquisa = setTimeout(carregarClientes, 300);
    });
    elemento('selectTipoPessoa').addEventListener('change', aplicarMascaraDocumento);
    elemento('inputCpfCnpj').addEventListener('input', aplicarMascaraDocumento);
    ['inputTelefone', 'inputWhatsapp'].forEach(function (id) {
        elemento(id).addEventListener('input', function () {
            this.value = formatarTelefone(this.value);
        });
    });
    elemento('inputEstado').addEventListener('input', function () {
        this.value = this.value.replace(/[^a-z]/gi, '').toUpperCase().slice(0, 2);
    });
    carregarClientes();
});

async function carregarClientes() {
    const versaoAtual = ++versaoCarregamento;
    elemento('indicadorCarregamento').classList.remove('oculto');
    try {
        const lista = await listarClientes(elemento('inputPesquisa').value);
        if (versaoAtual !== versaoCarregamento) return;
        if (!Array.isArray(lista)) throw new Error('Resposta inválida ao listar clientes.');
        clientesCarregados = lista;
        renderizarClientes();
    } catch (erro) {
        if (versaoAtual !== versaoCarregamento) return;
        clientesCarregados = [];
        elemento('corpoTabelaClientes').replaceChildren();
        elemento('estadoVazioClientes').classList.add('oculto');
        exibirMensagem(erro.mensagem || erro.message || 'Não foi possível carregar os clientes.', 'erro');
    } finally {
        if (versaoAtual === versaoCarregamento) {
            elemento('indicadorCarregamento').classList.add('oculto');
        }
    }
}

function renderizarClientes() {
    const filtro = elemento('selectFiltroStatus').value;
    const lista = clientesCarregados.filter(function (cliente) {
        return !filtro || cliente.status === filtro;
    });
    const corpoTabela = elemento('corpoTabelaClientes');
    corpoTabela.replaceChildren();
    elemento('estadoVazioClientes').classList.toggle('oculto', lista.length > 0);

    lista.forEach(function (cliente) {
        const linha = document.createElement('tr');
        linha.appendChild(criarCelula('Nome', cliente.nomeCompleto));
        linha.appendChild(criarCelula('Tipo', cliente.tipoPessoa === 'JURIDICA' ? 'Jurídica' : 'Física'));
        linha.appendChild(criarCelula('CPF/CNPJ', formatarDocumento(cliente.cpfCnpj, cliente.tipoPessoa)));
        linha.appendChild(criarCelula('Telefone', formatarTelefone(cliente.telefone)));
        linha.appendChild(criarCelula('Cidade/UF', [cliente.cidade, cliente.estado].filter(Boolean).join('/')));

        const celulaStatus = criarCelula('Status', '');
        const selo = document.createElement('span');
        const classesStatus = { ATIVO: 'status-ativo', INATIVO: 'status-inativo', AGUARDANDO_RETORNO: 'status-aguardando' };
        selo.classList.add('selo-status');
        if (classesStatus[cliente.status]) selo.classList.add(classesStatus[cliente.status]);
        selo.textContent = rotulosStatusCliente[cliente.status] || cliente.status;
        celulaStatus.replaceChildren(selo);
        linha.appendChild(celulaStatus);
        linha.appendChild(criarCelulaAcoes(cliente));
        corpoTabela.appendChild(linha);
    });
}

function criarCelula(rotulo, valor) {
    const celula = document.createElement('td');
    celula.setAttribute('data-rotulo', rotulo);
    celula.textContent = valor || '—';
    return celula;
}

function criarCelulaAcoes(cliente) {
    const celula = document.createElement('td');
    celula.setAttribute('data-rotulo', 'Ações');
    const container = document.createElement('div');
    container.classList.add('acoes-tabela');
    const acoes = [
        ['Histórico', function () { window.location.href = 'atendimentos.html?clienteId=' + cliente.id; }],
        ['Tarefas', function () { window.location.href = 'tarefas.html?clienteId=' + cliente.id; }],
        ['Imóveis', function () { window.location.href = 'imoveis.html?clienteId=' + cliente.id; }],
        ['Processos', function () { window.location.href = 'processos.html?clienteId=' + cliente.id; }],
        ['Documentos', function () { window.location.href = 'documentos.html?clienteId=' + cliente.id; }],
        ['Editar', function () { abrirModalEdicaoCliente(cliente.id); }],
        ['Excluir', function () { abrirModalExclusao(cliente.id, cliente.nomeCompleto); }]
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

function abrirModalNovoCliente() {
    versaoEdicao++;
    clienteEmEdicaoId = null;
    elemento('formularioCliente').reset();
    limparErrosFormulario();
    elemento('tituloModalFormulario').textContent = 'Novo cliente';
    elemento('selectTipoPessoa').value = 'FISICA';
    elemento('selectStatus').value = 'ATIVO';
    aplicarMascaraDocumento();
    elemento('modalFormulario').showModal();
}

async function abrirModalEdicaoCliente(id) {
    const versaoAtual = ++versaoEdicao;
    try {
        const cliente = await buscarCliente(id);
        if (versaoAtual !== versaoEdicao) return;
        clienteEmEdicaoId = id;
        elemento('formularioCliente').reset();
        limparErrosFormulario();
        Object.entries(camposCliente).forEach(function ([campo, idCampo]) {
            elemento(idCampo).value = cliente[campo] ?? '';
        });
        aplicarMascaraDocumento();
        elemento('inputTelefone').value = formatarTelefone(cliente.telefone);
        elemento('inputWhatsapp').value = formatarTelefone(cliente.whatsapp);
        elemento('tituloModalFormulario').textContent = 'Editar cliente';
        elemento('modalFormulario').showModal();
    } catch (erro) {
        if (versaoAtual === versaoEdicao) {
            exibirMensagem(erro.mensagem || 'Não foi possível abrir o cliente.', 'erro');
        }
    }
}

function fecharModalFormulario() {
    elemento('modalFormulario').close();
}

async function salvarCliente(evento) {
    evento.preventDefault();
    const botao = elemento('btnSalvarCliente');
    if (botao.disabled) return;
    limparErrosFormulario();
    if (!elemento('formularioCliente').reportValidity()) return;

    const dados = {};
    Object.entries(camposCliente).forEach(function ([campo, idCampo]) {
        dados[campo] = elemento(idCampo).value.trim();
    });
    dados.cpfCnpj = somenteDigitos(dados.cpfCnpj);
    dados.telefone = somenteDigitos(dados.telefone);
    dados.whatsapp = somenteDigitos(dados.whatsapp);
    dados.estado = dados.estado.toUpperCase();
    const tamanhoDocumento = dados.tipoPessoa === 'JURIDICA' ? 14 : 11;
    if (!dados.nomeCompleto || dados.cpfCnpj.length !== tamanhoDocumento) {
        exibirMensagem('Informe o nome e um ' + (tamanhoDocumento === 14 ? 'CNPJ com 14' : 'CPF com 11') + ' dígitos.', 'erro');
        return;
    }

    botao.disabled = true;
    try {
        if (clienteEmEdicaoId !== null) {
            await atualizarCliente(clienteEmEdicaoId, dados);
        } else {
            await criarCliente(dados);
        }
        fecharModalFormulario();
        exibirMensagem('Cliente salvo com sucesso.', 'sucesso');
        // Mostra o registro salvo mesmo quando havia pesquisa ou filtro ativo.
        elemento('inputPesquisa').value = '';
        elemento('selectFiltroStatus').value = '';
        clearTimeout(temporizadorPesquisa);
        await carregarClientes();
    } catch (erro) {
        mostrarErrosFormulario(erro.erros);
        exibirMensagem(erro.mensagem || 'Não foi possível salvar o cliente.', 'erro');
    } finally {
        botao.disabled = false;
    }
}

function abrirModalExclusao(id, nome) {
    clienteParaExcluirId = id;
    elemento('textoConfirmarExclusao').textContent = 'Deseja excluir o cliente "' + nome + '"?';
    elemento('modalConfirmarExclusao').showModal();
}

function fecharModalExclusao() {
    elemento('modalConfirmarExclusao').close();
}

async function confirmarExclusao() {
    const botao = elemento('btnConfirmarExclusao');
    if (botao.disabled || clienteParaExcluirId === null) return;
    botao.disabled = true;
    try {
        await excluirCliente(clienteParaExcluirId);
        fecharModalExclusao();
        clienteParaExcluirId = null;
        exibirMensagem('Cliente excluído com sucesso.', 'sucesso');
        await carregarClientes();
    } catch (erro) {
        exibirMensagem(erro.mensagem || 'Não foi possível excluir o cliente.', 'erro');
    } finally {
        botao.disabled = false;
    }
}

function limparErrosFormulario() {
    document.querySelectorAll('#formularioCliente .mensagem-erro-campo').forEach(function (campo) {
        campo.textContent = '';
    });
}

function mostrarErrosFormulario(erros) {
    const camposErro = {
        nomeCompleto: 'erroNomeCompleto', tipoPessoa: 'erroTipoPessoa',
        cpfCnpj: 'erroCpfCnpj', email: 'erroEmail', status: 'erroStatus'
    };
    Object.entries(erros || {}).forEach(function ([campo, mensagem]) {
        if (camposErro[campo]) elemento(camposErro[campo]).textContent = mensagem;
    });
}

function somenteDigitos(valor) {
    return String(valor ?? '').replace(/\D/g, '');
}

function formatarDocumento(valor, tipo) {
    const digitos = somenteDigitos(valor).slice(0, tipo === 'JURIDICA' ? 14 : 11);
    if (tipo === 'JURIDICA') {
        return digitos.replace(/^(\d{2})(\d)/, '$1.$2')
            .replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3')
            .replace(/\.(\d{3})(\d)/, '.$1/$2')
            .replace(/(\d{4})(\d{1,2})$/, '$1-$2');
    }
    return digitos.replace(/^(\d{3})(\d)/, '$1.$2')
        .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
        .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
}

function aplicarMascaraDocumento() {
    const campo = elemento('inputCpfCnpj');
    const tipo = elemento('selectTipoPessoa').value;
    campo.maxLength = tipo === 'JURIDICA' ? 18 : 14;
    campo.value = formatarDocumento(campo.value, tipo);
}

function formatarTelefone(valor) {
    const digitos = somenteDigitos(valor).slice(0, 11);
    if (digitos.length <= 2) return digitos;
    const numero = digitos.slice(2);
    const tamanhoPrefixo = digitos.length === 11 ? 5 : 4;
    return '(' + digitos.slice(0, 2) + ') ' + numero.slice(0, tamanhoPrefixo)
        + (numero.length > tamanhoPrefixo ? '-' + numero.slice(tamanhoPrefixo) : '');
}

function exibirMensagem(texto, tipo) {
    const mensagem = document.createElement('div');
    mensagem.classList.add('mensagem-toast', tipo);
    mensagem.textContent = texto;
    elemento('containerMensagens').appendChild(mensagem);
    setTimeout(function () { mensagem.remove(); }, 5000);
}
