// Página do financeiro. Aceita ?mes=AAAA-MM e ?clienteId=N na URL.
const parametrosUrlFinanceiro = new URLSearchParams(window.location.search);

const nomesMesesFinanceiro = [
    'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
    'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'
];

const rotulosCategoria = {
    HONORARIOS: 'Honorários',
    REEMBOLSO_TAXAS: 'Reembolso de taxas',
    TAXAS_EMOLUMENTOS: 'Taxas e emolumentos',
    DESLOCAMENTO: 'Deslocamento',
    SERVICO_TERCEIRO: 'Serviço de terceiro',
    MATERIAL_ESCRITORIO: 'Material de escritório',
    IMPOSTO: 'Imposto',
    OUTRO: 'Outro'
};

// Categorias sugeridas para cada tipo (a ordem define a lista do formulário)
const categoriasPorTipo = {
    RECEITA: ['HONORARIOS', 'REEMBOLSO_TAXAS', 'OUTRO'],
    DESPESA: ['TAXAS_EMOLUMENTOS', 'DESLOCAMENTO', 'SERVICO_TERCEIRO', 'MATERIAL_ESCRITORIO', 'IMPOSTO', 'OUTRO']
};

const rotulosFormaPagamento = {
    PIX: 'PIX',
    DINHEIRO: 'Dinheiro',
    TRANSFERENCIA: 'Transferência',
    BOLETO: 'Boleto',
    CARTAO: 'Cartão',
    CHEQUE: 'Cheque'
};

const rotulosSituacaoLancamento = {
    PAGO: 'Pago',
    PENDENTE: 'Em aberto',
    ATRASADO: 'Atrasado'
};

