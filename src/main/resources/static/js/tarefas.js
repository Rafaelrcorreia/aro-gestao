const parametrosUrlTarefas = new URLSearchParams(window.location.search);
const clienteIdAtualTarefas = parametrosUrlTarefas.get('clienteId');

let tarefaEmEdicaoId = null;
let tarefaParaExcluirId = null;

const tituloTarefas = document.getElementById('tituloTarefas');
const listaTarefas = document.getElementById('listaTarefas');
const estadoVazioTarefas = document.getElementById('estadoVazioTarefas');
const indicadorCarregamento = document.getElementById('indicadorCarregamento');
const btnNovaTarefa = document.getElementById('btnNovaTarefa');

const modalFormularioTarefa = document.getElementById('modalFormularioTarefa');
modalFormularioTarefa.addEventListener('close', function () {
    limparFormularioTarefa();
    tarefaEmEdicaoId = null;
});

const formularioTarefa = document.getElementById('formularioTarefa');
const tituloModalTarefa = document.getElementById('tituloModalTarefa');
const btnFecharModalTarefa = document.getElementById('btnFecharModalTarefa');
const btnCancelarTarefa = document.getElementById('btnCancelarTarefa');
const btnSalvarTarefa = document.getElementById('btnSalvarTarefa');

const modalConfirmarExclusaoTarefa = document.getElementById('modalConfirmarExclusaoTarefa');
const textoConfirmarExclusaoTarefa = document.getElementById('textoConfirmarExclusaoTarefa');
const btnFecharModalExclusaoTarefa = document.getElementById('btnFecharModalExclusaoTarefa');
const btnCancelarExclusaoTarefa = document.getElementById('btnCancelarExclusaoTarefa');
const btnConfirmarExclusaoTarefa = document.getElementById('btnConfirmarExclusaoTarefa');

const inputTitulo = document.getElementById('inputTitulo');
const textareaDescricaoTarefa = document.getElementById('textareaDescricaoTarefa');
const inputResponsavel = document.getElementById('inputResponsavel');
const selectPrioridade = document.getElementById('selectPrioridade');
const inputPrazo = document.getElementById('inputPrazo');
const selectStatusTarefa = document.getElementById('selectStatusTarefa');

const rotulosPrioridade = {
    BAIXA: 'Baixa',
    MEDIA: 'Média',
    ALTA: 'Alta'
};

const rotulosStatusTarefa = {
    PENDENTE: 'Pendente',
    EM_ANDAMENTO: 'Em andamento',
    AGUARDANDO_CLIENTE: 'Aguardando cliente',
    CONCLUIDA: 'Concluída',
    ATRASADA: 'Atrasada'
};

document.addEventListener('DOMContentLoaded', function () {
    if (!clienteIdAtualTarefas) {
        btnNovaTarefa.disabled = true;
        exibirMensagem('Nenhum cliente informado. Volte para a lista de clientes.', 'erro');
        return;
    }

    carregarNomeClienteTarefas();
    carregarTarefas();

    btnNovaTarefa.addEventListener('click', abrirModalNovaTarefa);
    btnFecharModalTarefa.addEventListener('click', fecharModalFormularioTarefa);
    btnCancelarTarefa.addEventListener('click', fecharModalFormularioTarefa);
    formularioTarefa.addEventListener('submit', salvarTarefa);

    btnFecharModalExclusaoTarefa.addEventListener('click', fecharModalExclusaoTarefa);
    btnCancelarExclusaoTarefa.addEventListener('click', fecharModalExclusaoTarefa);
    btnConfirmarExclusaoTarefa.addEventListener('click', confirmarExclusaoTarefa);
});

function carregarNomeClienteTarefas() {
    buscarCliente(clienteIdAtualTarefas)
        .then(function (cliente) {
            tituloTarefas.textContent = 'Tarefas de ' + cliente.nomeCompleto;
        })
        .catch(function () {
            tituloTarefas.textContent = 'Tarefas';
        });
}

function mostrarCarregamento(mostrar) {
    indicadorCarregamento.classList.toggle('oculto', !mostrar);
}

function carregarTarefas() {
    mostrarCarregamento(true);

    listarTarefasPorCliente(clienteIdAtualTarefas)
        .then(function (lista) {
            renderizarListaTarefas(lista);
        })
        .catch(function (erro) {
            exibirMensagem(erro.mensagem || 'Não foi possível carregar as tarefas.', 'erro');
        })
        .finally(function () {
            mostrarCarregamento(false);
        });
}

