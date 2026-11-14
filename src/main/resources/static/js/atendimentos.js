const parametrosUrl = new URLSearchParams(window.location.search);
const clienteIdAtual = parametrosUrl.get('clienteId');

let atendimentoEmEdicaoId = null;
let atendimentoParaExcluirId = null;

const tituloAtendimentos = document.getElementById('tituloAtendimentos');
const listaAtendimentos = document.getElementById('listaAtendimentos');
const estadoVazioAtendimentos = document.getElementById('estadoVazioAtendimentos');
const indicadorCarregamento = document.getElementById('indicadorCarregamento');
const btnNovoAtendimento = document.getElementById('btnNovoAtendimento');

const modalFormularioAtendimento = document.getElementById('modalFormularioAtendimento');
modalFormularioAtendimento.addEventListener('close', function () {
    limparFormularioAtendimento();
    atendimentoEmEdicaoId = null;
});

const formularioAtendimento = document.getElementById('formularioAtendimento');
const tituloModalAtendimento = document.getElementById('tituloModalAtendimento');
const btnFecharModalAtendimento = document.getElementById('btnFecharModalAtendimento');
const btnCancelarAtendimento = document.getElementById('btnCancelarAtendimento');
const btnSalvarAtendimento = document.getElementById('btnSalvarAtendimento');

const modalConfirmarExclusaoAtendimento = document.getElementById('modalConfirmarExclusaoAtendimento');
const textoConfirmarExclusaoAtendimento = document.getElementById('textoConfirmarExclusaoAtendimento');
const btnFecharModalExclusaoAtendimento = document.getElementById('btnFecharModalExclusaoAtendimento');
const btnCancelarExclusaoAtendimento = document.getElementById('btnCancelarExclusaoAtendimento');
const btnConfirmarExclusaoAtendimento = document.getElementById('btnConfirmarExclusaoAtendimento');

const inputDataHora = document.getElementById('inputDataHora');
const selectCanal = document.getElementById('selectCanal');
const inputAssunto = document.getElementById('inputAssunto');
const textareaDescricao = document.getElementById('textareaDescricao');
const inputProximoContato = document.getElementById('inputProximoContato');

const rotulosCanal = {
    WHATSAPP: 'WhatsApp',
    TELEFONE: 'Telefone',
    PRESENCIAL: 'Presencial',
    EMAIL: 'E-mail'
};

document.addEventListener('DOMContentLoaded', function () {
    if (!clienteIdAtual) {
        btnNovoAtendimento.disabled = true;
        exibirMensagem('Nenhum cliente informado. Volte para a lista de clientes.', 'erro');
        return;
    }

    carregarNomeCliente();
    carregarAtendimentos();

    btnNovoAtendimento.addEventListener('click', abrirModalNovoAtendimento);
    btnFecharModalAtendimento.addEventListener('click', fecharModalFormularioAtendimento);
    btnCancelarAtendimento.addEventListener('click', fecharModalFormularioAtendimento);
    formularioAtendimento.addEventListener('submit', salvarAtendimento);

    btnFecharModalExclusaoAtendimento.addEventListener('click', fecharModalExclusaoAtendimento);
    btnCancelarExclusaoAtendimento.addEventListener('click', fecharModalExclusaoAtendimento);
    btnConfirmarExclusaoAtendimento.addEventListener('click', confirmarExclusaoAtendimento);
});

function carregarNomeCliente() {
    buscarCliente(clienteIdAtual)
        .then(function (cliente) {
            tituloAtendimentos.textContent = 'Atendimentos de ' + cliente.nomeCompleto;
        })
        .catch(function () {
            tituloAtendimentos.textContent = 'Atendimentos';
        });
}

function mostrarCarregamento(mostrar) {
    indicadorCarregamento.classList.toggle('oculto', !mostrar);
}

function carregarAtendimentos() {
    mostrarCarregamento(true);

    listarAtendimentosPorCliente(clienteIdAtual)
        .then(function (lista) {
            renderizarLista(lista);
        })
        .catch(function (erro) {
            exibirMensagem(erro.mensagem || 'Não foi possível carregar os atendimentos.', 'erro');
        })
        .finally(function () {
            mostrarCarregamento(false);
        });
}