const rotulosTipoProcessoFin = {
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

const formatoMoeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

// Mês na tela
const hojeFinanceiro = new Date();
let anoAtual = hojeFinanceiro.getFullYear();
let mesAtual = hojeFinanceiro.getMonth() + 1;

let lancamentosCarregados = [];
let lancamentoEmEdicaoId = null;
let lancamentoParaExcluirId = null;
let lancamentoParaPagarId = null;
let versaoCarregamento = 0;
let versaoEdicao = 0;
let versaoProcessos = 0;
let temporizadorPesquisa;

// Nome do campo na API -> id do campo na tela
const camposLancamento = {
    tipo: 'selectTipo',
    categoria: 'selectCategoria',
    descricao: 'inputDescricao',
    valor: 'inputValor',
    dataVencimento: 'inputDataVencimento',
    clienteId: 'selectCliente',
    processoId: 'selectProcesso',
    dataPagamento: 'inputDataPagamento',
    formaPagamento: 'selectFormaPagamento',
    observacoes: 'textareaObservacoes'
};

// Nome do campo na API -> id do <span> de erro
const errosLancamento = {
    tipo: 'erroTipo',
    categoria: 'erroCategoria',
    descricao: 'erroDescricao',
    valor: 'erroValor',
    dataVencimento: 'erroDataVencimento',
    clienteId: 'erroClienteId',
    processoId: 'erroProcessoId',
    dataPagamento: 'erroDataPagamento',
    formaPagamento: 'erroFormaPagamento',
    observacoes: 'erroObservacoes'
};

function elemento(id) {
    return document.getElementById(id);
}

document.addEventListener('DOMContentLoaded', async function () {
    const mesDaUrl = parametrosUrlFinanceiro.get('mes');
    if (mesDaUrl && /^\d{4}-(0[1-9]|1[0-2])$/.test(mesDaUrl)) {
        anoAtual = Number(mesDaUrl.slice(0, 4));
        mesAtual = Number(mesDaUrl.slice(5, 7));
    }

    preencherFormasPagamento();

    elemento('btnMesAnterior').addEventListener('click', function () { mudarMes(-1); });
    elemento('btnMesSeguinte').addEventListener('click', function () { mudarMes(1); });
    elemento('btnMesAtual').addEventListener('click', function () {
        const hoje = new Date();
        anoAtual = hoje.getFullYear();
        mesAtual = hoje.getMonth() + 1;
        carregarMes();
    });

    elemento('btnNovaReceita').addEventListener('click', function () { abrirModalNovo('RECEITA'); });
    elemento('btnNovaDespesa').addEventListener('click', function () { abrirModalNovo('DESPESA'); });
    elemento('modalFormulario').addEventListener('close', function () {
        elemento('formularioLancamento').reset();
        limparErrosFormulario();
        preencherProcessos([]);
        lancamentoEmEdicaoId = null;
        versaoEdicao++;
    });
    elemento('btnFecharModalFormulario').addEventListener('click', fecharModalFormulario);
    elemento('btnCancelarFormulario').addEventListener('click', fecharModalFormulario);
    elemento('formularioLancamento').addEventListener('submit', salvarLancamento);
    elemento('selectTipo').addEventListener('change', function () {
        preencherCategorias(this.value, null);
        ajustarRotulosDoTipo(this.value);
    });
    elemento('selectCliente').addEventListener('change', function () {
        carregarProcessosDoCliente(this.value, null);
    });
    elemento('inputValor').addEventListener('input', function () {
        // Só números, vírgula e ponto
        this.value = this.value.replace(/[^\d,.]/g, '');
    });
    elemento('inputValor').addEventListener('blur', function () {
        const valor = lerValor(this.value);
        if (valor !== null && !Number.isNaN(valor)) this.value = valorParaCampo(valor);
    });

    elemento('formularioPagamento').addEventListener('submit', confirmarPagamento);
    elemento('btnFecharModalPagamento').addEventListener('click', fecharModalPagamento);
    elemento('btnCancelarPagamento').addEventListener('click', fecharModalPagamento);

    elemento('btnFecharModalExclusao').addEventListener('click', fecharModalExclusao);
    elemento('btnCancelarExclusao').addEventListener('click', fecharModalExclusao);
    elemento('btnConfirmarExclusao').addEventListener('click', confirmarExclusao);

    elemento('selectFiltroTipo').addEventListener('change', carregarLancamentos);
    elemento('selectFiltroCliente').addEventListener('change', carregarLancamentos);
    // A situação é filtrada aqui mesmo, sem nova consulta
    elemento('selectFiltroSituacao').addEventListener('change', renderizarLancamentos);
    elemento('inputPesquisa').addEventListener('input', function () {
        clearTimeout(temporizadorPesquisa);
        versaoCarregamento++;
        temporizadorPesquisa = setTimeout(carregarLancamentos, 300);
    });

    await carregarClientes();
    carregarMes();
});

/* ---------- Mês ---------- */

function doisDigitos(numero) {
    return String(numero).padStart(2, '0');
}

function mesTexto() {
    return anoAtual + '-' + doisDigitos(mesAtual);
}

function dataDeHojeTexto() {
    const hoje = new Date();
    return hoje.getFullYear() + '-' + doisDigitos(hoje.getMonth() + 1) + '-' + doisDigitos(hoje.getDate());
}

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

function carregarMes() {
    elemento('tituloMes').textContent = nomesMesesFinanceiro[mesAtual - 1] + ' de ' + anoAtual;
    const url = new URL(window.location.href);
    url.searchParams.set('mes', mesTexto());
    history.replaceState(null, '', url);
    carregarLancamentos();
}

/* ---------- Valores ---------- */

function formatarMoeda(valor) {
    return formatoMoeda.format(Number(valor || 0));
}

// "1.234,56", "1234,56" ou "1234.56" -> 1234.56. Devolve null se vazio e NaN se inválido.
function lerValor(texto) {
    let limpo = String(texto || '').trim();
    if (!limpo) return null;
    if (limpo.includes(',')) {
        limpo = limpo.replace(/\./g, '').replace(',', '.');
    }
    if (!/^\d+(\.\d{1,2})?$/.test(limpo)) return NaN;
    return Number(limpo);
}

// 1234.5 -> "1.234,50"
function valorParaCampo(valor) {
    return Number(valor).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatarData(valor) {
    if (!valor) return '';
    const partes = valor.split('-');
    return partes[2] + '/' + partes[1] + '/' + partes[0];
}

/* ---------- Clientes, processos e listas do formulário ---------- */

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

        const clienteDaUrl = parametrosUrlFinanceiro.get('clienteId');
        if (clienteDaUrl && ordenados.some(function (c) { return String(c.id) === clienteDaUrl; })) {
            elemento('selectFiltroCliente').value = clienteDaUrl;
        }
    } catch (erro) {
        exibirMensagem('Não foi possível carregar a lista de clientes.', 'aviso');
    }
}