function renderizarListaTarefas(lista) {
    listaTarefas.textContent = '';

    if (!lista || lista.length === 0) {
        estadoVazioTarefas.classList.remove('oculto');
        return;
    }

    estadoVazioTarefas.classList.add('oculto');

    lista.forEach(function (tarefa) {
        listaTarefas.appendChild(criarCartaoTarefa(tarefa));
    });
}

function criarCartaoTarefa(tarefa) {
    const cartao = document.createElement('article');
    cartao.classList.add('cartao-tarefa');

    if (tarefa.atrasada) {
        cartao.classList.add('tarefa-atrasada');
    }

    const cabecalho = document.createElement('div');
    cabecalho.classList.add('cartao-atendimento-cabecalho');

    const grupoSelos = document.createElement('div');
    grupoSelos.classList.add('grupo-selos-tarefa');

    const seloPrioridade = document.createElement('span');
    seloPrioridade.classList.add('selo-prioridade', 'prioridade-' + tarefa.prioridade.toLowerCase());
    seloPrioridade.textContent = rotulosPrioridade[tarefa.prioridade] || tarefa.prioridade;

    const seloStatus = document.createElement('span');
    seloStatus.classList.add('selo-status-tarefa', 'status-tarefa-' + tarefa.status.toLowerCase());
    seloStatus.textContent = rotulosStatusTarefa[tarefa.status] || tarefa.status;

    grupoSelos.appendChild(seloPrioridade);
    grupoSelos.appendChild(seloStatus);

    if (tarefa.atrasada) {
        const seloAtrasada = document.createElement('span');
        seloAtrasada.classList.add('selo-status-tarefa', 'status-tarefa-atrasada');
        seloAtrasada.textContent = 'Prazo vencido';
        grupoSelos.appendChild(seloAtrasada);
    }

    const prazo = document.createElement('span');
    prazo.classList.add('cartao-atendimento-data');
    prazo.textContent = 'Prazo: ' + formatarDataTarefa(tarefa.prazo);

    cabecalho.appendChild(grupoSelos);
    cabecalho.appendChild(prazo);

    const titulo = document.createElement('h3');
    titulo.classList.add('cartao-atendimento-assunto');
    titulo.textContent = tarefa.titulo;

    const responsavel = document.createElement('p');
    responsavel.classList.add('cartao-tarefa-responsavel');
    responsavel.textContent = 'Responsável: ' + tarefa.responsavel;

    cartao.appendChild(cabecalho);
    cartao.appendChild(titulo);
    cartao.appendChild(responsavel);

    if (tarefa.descricao) {
        const descricao = document.createElement('p');
        descricao.classList.add('cartao-atendimento-descricao');
        descricao.textContent = tarefa.descricao;
        cartao.appendChild(descricao);
    }

    cartao.appendChild(criarSecaoChecklist(tarefa));

    const acoes = document.createElement('div');
    acoes.classList.add('acoes-tabela');

    const botaoEditar = document.createElement('button');
    botaoEditar.type = 'button';
    botaoEditar.classList.add('botao-icone');
    botaoEditar.textContent = 'Editar';
    botaoEditar.addEventListener('click', function () {
        abrirModalEdicaoTarefa(tarefa.id);
    });

    const botaoExcluir = document.createElement('button');
    botaoExcluir.type = 'button';
    botaoExcluir.classList.add('botao-icone', 'excluir');
    botaoExcluir.textContent = 'Excluir';
    botaoExcluir.addEventListener('click', function () {
        abrirModalExclusaoTarefa(tarefa.id, tarefa.titulo);
    });

    acoes.appendChild(botaoEditar);
    acoes.appendChild(botaoExcluir);
    cartao.appendChild(acoes);

    return cartao;
}