function renderizarLista(lista) {
    listaAtendimentos.textContent = '';

    if (!lista || lista.length === 0) {
        estadoVazioAtendimentos.classList.remove('oculto');
        return;
    }

    estadoVazioAtendimentos.classList.add('oculto');

    lista.forEach(function (atendimento) {
        listaAtendimentos.appendChild(criarCartaoAtendimento(atendimento));
    });
}

function criarCartaoAtendimento(atendimento) {
    const cartao = document.createElement('article');
    cartao.classList.add('cartao-atendimento');

    const cabecalho = document.createElement('div');
    cabecalho.classList.add('cartao-atendimento-cabecalho');

    const dataHora = document.createElement('span');
    dataHora.classList.add('cartao-atendimento-data');
    dataHora.textContent = formatarDataHora(atendimento.dataHora);

    const selo = document.createElement('span');
    selo.classList.add('selo-canal', 'canal-' + atendimento.canal.toLowerCase());
    selo.textContent = rotulosCanal[atendimento.canal] || atendimento.canal;

    cabecalho.appendChild(dataHora);
    cabecalho.appendChild(selo);

    const assunto = document.createElement('h3');
    assunto.classList.add('cartao-atendimento-assunto');
    assunto.textContent = atendimento.assunto;

    const descricao = document.createElement('p');
    descricao.classList.add('cartao-atendimento-descricao');
    descricao.textContent = atendimento.descricao;

    cartao.appendChild(cabecalho);
    cartao.appendChild(assunto);
    cartao.appendChild(descricao);

    if (atendimento.proximoContato) {
        const proximoContato = document.createElement('span');
        proximoContato.classList.add('chip-proximo-contato');
        proximoContato.textContent = 'Próximo contato: ' + formatarData(atendimento.proximoContato);
        cartao.appendChild(proximoContato);
    }

    const acoes = document.createElement('div');
    acoes.classList.add('acoes-tabela');

    const botaoEditar = document.createElement('button');
    botaoEditar.type = 'button';
    botaoEditar.classList.add('botao-icone');
    botaoEditar.textContent = 'Editar';
    botaoEditar.addEventListener('click', function () {
        abrirModalEdicaoAtendimento(atendimento.id);
    });

    const botaoExcluir = document.createElement('button');
    botaoExcluir.type = 'button';
    botaoExcluir.classList.add('botao-icone', 'excluir');
    botaoExcluir.textContent = 'Excluir';
    botaoExcluir.addEventListener('click', function () {
        abrirModalExclusaoAtendimento(atendimento.id, atendimento.assunto);
    });

    acoes.appendChild(botaoEditar);
    acoes.appendChild(botaoExcluir);
    cartao.appendChild(acoes);

    return cartao;
}

function formatarDataHora(valor) {
    const data = new Date(valor);
    return data.toLocaleDateString('pt-BR') + ' às ' + data.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
}

function formatarData(valor) {
    const partes = valor.split('-');
    return partes[2] + '/' + partes[1] + '/' + partes[0];
}

function abrirModalNovoAtendimento() {
    atendimentoEmEdicaoId = null;
    tituloModalAtendimento.textContent = 'Novo atendimento';
    limparFormularioAtendimento();
    modalFormularioAtendimento.showModal();
    inputDataHora.focus();
}

function abrirModalEdicaoAtendimento(id) {
    mostrarCarregamento(true);

    buscarAtendimento(id)
        .then(function (atendimento) {
            atendimentoEmEdicaoId = atendimento.id;
            tituloModalAtendimento.textContent = 'Editar atendimento';
            preencherFormularioAtendimento(atendimento);
            modalFormularioAtendimento.showModal();
            inputDataHora.focus();
        })
        .catch(function (erro) {
            exibirMensagem(erro.mensagem || 'Não foi possível carregar o atendimento.', 'erro');
        })
        .finally(function () {
            mostrarCarregamento(false);
        });
}

function preencherFormularioAtendimento(atendimento) {
    inputDataHora.value = atendimento.dataHora ? atendimento.dataHora.slice(0, 16) : '';
    selectCanal.value = atendimento.canal || '';
    inputAssunto.value = atendimento.assunto || '';
    textareaDescricao.value = atendimento.descricao || '';
    inputProximoContato.value = atendimento.proximoContato || '';
}

function limparFormularioAtendimento() {
    formularioAtendimento.reset();
    limparErrosFormularioAtendimento();
}

function fecharModalFormularioAtendimento() {
    modalFormularioAtendimento.close();
    limparFormularioAtendimento();
    atendimentoEmEdicaoId = null;
}