function preencherFormasPagamento() {
    ['selectFormaPagamento', 'selectFormaPagamentoRapido'].forEach(function (idSelect) {
        const select = elemento(idSelect);
        Object.entries(rotulosFormaPagamento).forEach(function ([valor, rotulo]) {
            const opcao = document.createElement('option');
            opcao.value = valor;
            opcao.textContent = rotulo;
            select.appendChild(opcao);
        });
    });
}

function preencherCategorias(tipo, selecionada) {
    const select = elemento('selectCategoria');
    select.replaceChildren();
    const vazio = document.createElement('option');
    vazio.value = '';
    vazio.textContent = 'Selecione';
    select.appendChild(vazio);
    const lista = (categoriasPorTipo[tipo] || []).slice();
    // Ao editar, mantém a categoria salva mesmo que não seja a sugerida para o tipo
    if (selecionada && !lista.includes(selecionada)) lista.push(selecionada);
    lista.forEach(function (categoria) {
        const opcao = document.createElement('option');
        opcao.value = categoria;
        opcao.textContent = rotulosCategoria[categoria] || categoria;
        select.appendChild(opcao);
    });
    select.value = selecionada || (tipo === 'RECEITA' ? 'HONORARIOS' : '');
}

function ajustarRotulosDoTipo(tipo) {
    elemento('rotuloDataPagamento').textContent = tipo === 'RECEITA' ? 'Recebido em' : 'Pago em';
}

function preencherProcessos(processos) {
    const select = elemento('selectProcesso');
    select.replaceChildren();
    const vazio = document.createElement('option');
    vazio.value = '';
    vazio.textContent = 'Nenhum';
    select.appendChild(vazio);
    processos.forEach(function (processo) {
        const opcao = document.createElement('option');
        opcao.value = processo.id;
        const tipo = rotulosTipoProcessoFin[processo.tipo] || processo.tipo;
        opcao.textContent = processo.numeroProtocolo ? tipo + ' — ' + processo.numeroProtocolo
            : tipo + ' (aberto em ' + formatarData(processo.dataAbertura) + ')';
        select.appendChild(opcao);
    });
}

async function carregarProcessosDoCliente(clienteId, processoSelecionado) {
    const versaoAtual = ++versaoProcessos;
    preencherProcessos([]);
    elemento('selectProcesso').disabled = !clienteId;
    if (!clienteId) return;
    try {
        const processos = await listarProcessos(clienteId, '', '');
        if (versaoAtual !== versaoProcessos) return;
        preencherProcessos(processos || []);
        if (processoSelecionado) elemento('selectProcesso').value = String(processoSelecionado);
    } catch (erro) {
        if (versaoAtual === versaoProcessos) {
            exibirMensagem('Não foi possível carregar os processos do cliente.', 'aviso');
        }
    }
}

/* ---------- Lista e resumo ---------- */

async function carregarLancamentos() {
    const versaoAtual = ++versaoCarregamento;
    const mes = mesTexto();
    elemento('indicadorCarregamento').classList.remove('oculto');
    try {
        const [lista, resumo] = await Promise.all([
            listarLancamentos(mes, elemento('selectFiltroTipo').value,
                elemento('selectFiltroCliente').value, elemento('inputPesquisa').value),
            buscarResumoFinanceiro(mes)
        ]);
        if (versaoAtual !== versaoCarregamento) return;
        lancamentosCarregados = Array.isArray(lista) ? lista : [];
        preencherResumo(resumo);
        renderizarLancamentos();
    } catch (erro) {
        if (versaoAtual !== versaoCarregamento) return;
        lancamentosCarregados = [];
        renderizarLancamentos();
        exibirMensagem(erro.mensagem || 'Não foi possível carregar o financeiro.', 'erro');
    } finally {
        if (versaoAtual === versaoCarregamento) {
            elemento('indicadorCarregamento').classList.add('oculto');
        }
    }
}

