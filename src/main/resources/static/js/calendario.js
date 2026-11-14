const nomesMeses = [
    'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
    'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'
];

const nomesDiasSemana = ['dom', 'seg', 'ter', 'qua', 'qui', 'sex', 'sáb'];

const rotulosTipoEvento = {
    TAREFA: 'Prazo',
    RETORNO: 'Retorno',
    COMPROMISSO: 'Compromisso',
    PROCESSO: 'Processo'
};

// Processos com estes status já terminaram e aparecem riscados no calendário
const statusProcessoFinalizados = ['DEFERIDO', 'INDEFERIDO', 'ARQUIVADO'];

// Mês que está na tela. new Date() sem texto usa o relógio do computador, sem problema de fuso.
const agora = new Date();
let anoAtual = agora.getFullYear();
let mesAtual = agora.getMonth() + 1;

let compromissoEmEdicaoId = null;

const tituloMes = document.getElementById('tituloMes');
const gradeCalendario = document.getElementById('gradeCalendario');
const estadoVazioCalendario = document.getElementById('estadoVazioCalendario');
const indicadorCarregamento = document.getElementById('indicadorCarregamento');

const modalFormularioCompromisso = document.getElementById('modalFormularioCompromisso');
const formularioCompromisso = document.getElementById('formularioCompromisso');
const tituloModalCompromisso = document.getElementById('tituloModalCompromisso');
const btnSalvarCompromisso = document.getElementById('btnSalvarCompromisso');
const btnCancelarCompromisso = document.getElementById('btnCancelarCompromisso');
const btnExcluirCompromisso = document.getElementById('btnExcluirCompromisso');

const modalConfirmarExclusaoCompromisso = document.getElementById('modalConfirmarExclusaoCompromisso');
const textoConfirmarExclusaoCompromisso = document.getElementById('textoConfirmarExclusaoCompromisso');
const btnConfirmarExclusaoCompromisso = document.getElementById('btnConfirmarExclusaoCompromisso');
const btnCancelarExclusaoCompromisso = document.getElementById('btnCancelarExclusaoCompromisso');

const inputTituloCompromisso = document.getElementById('inputTituloCompromisso');
const inputDataCompromisso = document.getElementById('inputDataCompromisso');
const inputHoraCompromisso = document.getElementById('inputHoraCompromisso');
const selectClienteCompromisso = document.getElementById('selectClienteCompromisso');
const textareaDescricaoCompromisso = document.getElementById('textareaDescricaoCompromisso');

// Liga cada campo que a API valida ao <span> de erro e ao campo da tela
const camposCompromisso = {
    titulo: { erro: 'erroTituloCompromisso', campo: inputTituloCompromisso },
    data: { erro: 'erroDataCompromisso', campo: inputDataCompromisso },
    hora: { erro: 'erroHoraCompromisso', campo: inputHoraCompromisso },
    clienteId: { erro: 'erroClienteCompromisso', campo: selectClienteCompromisso },
    descricao: { erro: 'erroDescricaoCompromisso', campo: textareaDescricaoCompromisso }
};

// Fechar com Esc também passa por aqui, então o formulário nunca fica com dados antigos
modalFormularioCompromisso.addEventListener('close', function () {
    limparFormularioCompromisso();
    compromissoEmEdicaoId = null;
});