function limparErrosFormularioAtendimento() {
    document.querySelectorAll('#formularioAtendimento .mensagem-erro-campo').forEach(function (elemento) {
        elemento.textContent = '';
    });
    document.querySelectorAll('#formularioAtendimento .campo-formulario.com-erro').forEach(function (elemento) {
        elemento.classList.remove('com-erro');
    });
}

function validarFormularioAtendimento() {
    limparErrosFormularioAtendimento();
    let valido = true;

    if (!inputDataHora.value) {
        marcarCampoComErroAtendimento('erroDataHora', inputDataHora, 'Informe a data e hora');
        valido = false;
    }

    if (!selectCanal.value) {
        marcarCampoComErroAtendimento('erroCanal', selectCanal, 'Selecione o canal');
        valido = false;
    }

    if (!inputAssunto.value.trim()) {
        marcarCampoComErroAtendimento('erroAssunto', inputAssunto, 'O assunto é obrigatório');
        valido = false;
    }

    if (!textareaDescricao.value.trim()) {
        marcarCampoComErroAtendimento('erroDescricao', textareaDescricao, 'A descrição é obrigatória');
        valido = false;
    }

    return valido;
}

function marcarCampoComErroAtendimento(idSpanErro, campo, mensagem) {
    document.getElementById(idSpanErro).textContent = mensagem;
    campo.closest('.campo-formulario').classList.add('com-erro');
}

function montarDadosFormularioAtendimento() {
    return {
        clienteId: Number(clienteIdAtual),
        dataHora: inputDataHora.value,
        canal: selectCanal.value,
        assunto: inputAssunto.value.trim(),
        descricao: textareaDescricao.value.trim(),
        proximoContato: inputProximoContato.value || null
    };
}

function salvarAtendimento(evento) {
    evento.preventDefault();

    if (!validarFormularioAtendimento()) {
        return;
    }

    const dados = montarDadosFormularioAtendimento();
    btnSalvarAtendimento.disabled = true;
    btnCancelarAtendimento.disabled = true;

    const operacao = atendimentoEmEdicaoId
        ? atualizarAtendimento(atendimentoEmEdicaoId, dados)
        : criarAtendimento(dados);

    operacao
        .then(function () {
            exibirMensagem(atendimentoEmEdicaoId ? 'Atendimento atualizado com sucesso.' : 'Atendimento registrado com sucesso.', 'sucesso');
            fecharModalFormularioAtendimento();
            carregarAtendimentos();
        })
        .catch(function (erro) {
            exibirMensagem(erro.mensagem || 'Não foi possível salvar o atendimento.', 'erro');
        })
        .finally(function () {
            btnSalvarAtendimento.disabled = false;
            btnCancelarAtendimento.disabled = false;
        });
}

function abrirModalExclusaoAtendimento(id, assunto) {
    atendimentoParaExcluirId = id;
    textoConfirmarExclusaoAtendimento.textContent = 'Deseja realmente excluir o atendimento "' + assunto + '"? Essa ação não pode ser desfeita.';
    modalConfirmarExclusaoAtendimento.showModal();
}

function fecharModalExclusaoAtendimento() {
    modalConfirmarExclusaoAtendimento.close();
    atendimentoParaExcluirId = null;
}

function confirmarExclusaoAtendimento() {
    if (!atendimentoParaExcluirId) {
        return;
    }

    btnConfirmarExclusaoAtendimento.disabled = true;
    btnCancelarExclusaoAtendimento.disabled = true;

    excluirAtendimento(atendimentoParaExcluirId)
        .then(function () {
            exibirMensagem('Atendimento excluído com sucesso.', 'sucesso');
            fecharModalExclusaoAtendimento();
            carregarAtendimentos();
        })
        .catch(function (erro) {
            exibirMensagem(erro.mensagem || 'Não foi possível excluir o atendimento.', 'erro');
        })
        .finally(function () {
            btnConfirmarExclusaoAtendimento.disabled = false;
            btnCancelarExclusaoAtendimento.disabled = false;
        });
}

function exibirMensagem(texto, tipo) {
    const container = document.getElementById('containerMensagens');

    const toast = document.createElement('div');
    toast.classList.add('mensagem-toast', tipo || 'sucesso');
    toast.textContent = texto;

    container.appendChild(toast);

    setTimeout(function () {
        toast.remove();
    }, 4000);
}