function preencherResumo(resumo) {
    elemento('valorRecebido').textContent = formatarMoeda(resumo.recebido);
    elemento('valorPago').textContent = formatarMoeda(resumo.pago);

    const saldo = Number(resumo.saldo);
    const campoSaldo = elemento('valorSaldo');
    campoSaldo.textContent = formatarMoeda(saldo);
    campoSaldo.classList.toggle('valor-receita', saldo > 0);
    campoSaldo.classList.toggle('valor-despesa', saldo < 0);

    elemento('valorAReceber').textContent = formatarMoeda(resumo.totalAReceber);
    elemento('detalheAPagar').textContent = 'A pagar no mês: ' + formatarMoeda(resumo.totalAPagar);

    const atrasado = Number(resumo.receberEmAtraso);
    elemento('valorReceberAtrasado').textContent = formatarMoeda(atrasado);
    elemento('detalhePagarAtrasado').textContent = 'A pagar em atraso: ' + formatarMoeda(resumo.pagarEmAtraso);
    elemento('cartaoAtrasado').classList.toggle('cartao-alerta', atrasado > 0 || Number(resumo.pagarEmAtraso) > 0);
}

function renderizarLancamentos() {
    const filtroSituacao = elemento('selectFiltroSituacao').value;
    const lista = lancamentosCarregados.filter(function (lancamento) {
        return !filtroSituacao || lancamento.situacao === filtroSituacao;
    });

    const corpo = elemento('corpoTabelaLancamentos');
    corpo.replaceChildren();
    elemento('estadoVazioLancamentos').classList.toggle('oculto', lista.length > 0);

    lista.forEach(function (lancamento) {
        const linha = document.createElement('tr');
        const receita = lancamento.tipo === 'RECEITA';

        linha.appendChild(criarCelula('Vencimento', formatarData(lancamento.dataVencimento)));

        const complemento = (receita ? 'Receita' : 'Despesa') + ' · ' + (rotulosCategoria[lancamento.categoria] || lancamento.categoria);
        linha.appendChild(criarCelulaDuasLinhas('Descrição', lancamento.descricao, complemento));

        linha.appendChild(criarCelulaDuasLinhas('Cliente / Processo', lancamento.nomeCliente, lancamento.descricaoProcesso));

        const celulaValor = criarCelula('Valor', (receita ? '+ ' : '− ') + formatarMoeda(lancamento.valor));
        celulaValor.classList.add('celula-valor', receita ? 'valor-receita' : 'valor-despesa');
        linha.appendChild(celulaValor);

        const celulaSituacao = criarCelula('Situação', '');
        const selo = document.createElement('span');
        selo.classList.add('selo-lancamento', 'lancamento-' + String(lancamento.situacao).toLowerCase());
        selo.textContent = lancamento.situacao === 'PAGO'
            ? (receita ? 'Recebido' : 'Pago')
            : rotulosSituacaoLancamento[lancamento.situacao];
        celulaSituacao.replaceChildren(selo);
        if (lancamento.dataPagamento) {
            const detalhe = document.createElement('span');
            detalhe.classList.add('imovel-codigos');
            detalhe.textContent = 'em ' + formatarData(lancamento.dataPagamento)
                + (lancamento.formaPagamento ? ' · ' + rotulosFormaPagamento[lancamento.formaPagamento] : '');
            celulaSituacao.appendChild(detalhe);
        }
        linha.appendChild(celulaSituacao);

        linha.appendChild(criarCelulaAcoes(lancamento));
        corpo.appendChild(linha);
    });
}

function criarCelula(rotulo, valor) {
    const celula = document.createElement('td');
    celula.setAttribute('data-rotulo', rotulo);
    celula.textContent = valor || '—';
    return celula;
}

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