function criarSecaoChecklist(tarefa) {
    const secao = document.createElement('div');
    secao.classList.add('secao-checklist');

    const lista = document.createElement('ul');
    lista.classList.add('lista-checklist');

    tarefa.itensChecklist.forEach(function (item) {
        lista.appendChild(criarLinhaChecklist(tarefa.id, item));
    });

    const linhaAdicionar = document.createElement('div');
    linhaAdicionar.classList.add('linha-adicionar-item');

    const inputNovoItem = document.createElement('input');
    inputNovoItem.type = 'text';
    inputNovoItem.placeholder = 'Adicionar item ao checklist';
    inputNovoItem.maxLength = 200;

    const botaoAdicionarItem = document.createElement('button');
    botaoAdicionarItem.type = 'button';
    botaoAdicionarItem.classList.add('botao-icone');
    botaoAdicionarItem.textContent = 'Adicionar';

    function adicionarItem() {
        const descricao = inputNovoItem.value.trim();

        if (!descricao) {
            return;
        }

        botaoAdicionarItem.disabled = true;

        adicionarItemChecklist(tarefa.id, { descricao: descricao })
            .then(function () {
                inputNovoItem.value = '';
                carregarTarefas();
            })
            .catch(function (erro) {
                exibirMensagem(erro.mensagem || 'Não foi possível adicionar o item.', 'erro');
            })
            .finally(function () {
                botaoAdicionarItem.disabled = false;
            });
    }

    botaoAdicionarItem.addEventListener('click', adicionarItem);
    inputNovoItem.addEventListener('keydown', function (evento) {
        if (evento.key === 'Enter') {
            evento.preventDefault();
            adicionarItem();
        }
    });

    linhaAdicionar.appendChild(inputNovoItem);
    linhaAdicionar.appendChild(botaoAdicionarItem);

    secao.appendChild(lista);
    secao.appendChild(linhaAdicionar);

    return secao;
}

function criarLinhaChecklist(tarefaId, item) {
    const linha = document.createElement('li');
    linha.classList.add('linha-checklist');

    if (item.concluido) {
        linha.classList.add('checklist-concluido');
    }

    const checkbox = document.createElement('input');
    checkbox.type = 'checkbox';
    checkbox.checked = item.concluido;
    checkbox.addEventListener('change', function () {
        checkbox.disabled = true;

        alternarItemChecklist(tarefaId, item.id)
            .then(function () {
                carregarTarefas();
            })
            .catch(function (erro) {
                exibirMensagem(erro.mensagem || 'Não foi possível atualizar o item.', 'erro');
                checkbox.disabled = false;
            });
    });

    const textoItem = document.createElement('span');
    textoItem.textContent = item.descricao;

    const botaoRemoverItem = document.createElement('button');
    botaoRemoverItem.type = 'button';
    botaoRemoverItem.classList.add('botao-remover-item');
    botaoRemoverItem.textContent = '×';
    botaoRemoverItem.setAttribute('aria-label', 'Remover item');
    botaoRemoverItem.addEventListener('click', function () {
        removerItemChecklist(tarefaId, item.id)
            .then(function () {
                carregarTarefas();
            })
            .catch(function (erro) {
                exibirMensagem(erro.mensagem || 'Não foi possível remover o item.', 'erro');
            });
    });

    linha.appendChild(checkbox);
    linha.appendChild(textoItem);
    linha.appendChild(botaoRemoverItem);

    return linha;
}

function formatarDataTarefa(valor) {
    const partes = valor.split('-');
    return partes[2] + '/' + partes[1] + '/' + partes[0];
}

function abrirModalNovaTarefa() {
    tarefaEmEdicaoId = null;
    tituloModalTarefa.textContent = 'Nova tarefa';
    limparFormularioTarefa();
    modalFormularioTarefa.showModal();
    inputTitulo.focus();
}

function abrirModalEdicaoTarefa(id) {
    mostrarCarregamento(true);

    buscarTarefa(id)
        .then(function (tarefa) {
            tarefaEmEdicaoId = tarefa.id;
            tituloModalTarefa.textContent = 'Editar tarefa';
            preencherFormularioTarefa(tarefa);
            modalFormularioTarefa.showModal();
            inputTitulo.focus();
        })
        .catch(function (erro) {
            exibirMensagem(erro.mensagem || 'Não foi possível carregar a tarefa.', 'erro');
        })
        .finally(function () {
            mostrarCarregamento(false);
        });
}

function preencherFormularioTarefa(tarefa) {
    inputTitulo.value = tarefa.titulo || '';
    textareaDescricaoTarefa.value = tarefa.descricao || '';
    inputResponsavel.value = tarefa.responsavel || '';
    selectPrioridade.value = tarefa.prioridade || '';
    inputPrazo.value = tarefa.prazo || '';
    // "Atrasada" nao e mais uma opcao: o atraso e calculado pelo prazo.
    // Tarefas antigas salvas assim abrem com o status em branco para voce escolher.
    selectStatusTarefa.value = tarefa.status === 'ATRASADA' ? '' : (tarefa.status || '');
}