document.addEventListener('DOMContentLoaded', function () {
    document.getElementById('btnMesAnterior').addEventListener('click', function () {
        mudarMes(-1);
    });
    document.getElementById('btnMesSeguinte').addEventListener('click', function () {
        mudarMes(1);
    });
    document.getElementById('btnHoje').addEventListener('click', function () {
        const hoje = new Date();
        anoAtual = hoje.getFullYear();
        mesAtual = hoje.getMonth() + 1;
        carregarMes();
    });

    document.getElementById('btnNovoCompromisso').addEventListener('click', function () {
        abrirModalNovoCompromisso(null);
    });
    document.getElementById('btnFecharModalCompromisso').addEventListener('click', fecharModalCompromisso);
    btnCancelarCompromisso.addEventListener('click', fecharModalCompromisso);
    formularioCompromisso.addEventListener('submit', salvarCompromisso);
    btnExcluirCompromisso.addEventListener('click', abrirModalExclusaoCompromisso);

    document.getElementById('btnFecharModalExclusaoCompromisso').addEventListener('click', fecharModalExclusaoCompromisso);
    btnCancelarExclusaoCompromisso.addEventListener('click', fecharModalExclusaoCompromisso);
    btnConfirmarExclusaoCompromisso.addEventListener('click', confirmarExclusaoCompromisso);

    carregarClientesDoFormulario();
    carregarMes();
});

/* ---------- Mês e grade ---------- */

function mudarMes(diferenca) {
    mesAtual += diferenca;

    if (mesAtual < 1) {
        mesAtual = 12;
        anoAtual -= 1;
    } else if (mesAtual > 12) {
        mesAtual = 1;
        anoAtual += 1;
    }

    carregarMes();
}

function doisDigitos(numero) {
    return String(numero).padStart(2, '0');
}

// Monta "AAAA-MM-DD" a partir de números
function montarDataTexto(ano, mes, dia) {
    return ano + '-' + doisDigitos(mes) + '-' + doisDigitos(dia);
}

function dataDeHojeTexto() {
    const hoje = new Date();
    return montarDataTexto(hoje.getFullYear(), hoje.getMonth() + 1, hoje.getDate());
}

// Soma dias a uma data "AAAA-MM-DD" e devolve no mesmo formato
function somarDias(dataTexto, dias) {
    const partes = dataTexto.split('-').map(Number);
    const data = new Date(partes[0], partes[1] - 1, partes[2] + dias);
    return montarDataTexto(data.getFullYear(), data.getMonth() + 1, data.getDate());
}

function carregarMes() {
    tituloMes.textContent = nomesMeses[mesAtual - 1] + ' de ' + anoAtual;
    indicadorCarregamento.classList.remove('oculto');

    // Guarda qual mês foi pedido: se o usuário trocar de mês antes da resposta chegar,
    // a resposta antiga é ignorada
    const mesPedido = anoAtual + '-' + doisDigitos(mesAtual);

    buscarCalendario(mesPedido)
        .then(function (calendario) {
            if (mesPedido !== anoAtual + '-' + doisDigitos(mesAtual)) {
                return;
            }
            renderizarGrade(calendario.eventos || []);
        })
        .catch(function (erro) {
            if (mesPedido !== anoAtual + '-' + doisDigitos(mesAtual)) {
                return;
            }
            renderizarGrade([]);
            exibirMensagem(erro.mensagem || 'Não foi possível carregar o calendário.', 'erro');
        })
        .finally(function () {
            indicadorCarregamento.classList.add('oculto');
        });
}

function renderizarGrade(eventos) {
    gradeCalendario.textContent = '';
    estadoVazioCalendario.classList.toggle('oculto', eventos.length > 0);

    // Agrupa os eventos por dia: { "2026-11-12": [evento, evento], ... }
    const eventosPorDia = {};
    eventos.forEach(function (evento) {
        if (!eventosPorDia[evento.data]) {
            eventosPorDia[evento.data] = [];
        }
        eventosPorDia[evento.data].push(evento);
    });

    // new Date(ano, mes, dia) com números usa o horário local e não sofre com o fuso
    const diaSemanaDoPrimeiro = new Date(anoAtual, mesAtual - 1, 1).getDay();
    const diasNoMes = new Date(anoAtual, mesAtual, 0).getDate();
    const hoje = dataDeHojeTexto();

    // Casas vazias antes do dia 1, para o mês começar na coluna certa
    for (let i = 0; i < diaSemanaDoPrimeiro; i++) {
        const casaVazia = document.createElement('div');
        casaVazia.classList.add('dia-calendario', 'dia-fora-do-mes');
        gradeCalendario.appendChild(casaVazia);
    }

    for (let dia = 1; dia <= diasNoMes; dia++) {
        const dataTexto = montarDataTexto(anoAtual, mesAtual, dia);
        const eventosDoDia = eventosPorDia[dataTexto] || [];
        gradeCalendario.appendChild(criarCasaDoDia(dia, dataTexto, eventosDoDia, hoje));
    }
}