function criarCelulaAcoes(lancamento) {
    const celula = document.createElement('td');
    celula.setAttribute('data-rotulo', 'Ações');
    const container = document.createElement('div');
    container.classList.add('acoes-tabela');
    const receita = lancamento.tipo === 'RECEITA';
    const acoes = [];
    if (lancamento.situacao === 'PAGO') {
        acoes.push(['Reabrir', function () { reabrirLancamento(lancamento); }]);
    } else {
        acoes.push([receita ? 'Receber' : 'Pagar', function () { abrirModalPagamento(lancamento); }]);
    }
    acoes.push(['Editar', function () { abrirModalEdicao(lancamento.id); }]);
    acoes.push(['Excluir', function () { abrirModalExclusao(lancamento); }]);
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

/* ---------- Formulário ---------- */

function abrirModalNovo(tipo) {
    versaoEdicao++;
    lancamentoEmEdicaoId = null;
    elemento('formularioLancamento').reset();
    limparErrosFormulario();
    elemento('tituloModalFormulario').textContent = tipo === 'RECEITA' ? 'Nova receita' : 'Nova despesa';
    elemento('selectTipo').value = tipo;
    preencherCategorias(tipo, null);
    ajustarRotulosDoTipo(tipo);
    elemento('inputDataVencimento').value = dataDeHojeTexto();
    const clienteFiltrado = elemento('selectFiltroCliente').value;
    elemento('selectCliente').value = clienteFiltrado;
    carregarProcessosDoCliente(clienteFiltrado, null);
    elemento('modalFormulario').showModal();
    elemento('inputDescricao').focus();
}

async function abrirModalEdicao(id) {
    const versaoAtual = ++versaoEdicao;
    try {
        const lancamento = await buscarLancamento(id);
        if (versaoAtual !== versaoEdicao) return;
        lancamentoEmEdicaoId = id;
        elemento('formularioLancamento').reset();
        limparErrosFormulario();
        Object.entries(camposLancamento).forEach(function ([campo, idCampo]) {
            if (campo === 'processoId' || campo === 'categoria') return;
            elemento(idCampo).value = lancamento[campo] ?? '';
        });
        preencherCategorias(lancamento.tipo, lancamento.categoria);
        ajustarRotulosDoTipo(lancamento.tipo);
        elemento('inputValor').value = valorParaCampo(lancamento.valor);
        elemento('selectCliente').value = lancamento.clienteId ? String(lancamento.clienteId) : '';
        carregarProcessosDoCliente(lancamento.clienteId, lancamento.processoId);
        elemento('tituloModalFormulario').textContent = lancamento.tipo === 'RECEITA' ? 'Editar receita' : 'Editar despesa';
        elemento('modalFormulario').showModal();
    } catch (erro) {
        if (versaoAtual === versaoEdicao) {
            exibirMensagem(erro.mensagem || 'Não foi possível abrir o lançamento.', 'erro');
        }
    }
}

function fecharModalFormulario() {
    elemento('modalFormulario').close();
}

function montarDados() {
    const dados = {};
    Object.entries(camposLancamento).forEach(function ([campo, idCampo]) {
        const valor = elemento(idCampo).value.trim();
        dados[campo] = valor === '' ? null : valor;
    });
    dados.descricao = dados.descricao || '';
    dados.valor = lerValor(elemento('inputValor').value);
    dados.clienteId = dados.clienteId ? Number(dados.clienteId) : null;
    dados.processoId = dados.processoId ? Number(dados.processoId) : null;
    if (!dados.dataPagamento) dados.formaPagamento = null;
    return dados;
}

function validarFormulario(dados) {
    let valido = true;
    if (!dados.categoria) {
        marcarErro('categoria', 'Selecione a categoria');
        valido = false;
    }
    if (!dados.descricao) {
        marcarErro('descricao', 'Informe a descrição');
        valido = false;
    }
    if (dados.valor === null || Number.isNaN(dados.valor) || dados.valor <= 0) {
        marcarErro('valor', 'Informe um valor maior que zero, com até 2 casas decimais');
        valido = false;
    }
    if (!dados.dataVencimento) {
        marcarErro('dataVencimento', 'Informe o vencimento');
        valido = false;
    }
    if (dados.dataPagamento && dados.dataPagamento > dataDeHojeTexto()) {
        marcarErro('dataPagamento', 'Não pode ser uma data futura');
        valido = false;
    }
    return valido;
}

async function salvarLancamento(evento) {
    evento.preventDefault();
    const botao = elemento('btnSalvarLancamento');
    if (botao.disabled) return;
    limparErrosFormulario();

    const dados = montarDados();
    if (!validarFormulario(dados)) return;

    botao.disabled = true;
    try {
        if (lancamentoEmEdicaoId !== null) {
            await atualizarLancamento(lancamentoEmEdicaoId, dados);
        } else {
            await criarLancamento(dados);
        }
        fecharModalFormulario();
        exibirMensagem('Lançamento salvo com sucesso.', 'sucesso');
        // Vai para o mês do vencimento, para o lançamento aparecer na lista
        anoAtual = Number(dados.dataVencimento.slice(0, 4));
        mesAtual = Number(dados.dataVencimento.slice(5, 7));
        elemento('inputPesquisa').value = '';
        elemento('selectFiltroSituacao').value = '';
        elemento('selectFiltroTipo').value = '';
        clearTimeout(temporizadorPesquisa);
        carregarMes();
    } catch (erro) {
        let marcouCampo = false;
        Object.entries(erro.erros || {}).forEach(function ([campo, mensagem]) {
            if (marcarErro(campo, mensagem)) marcouCampo = true;
        });
        if (!marcouCampo) {
            exibirMensagem(erro.mensagem || 'Não foi possível salvar o lançamento.', 'erro');
        }
    } finally {
        botao.disabled = false;
    }
}

function marcarErro(campo, mensagem) {
    if (!errosLancamento[campo]) return false;
    elemento(errosLancamento[campo]).textContent = mensagem;
    return true;
}

function limparErrosFormulario() {
    document.querySelectorAll('#formularioLancamento .mensagem-erro-campo').forEach(function (campo) {
        campo.textContent = '';
    });
}

/* ---------- Pagamento rápido ---------- */

function abrirModalPagamento(lancamento) {
    lancamentoParaPagarId = lancamento.id;
    const receita = lancamento.tipo === 'RECEITA';
    elemento('tituloModalPagamento').textContent = receita ? 'Registrar recebimento' : 'Registrar pagamento';
    elemento('textoPagamento').textContent = lancamento.descricao + ' — ' + formatarMoeda(lancamento.valor);
    elemento('inputDataPagamentoRapido').value = dataDeHojeTexto();
    elemento('selectFormaPagamentoRapido').value = '';
    elemento('erroDataPagamentoRapido').textContent = '';
    elemento('modalPagamento').showModal();
}

function fecharModalPagamento() {
    elemento('modalPagamento').close();
    lancamentoParaPagarId = null;
}

async function confirmarPagamento(evento) {
    evento.preventDefault();
    const botao = elemento('btnConfirmarPagamento');
    if (botao.disabled || lancamentoParaPagarId === null) return;
    const data = elemento('inputDataPagamentoRapido').value;
    if (!data) {
        elemento('erroDataPagamentoRapido').textContent = 'Informe a data';
        return;
    }
    if (data > dataDeHojeTexto()) {
        elemento('erroDataPagamentoRapido').textContent = 'Não pode ser uma data futura';
        return;
    }
    botao.disabled = true;
    try {
        await registrarPagamentoLancamento(lancamentoParaPagarId, {
            dataPagamento: data,
            formaPagamento: elemento('selectFormaPagamentoRapido').value || null
        });
        fecharModalPagamento();
        exibirMensagem('Pagamento registrado.', 'sucesso');
        carregarLancamentos();
    } catch (erro) {
        exibirMensagem(erro.mensagem || 'Não foi possível registrar o pagamento.', 'erro');
    } finally {
        botao.disabled = false;
    }
}

async function reabrirLancamento(lancamento) {
    try {
        await desfazerPagamentoLancamento(lancamento.id);
        exibirMensagem('Lançamento voltou a ficar em aberto.', 'sucesso');
        carregarLancamentos();
    } catch (erro) {
        exibirMensagem(erro.mensagem || 'Não foi possível reabrir o lançamento.', 'erro');
    }
}

/* ---------- Exclusão ---------- */

function abrirModalExclusao(lancamento) {
    lancamentoParaExcluirId = lancamento.id;
    elemento('textoConfirmarExclusao').textContent = 'Deseja excluir o lançamento "' + lancamento.descricao
        + '" de ' + formatarMoeda(lancamento.valor) + '? Essa ação não pode ser desfeita.';
    elemento('modalConfirmarExclusao').showModal();
}

function fecharModalExclusao() {
    elemento('modalConfirmarExclusao').close();
}

async function confirmarExclusao() {
    const botao = elemento('btnConfirmarExclusao');
    if (botao.disabled || lancamentoParaExcluirId === null) return;
    botao.disabled = true;
    try {
        await excluirLancamento(lancamentoParaExcluirId);
        fecharModalExclusao();
        lancamentoParaExcluirId = null;
        exibirMensagem('Lançamento excluído.', 'sucesso');
        carregarLancamentos();
    } catch (erro) {
        exibirMensagem(erro.mensagem || 'Não foi possível excluir o lançamento.', 'erro');
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