function limparFormularioTarefa() {
    formularioTarefa.reset();
    limparErrosFormularioTarefa();
}

function fecharModalFormularioTarefa() {
    modalFormularioTarefa.close();
    limparFormularioTarefa();
    tarefaEmEdicaoId = null;
}

function limparErrosFormularioTarefa() {
    document.querySelectorAll('#formularioTarefa .mensagem-erro-campo').forEach(function (elemento) {
        elemento.textContent = '';
    });
    document.querySelectorAll('#formularioTarefa .campo-formulario.com-erro').forEach(function (elemento) {
        elemento.classList.remove('com-erro');
    });
}

function validarFormularioTarefa() {
    limparErrosFormularioTarefa();
    let valido = true;

    if (!inputTitulo.value.trim()) {
        marcarCampoComErroTarefa('erroTitulo', inputTitulo, 'O título é obrigatório');
        valido = false;
    }

    if (!inputResponsavel.value.trim()) {
        marcarCampoComErroTarefa('erroResponsavel', inputResponsavel, 'O responsável é obrigatório');
        valido = false;
    }

    if (!selectPrioridade.value) {
        marcarCampoComErroTarefa('erroPrioridade', selectPrioridade, 'Selecione a prioridade');
        valido = false;
    }

    if (!inputPrazo.value) {
        marcarCampoComErroTarefa('erroPrazo', inputPrazo, 'Informe o prazo');
        valido = false;
    }

    if (!selectStatusTarefa.value) {
        marcarCampoComErroTarefa('erroStatusTarefa', selectStatusTarefa, 'Selecione o status');
        valido = false;
    }

    return valido;
}

function marcarCampoComErroTarefa(idSpanErro, campo, mensagem) {
    document.getElementById(idSpanErro).textContent = mensagem;
    campo.closest('.campo-formulario').classList.add('com-erro');
}

function montarDadosFormularioTarefa() {
    return {
        clienteId: Number(clienteIdAtualTarefas),
        titulo: inputTitulo.value.trim(),
        descricao: textareaDescricaoTarefa.value.trim(),
        responsavel: inputResponsavel.value.trim(),
        prioridade: selectPrioridade.value,
        prazo: inputPrazo.value,
        status: selectStatusTarefa.value
    };
}

function salvarTarefa(evento) {
    evento.preventDefault();

    if (!validarFormularioTarefa()) {
        return;
    }

    const dados = montarDadosFormularioTarefa();
    btnSalvarTarefa.disabled = true;
    btnCancelarTarefa.disabled = true;

    const operacao = tarefaEmEdicaoId
        ? atualizarTarefa(tarefaEmEdicaoId, dados)
        : criarTarefa(dados);

    operacao
        .then(function () {
            exibirMensagem(tarefaEmEdicaoId ? 'Tarefa atualizada com sucesso.' : 'Tarefa criada com sucesso.', 'sucesso');
            fecharModalFormularioTarefa();
            carregarTarefas();
        })
        .catch(function (erro) {
            exibirMensagem(erro.mensagem || 'Não foi possível salvar a tarefa.', 'erro');
        })
        .finally(function () {
            btnSalvarTarefa.disabled = false;
            btnCancelarTarefa.disabled = false;
        });
}

function abrirModalExclusaoTarefa(id, titulo) {
    tarefaParaExcluirId = id;
    textoConfirmarExclusaoTarefa.textContent = 'Deseja realmente excluir a tarefa "' + titulo + '"? Essa ação não pode ser desfeita.';
    modalConfirmarExclusaoTarefa.showModal();
}

function fecharModalExclusaoTarefa() {
    modalConfirmarExclusaoTarefa.close();
    tarefaParaExcluirId = null;
}

function confirmarExclusaoTarefa() {
    if (!tarefaParaExcluirId) {
        return;
    }

    btnConfirmarExclusaoTarefa.disabled = true;
    btnCancelarExclusaoTarefa.disabled = true;

    excluirTarefa(tarefaParaExcluirId)
        .then(function () {
            exibirMensagem('Tarefa excluída com sucesso.', 'sucesso');
            fecharModalExclusaoTarefa();
            carregarTarefas();
        })
        .catch(function (erro) {
            exibirMensagem(erro.mensagem || 'Não foi possível excluir a tarefa.', 'erro');
        })
        .finally(function () {
            btnConfirmarExclusaoTarefa.disabled = false;
            btnCancelarExclusaoTarefa.disabled = false;
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