function criarCasaDoDia(dia, dataTexto, eventosDoDia, hoje) {
    const casa = document.createElement('div');
    casa.classList.add('dia-calendario');

    if (dataTexto === hoje) {
        casa.classList.add('dia-hoje');
    }
    if (eventosDoDia.length === 0) {
        casa.classList.add('dia-sem-eventos');
    }

    const cabecalho = document.createElement('div');
    cabecalho.classList.add('dia-cabecalho');

    const numero = document.createElement('span');
    numero.classList.add('dia-numero');
    numero.textContent = dia;

    const diaSemana = document.createElement('span');
    diaSemana.classList.add('dia-semana');
    diaSemana.textContent = nomesDiasSemana[new Date(anoAtual, mesAtual - 1, dia).getDay()];

    const botaoAdicionar = document.createElement('button');
    botaoAdicionar.type = 'button';
    botaoAdicionar.classList.add('botao-adicionar-dia');
    botaoAdicionar.textContent = '+';
    botaoAdicionar.setAttribute('aria-label', 'Novo compromisso em ' + formatarData(dataTexto));
    botaoAdicionar.addEventListener('click', function () {
        abrirModalNovoCompromisso(dataTexto);
    });

    cabecalho.appendChild(numero);
    cabecalho.appendChild(diaSemana);
    cabecalho.appendChild(botaoAdicionar);
    casa.appendChild(cabecalho);

    const lista = document.createElement('div');
    lista.classList.add('lista-eventos-dia');

    eventosDoDia.forEach(function (evento) {
        lista.appendChild(criarEvento(evento, hoje));
    });

    casa.appendChild(lista);
    return casa;
}

function criarEvento(evento, hoje) {
    const botao = document.createElement('button');
    botao.type = 'button';
    botao.classList.add('evento-calendario');

    if (evento.tipo === 'TAREFA') {
        botao.classList.add(classeDaTarefa(evento, hoje));
    } else if (evento.tipo === 'RETORNO') {
        botao.classList.add('evento-retorno');
    } else if (evento.tipo === 'PROCESSO') {
        botao.classList.add(classeDoProcesso(evento, hoje));
    } else {
        botao.classList.add('evento-compromisso');
    }

    const tipo = document.createElement('span');
    tipo.classList.add('evento-tipo');
    tipo.textContent = rotulosTipoEvento[evento.tipo] || evento.tipo;
    if (evento.hora) {
        tipo.textContent += ' ' + evento.hora.substring(0, 5);
    }

    const titulo = document.createElement('span');
    titulo.classList.add('evento-titulo');
    titulo.textContent = evento.titulo;

    botao.appendChild(tipo);
    botao.appendChild(titulo);

    if (evento.nomeCliente) {
        const cliente = document.createElement('span');
        cliente.classList.add('evento-cliente');
        cliente.textContent = evento.nomeCliente;
        botao.appendChild(cliente);
    }

    botao.addEventListener('click', function () {
        abrirEvento(evento);
    });

    return botao;
}

// Mesma regra do backend: atrasada = não concluída e com prazo antes de hoje
function classeDaTarefa(evento, hoje) {
    if (evento.statusTarefa === 'CONCLUIDA') {
        return 'evento-concluido';
    }
    if (evento.data < hoje) {
        return 'evento-atrasado';
    }
    if (evento.data <= somarDias(hoje, 3)) {
        return 'evento-proximo';
    }
    return 'evento-tarefa';
}

// Processo finalizado = riscado; em andamento com prazo vencido = vermelho
function classeDoProcesso(evento, hoje) {
    if (statusProcessoFinalizados.includes(evento.statusProcesso)) {
        return 'evento-concluido';
    }
    if (evento.data < hoje) {
        return 'evento-atrasado';
    }
    return 'evento-processo';
}

function abrirEvento(evento) {
    if (evento.tipo === 'TAREFA') {
        window.location.href = 'tarefas.html?clienteId=' + encodeURIComponent(evento.clienteId);
    } else if (evento.tipo === 'RETORNO') {
        window.location.href = 'atendimentos.html?clienteId=' + encodeURIComponent(evento.clienteId);
    } else if (evento.tipo === 'PROCESSO') {
        window.location.href = 'processos.html?clienteId=' + encodeURIComponent(evento.clienteId);
    } else {
        abrirModalEdicaoCompromisso(evento.origemId);
    }
}

function formatarData(valor) {
    const partes = valor.split('-');
    return partes[2] + '/' + partes[1] + '/' + partes[0];
}

/* ---------- Compromissos ---------- */

function carregarClientesDoFormulario() {
    listarClientes('')
        .then(function (clientes) {
            clientes.forEach(function (cliente) {
                const opcao = document.createElement('option');
                opcao.value = cliente.id;
                opcao.textContent = cliente.nomeCompleto;
                selectClienteCompromisso.appendChild(opcao);
            });
        })
        .catch(function () {
            exibirMensagem('Não foi possível carregar a lista de clientes.', 'aviso');
        });
}

function abrirModalNovoCompromisso(dataTexto) {
    compromissoEmEdicaoId = null;
    limparFormularioCompromisso();
    tituloModalCompromisso.textContent = 'Novo compromisso';
    btnExcluirCompromisso.classList.add('oculto');
    inputDataCompromisso.value = dataTexto || '';
    modalFormularioCompromisso.showModal();
    inputTituloCompromisso.focus();
}

function abrirModalEdicaoCompromisso(id) {
    indicadorCarregamento.classList.remove('oculto');

    buscarCompromisso(id)
        .then(function (compromisso) {
            limparFormularioCompromisso();
            compromissoEmEdicaoId = compromisso.id;
            tituloModalCompromisso.textContent = 'Editar compromisso';
            btnExcluirCompromisso.classList.remove('oculto');

            inputTituloCompromisso.value = compromisso.titulo || '';
            inputDataCompromisso.value = compromisso.data || '';
            inputHoraCompromisso.value = compromisso.hora ? compromisso.hora.substring(0, 5) : '';
            selectClienteCompromisso.value = compromisso.clienteId ? String(compromisso.clienteId) : '';
            textareaDescricaoCompromisso.value = compromisso.descricao || '';

            modalFormularioCompromisso.showModal();
            inputTituloCompromisso.focus();
        })
        .catch(function (erro) {
            exibirMensagem(erro.mensagem || 'Não foi possível carregar o compromisso.', 'erro');
        })
        .finally(function () {
            indicadorCarregamento.classList.add('oculto');
        });
}

function fecharModalCompromisso() {
    // O evento "close" do <dialog> limpa o formulário e o id de edição
    modalFormularioCompromisso.close();
}

function limparFormularioCompromisso() {
    formularioCompromisso.reset();
    limparErrosCompromisso();
}

function limparErrosCompromisso() {
    document.querySelectorAll('#formularioCompromisso .mensagem-erro-campo').forEach(function (elemento) {
        elemento.textContent = '';
    });
    document.querySelectorAll('#formularioCompromisso .campo-formulario.com-erro').forEach(function (elemento) {
        elemento.classList.remove('com-erro');
    });
}

function marcarErroCompromisso(nomeCampo, mensagem) {
    const configuracao = camposCompromisso[nomeCampo];

    if (!configuracao) {
        return false;
    }

    document.getElementById(configuracao.erro).textContent = mensagem;
    configuracao.campo.closest('.campo-formulario').classList.add('com-erro');
    return true;
}

function validarFormularioCompromisso() {
    limparErrosCompromisso();
    let valido = true;

    if (!inputTituloCompromisso.value.trim()) {
        marcarErroCompromisso('titulo', 'O título é obrigatório');
        valido = false;
    }

    if (!inputDataCompromisso.value) {
        marcarErroCompromisso('data', 'Informe a data');
        valido = false;
    }

    return valido;
}

function montarDadosCompromisso() {
    return {
        titulo: inputTituloCompromisso.value.trim(),
        data: inputDataCompromisso.value,
        hora: inputHoraCompromisso.value || null,
        clienteId: selectClienteCompromisso.value ? Number(selectClienteCompromisso.value) : null,
        descricao: textareaDescricaoCompromisso.value.trim() || null
    };
}

function salvarCompromisso(evento) {
    evento.preventDefault();

    if (!validarFormularioCompromisso()) {
        return;
    }

    const dados = montarDadosCompromisso();
    const editando = compromissoEmEdicaoId !== null;

    btnSalvarCompromisso.disabled = true;
    btnCancelarCompromisso.disabled = true;

    const operacao = editando
        ? atualizarCompromisso(compromissoEmEdicaoId, dados)
        : criarCompromisso(dados);

    operacao
        .then(function () {
            exibirMensagem(editando ? 'Compromisso atualizado com sucesso.' : 'Compromisso criado com sucesso.', 'sucesso');
            irParaMesDaData(dados.data);
            fecharModalCompromisso();
            carregarMes();
        })
        .catch(function (erro) {
            // Erros de validação da API vêm em "erros": { campo: mensagem }
            let marcouAlgumCampo = false;

            if (erro.erros) {
                Object.keys(erro.erros).forEach(function (nomeCampo) {
                    if (marcarErroCompromisso(nomeCampo, erro.erros[nomeCampo])) {
                        marcouAlgumCampo = true;
                    }
                });
            }

            if (!marcouAlgumCampo) {
                exibirMensagem(erro.mensagem || 'Não foi possível salvar o compromisso.', 'erro');
            }
        })
        .finally(function () {
            btnSalvarCompromisso.disabled = false;
            btnCancelarCompromisso.disabled = false;
        });
}

// Depois de salvar, mostra o mês em que o compromisso ficou
function irParaMesDaData(dataTexto) {
    const partes = dataTexto.split('-').map(Number);
    anoAtual = partes[0];
    mesAtual = partes[1];
}

function abrirModalExclusaoCompromisso() {
    if (compromissoEmEdicaoId === null) {
        return;
    }

    textoConfirmarExclusaoCompromisso.textContent =
        'Deseja realmente excluir o compromisso "' + inputTituloCompromisso.value.trim() + '"? Essa ação não pode ser desfeita.';
    modalConfirmarExclusaoCompromisso.showModal();
}

function fecharModalExclusaoCompromisso() {
    modalConfirmarExclusaoCompromisso.close();
}

function confirmarExclusaoCompromisso() {
    if (compromissoEmEdicaoId === null) {
        return;
    }

    btnConfirmarExclusaoCompromisso.disabled = true;
    btnCancelarExclusaoCompromisso.disabled = true;

    excluirCompromisso(compromissoEmEdicaoId)
        .then(function () {
            exibirMensagem('Compromisso excluído com sucesso.', 'sucesso');
            fecharModalExclusaoCompromisso();
            fecharModalCompromisso();
            carregarMes();
        })
        .catch(function (erro) {
            exibirMensagem(erro.mensagem || 'Não foi possível excluir o compromisso.', 'erro');
        })
        .finally(function () {
            btnConfirmarExclusaoCompromisso.disabled = false;
            btnCancelarExclusaoCompromisso.disabled = false;
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
