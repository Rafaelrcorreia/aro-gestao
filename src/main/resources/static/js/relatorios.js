// Relatórios. Cada relatório monta resumo + tabelas e informa qual tabela vai para o CSV.
// A aba escolhida fica na URL (?r=financeiro|processos|documentos|clientes).

const formatoMoedaRel = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

const rotulos = {
    categoria: {
        HONORARIOS: 'Honorários', REEMBOLSO_TAXAS: 'Reembolso de taxas', TAXAS_EMOLUMENTOS: 'Taxas e emolumentos',
        DESLOCAMENTO: 'Deslocamento', SERVICO_TERCEIRO: 'Serviço de terceiro',
        MATERIAL_ESCRITORIO: 'Material de escritório', IMPOSTO: 'Imposto', OUTRO: 'Outro'
    },
    formaPagamento: {
        PIX: 'PIX', DINHEIRO: 'Dinheiro', TRANSFERENCIA: 'Transferência', BOLETO: 'Boleto', CARTAO: 'Cartão', CHEQUE: 'Cheque'
    },
    statusProcesso: {
        EM_ELABORACAO: 'Em elaboração', PROTOCOLADO: 'Protocolado', EM_ANALISE: 'Em análise', EXIGENCIA: 'Exigência',
        DEFERIDO: 'Deferido', INDEFERIDO: 'Indeferido', ARQUIVADO: 'Arquivado'
    },
    tipoProcesso: {
        GEORREFERENCIAMENTO: 'Georreferenciamento', CAR: 'CAR', CCIR: 'CCIR', ITR: 'ITR',
        RETIFICACAO_MATRICULA: 'Retificação de matrícula', REGULARIZACAO_FUNDIARIA: 'Regularização fundiária',
        USUCAPIAO: 'Usucapião', LICENCIAMENTO_AMBIENTAL: 'Licenciamento ambiental', OUTORGA_AGUA: 'Outorga de água',
        OUTRO: 'Outro'
    },
    tipoDocumento: {
        MATRICULA: 'Matrícula', ESCRITURA: 'Escritura', CCIR: 'CCIR', ITR: 'ITR', CAR: 'CAR', CERTIDAO: 'Certidão',
        PLANTA_MEMORIAL: 'Planta e memorial', DOCUMENTO_PESSOAL: 'Documento pessoal',
        COMPROVANTE_RESIDENCIA: 'Comprovante de residência', CONTRATO: 'Contrato', PROCURACAO: 'Procuração',
        LICENCA: 'Licença / autorização', OUTRO: 'Outro'
    },
    statusCliente: { ATIVO: 'Ativo', INATIVO: 'Inativo', AGUARDANDO_RETORNO: 'Aguardando retorno' }
};

const relatorios = {
    financeiro: { titulo: 'Relatório financeiro', gerar: gerarFinanceiro, filtros: 'filtrosFinanceiro' },
    processos: { titulo: 'Relatório de processos', gerar: gerarProcessos },
    documentos: { titulo: 'Vencimento de documentos', gerar: gerarDocumentos, filtros: 'filtrosDocumentos' },
    clientes: { titulo: 'Carteira de clientes', gerar: gerarClientes }
};

let relatorioAtual = null;
// Tabela que o botão "Exportar CSV" usa: { nomeArquivo, linhas: [[...cabeçalho], [...], ...] }
let dadosCsv = null;
let versaoGeracao = 0;

function elemento(id) {
    return document.getElementById(id);
}

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.aba-relatorio').forEach(function (aba) {
        aba.addEventListener('click', function () { escolherRelatorio(aba.dataset.relatorio); });
    });
    elemento('btnImprimir').addEventListener('click', function () { window.print(); });
    elemento('btnExportarCsv').addEventListener('click', exportarCsv);
    elemento('btnGerarFinanceiro').addEventListener('click', gerarRelatorioAtual);
    elemento('selectDias').addEventListener('change', gerarRelatorioAtual);
    document.querySelectorAll('.atalhos-periodo [data-periodo]').forEach(function (botao) {
        botao.addEventListener('click', function () {
            aplicarPeriodo(botao.dataset.periodo);
            gerarRelatorioAtual();
        });
    });

    aplicarPeriodo('mes');
    const daUrl = new URLSearchParams(window.location.search).get('r');
    escolherRelatorio(relatorios[daUrl] ? daUrl : 'financeiro');
});

function escolherRelatorio(nome) {
    relatorioAtual = nome;
    document.querySelectorAll('.aba-relatorio').forEach(function (aba) {
        const ativa = aba.dataset.relatorio === nome;
        aba.classList.toggle('ativa', ativa);
        aba.setAttribute('aria-selected', String(ativa));
    });
    Object.values(relatorios).forEach(function (rel) {
        if (rel.filtros) elemento(rel.filtros).classList.add('oculto');
    });
    if (relatorios[nome].filtros) elemento(relatorios[nome].filtros).classList.remove('oculto');

    const url = new URL(window.location.href);
    url.searchParams.set('r', nome);
    history.replaceState(null, '', url);
    gerarRelatorioAtual();
}

async function gerarRelatorioAtual() {
    const versao = ++versaoGeracao;
    const relatorio = relatorios[relatorioAtual];
    elemento('tituloRelatorio').textContent = relatorio.titulo;
    elemento('subtituloRelatorio').textContent = '';
    elemento('corpoRelatorio').replaceChildren();
    dadosCsv = null;
    elemento('btnExportarCsv').disabled = true;
    elemento('indicadorCarregamento').classList.remove('oculto');
    try {
        await relatorio.gerar(function () { return versao === versaoGeracao; });
    } catch (erro) {
        if (versao === versaoGeracao) {
            exibirMensagem(erro.mensagem || 'Não foi possível gerar o relatório.', 'erro');
        }
    } finally {
        if (versao === versaoGeracao) {
            elemento('indicadorCarregamento').classList.add('oculto');
            elemento('btnExportarCsv').disabled = !dadosCsv;
        }
    }
}

/* ---------- Financeiro ---------- */

function aplicarPeriodo(periodo) {
    const hoje = new Date();
    let inicio;
    let fim;
    if (periodo === 'mes-anterior') {
        inicio = new Date(hoje.getFullYear(), hoje.getMonth() - 1, 1);
        fim = new Date(hoje.getFullYear(), hoje.getMonth(), 0);
    } else if (periodo === 'ano') {
        inicio = new Date(hoje.getFullYear(), 0, 1);
        fim = new Date(hoje.getFullYear(), 11, 31);
    } else {
        inicio = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
        fim = new Date(hoje.getFullYear(), hoje.getMonth() + 1, 0);
    }
    elemento('inputInicio').value = dataParaTexto(inicio);
    elemento('inputFim').value = dataParaTexto(fim);
}

async function gerarFinanceiro(aindaValido) {
    const inicio = elemento('inputInicio').value;
    const fim = elemento('inputFim').value;
    if (!inicio || !fim) {
        exibirMensagem('Informe as datas inicial e final.', 'erro');
        return;
    }
    const rel = await buscarRelatorioFinanceiro(inicio, fim);
    if (!aindaValido()) return;

    elemento('subtituloRelatorio').textContent = 'Período: ' + formatarData(rel.inicio) + ' a ' + formatarData(rel.fim)
        + ' · valores pela data de pagamento';
    const corpo = elemento('corpoRelatorio');

    corpo.appendChild(criarCartoes([
        ['Recebido', moeda(rel.totalRecebido), 'valor-receita'],
        ['Pago', moeda(rel.totalPago), 'valor-despesa'],
        ['Saldo', moeda(rel.saldo), Number(rel.saldo) < 0 ? 'valor-despesa' : 'valor-receita'],
        ['Ainda a receber no período', moeda(rel.aReceberNoPeriodo)],
        ['Ainda a pagar no período', moeda(rel.aPagarNoPeriodo)]
    ]));

    corpo.appendChild(criarSecao('Por categoria', ['Tipo', 'Categoria', 'Lançamentos', 'Total'],
        rel.porCategoria.map(function (c) {
            return [c.tipo === 'RECEITA' ? 'Receita' : 'Despesa', rotulos.categoria[c.categoria] || c.categoria,
                c.quantidade, moeda(c.total)];
        }), 'Nenhum pagamento ou recebimento no período.', [3]));

    corpo.appendChild(criarSecao('Recebido por cliente', ['Cliente', 'Recebimentos', 'Total'],
        rel.recebidoPorCliente.map(function (c) {
            return [c.nomeCliente || 'Sem cliente', c.quantidade, moeda(c.total)];
        }), 'Nenhuma receita recebida no período.', [2]));

    const cabecalho = ['Pago em', 'Tipo', 'Categoria', 'Descrição', 'Cliente', 'Forma', 'Valor'];
    const linhas = rel.lancamentos.map(function (l) {
        return [formatarData(l.dataPagamento), l.tipo === 'RECEITA' ? 'Receita' : 'Despesa',
            rotulos.categoria[l.categoria] || l.categoria, l.descricao, l.nomeCliente || '',
            rotulos.formaPagamento[l.formaPagamento] || '', (l.tipo === 'RECEITA' ? '' : '-') + moeda(l.valor)];
    });
    corpo.appendChild(criarSecao('Lançamentos pagos e recebidos', cabecalho, linhas,
        'Nenhum lançamento no período.', [6]));

    dadosCsv = {
        nomeArquivo: 'financeiro_' + rel.inicio + '_a_' + rel.fim + '.csv',
        linhas: [['Pago em', 'Tipo', 'Categoria', 'Descrição', 'Cliente', 'Processo', 'Forma', 'Vencimento', 'Valor']]
            .concat(rel.lancamentos.map(function (l) {
                return [formatarData(l.dataPagamento), l.tipo === 'RECEITA' ? 'Receita' : 'Despesa',
                    rotulos.categoria[l.categoria] || l.categoria, l.descricao, l.nomeCliente || '',
                    l.descricaoProcesso || '', rotulos.formaPagamento[l.formaPagamento] || '',
                    formatarData(l.dataVencimento), numeroCsv(l.tipo === 'RECEITA' ? l.valor : -l.valor)];
            }))
    };
}

/* ---------- Processos ---------- */

async function gerarProcessos(aindaValido) {
    const rel = await buscarRelatorioProcessos();
    if (!aindaValido()) return;

    elemento('subtituloRelatorio').textContent = 'Situação em ' + formatarData(dataParaTexto(new Date()));
    const corpo = elemento('corpoRelatorio');

    corpo.appendChild(criarCartoes([
        ['Processos cadastrados', rel.total],
        ['Em andamento', rel.emAndamento],
        ['Com prazo vencido', rel.comPrazoVencido, rel.comPrazoVencido > 0 ? 'valor-despesa' : '']
    ]));

    corpo.appendChild(criarSecao('Por status', ['Status', 'Quantidade'],
        rel.porStatus.map(function (s) { return [rotulos.statusProcesso[s.status] || s.status, s.quantidade]; }),
        'Nenhum processo cadastrado.', [1]));

    corpo.appendChild(criarSecao('Por tipo', ['Tipo', 'Total', 'Em andamento'],
        rel.porTipo.map(function (t) { return [rotulos.tipoProcesso[t.tipo] || t.tipo, t.total, t.emAndamento]; }),
        'Nenhum processo cadastrado.', [1, 2]));

    const cabecalho = ['Prazo previsto', 'Tipo', 'Cliente', 'Imóvel', 'Protocolo', 'Órgão', 'Status', 'Abertura'];
    const linhas = rel.processosEmAndamento.map(function (p) {
        return [(p.prazoPrevisto ? formatarData(p.prazoPrevisto) : 'Sem prazo') + (p.atrasado ? ' (vencido)' : ''),
            rotulos.tipoProcesso[p.tipo] || p.tipo, p.nomeCliente, p.denominacaoImovel || '',
            p.numeroProtocolo || '', p.orgao || '', rotulos.statusProcesso[p.status] || p.status,
            formatarData(p.dataAbertura)];
    });
    const secao = criarSecao('Processos em andamento (prazo mais próximo primeiro)', cabecalho, linhas,
        'Nenhum processo em andamento.');
    marcarLinhas(secao, rel.processosEmAndamento.map(function (p) { return p.atrasado; }));
    corpo.appendChild(secao);

    dadosCsv = { nomeArquivo: 'processos_em_andamento.csv', linhas: [cabecalho].concat(linhas) };
}

/* ---------- Documentos ---------- */

async function gerarDocumentos(aindaValido) {
    const dias = elemento('selectDias').value;
    const rel = await buscarRelatorioDocumentos(dias);
    if (!aindaValido()) return;

    elemento('subtituloRelatorio').textContent = 'Vencidos e com validade até ' + formatarData(rel.limite)
        + ' (referência: ' + formatarData(rel.dataReferencia) + ')';
    const corpo = elemento('corpoRelatorio');

    corpo.appendChild(criarCartoes([
        ['Vencidos', rel.vencidos, rel.vencidos > 0 ? 'valor-despesa' : ''],
        [rel.dias === 0 ? 'Vencem hoje' : 'Vencem em até ' + rel.dias + ' dias', rel.vencendo]
    ]));

    const cabecalho = ['Validade', 'Situação', 'Documento', 'Tipo', 'Número', 'Cliente', 'Imóvel'];
    const linhas = rel.documentos.map(function (d) {
        return [formatarData(d.dataValidade), d.situacaoValidade === 'VENCIDO' ? 'Vencido' : 'Vence em breve',
            d.titulo, rotulos.tipoDocumento[d.tipo] || d.tipo, d.numero || '', d.nomeCliente, d.denominacaoImovel || ''];
    });
    const secao = criarSecao('Documentos', cabecalho, linhas, 'Nenhum documento vencido ou vencendo neste prazo.');
    marcarLinhas(secao, rel.documentos.map(function (d) { return d.situacaoValidade === 'VENCIDO'; }));
    corpo.appendChild(secao);

    dadosCsv = { nomeArquivo: 'documentos_vencimento_' + rel.dataReferencia + '.csv', linhas: [cabecalho].concat(linhas) };
}

/* ---------- Clientes ---------- */

async function gerarClientes(aindaValido) {
    const rel = await buscarRelatorioClientes();
    if (!aindaValido()) return;

    elemento('subtituloRelatorio').textContent = 'Situação em ' + formatarData(dataParaTexto(new Date()));
    const corpo = elemento('corpoRelatorio');

    corpo.appendChild(criarCartoes([
        ['Clientes', rel.totalClientes],
        ['Imóveis', rel.imoveis],
        ['Processos em andamento', rel.processosEmAndamento],
        ['Documentos vencidos', rel.documentosVencidos, rel.documentosVencidos > 0 ? 'valor-despesa' : ''],
        ['A receber em atraso', moeda(rel.receberEmAtraso), Number(rel.receberEmAtraso) > 0 ? 'valor-despesa' : '']
    ]));

    const cabecalho = ['Cliente', 'Situação', 'Cidade/UF', 'Imóveis', 'Processos em andamento',
        'Documentos vencidos', 'A receber em atraso'];
    const linhas = rel.clientes.map(function (c) {
        return [c.nomeCliente, rotulos.statusCliente[c.status] || c.status, c.cidadeUf || '', c.imoveis,
            c.processosEmAndamento, c.documentosVencidos, moeda(c.receberEmAtraso)];
    });
    const secao = criarSecao('Clientes', cabecalho, linhas, 'Nenhum cliente cadastrado.', [3, 4, 5, 6]);
    marcarLinhas(secao, rel.clientes.map(function (c) {
        return c.documentosVencidos > 0 || Number(c.receberEmAtraso) > 0;
    }));
    corpo.appendChild(secao);

    dadosCsv = {
        nomeArquivo: 'carteira_de_clientes.csv',
        linhas: [cabecalho].concat(rel.clientes.map(function (c) {
            return [c.nomeCliente, rotulos.statusCliente[c.status] || c.status, c.cidadeUf || '', c.imoveis,
                c.processosEmAndamento, c.documentosVencidos, numeroCsv(c.receberEmAtraso)];
        }))
    };
}

/* ---------- Montagem da tela ---------- */

// itens: [rótulo, valor, classe opcional]
function criarCartoes(itens) {
    const area = document.createElement('section');
    area.classList.add('cartoes-dashboard', 'cartoes-relatorio');
    itens.forEach(function ([rotulo, valor, classe]) {
        const cartao = document.createElement('div');
        cartao.classList.add('cartao-indicador');
        const titulo = document.createElement('span');
        titulo.classList.add('cartao-indicador-titulo');
        titulo.textContent = rotulo;
        const numero = document.createElement('span');
        numero.classList.add('cartao-indicador-valor');
        if (classe) numero.classList.add(classe);
        numero.textContent = valor;
        cartao.appendChild(titulo);
        cartao.appendChild(numero);
        area.appendChild(cartao);
    });
    return area;
}

// colunasNumericas: índices alinhados à direita
function criarSecao(titulo, cabecalho, linhas, textoVazio, colunasNumericas) {
    const secao = document.createElement('section');
    secao.classList.add('secao-relatorio');
    const h3 = document.createElement('h3');
    h3.textContent = titulo;
    secao.appendChild(h3);

    if (linhas.length === 0) {
        const vazio = document.createElement('p');
        vazio.classList.add('texto-vazio-relatorio');
        vazio.textContent = textoVazio;
        secao.appendChild(vazio);
        return secao;
    }

    const area = document.createElement('div');
    area.classList.add('area-tabela');
    const tabela = document.createElement('table');
    tabela.classList.add('tabela-responsiva', 'tabela-relatorio');
    const thead = document.createElement('thead');
    const linhaCabecalho = document.createElement('tr');
    cabecalho.forEach(function (texto, indice) {
        const th = document.createElement('th');
        th.textContent = texto;
        if ((colunasNumericas || []).includes(indice)) th.classList.add('numero');
        linhaCabecalho.appendChild(th);
    });
    thead.appendChild(linhaCabecalho);
    const tbody = document.createElement('tbody');
    linhas.forEach(function (valores) {
        const tr = document.createElement('tr');
        valores.forEach(function (valor, indice) {
            const td = document.createElement('td');
            td.setAttribute('data-rotulo', cabecalho[indice]);
            td.textContent = valor === '' || valor === null || valor === undefined ? '—' : valor;
            if ((colunasNumericas || []).includes(indice)) td.classList.add('numero');
            tr.appendChild(td);
        });
        tbody.appendChild(tr);
    });
    tabela.appendChild(thead);
    tabela.appendChild(tbody);
    area.appendChild(tabela);
    secao.appendChild(area);
    return secao;
}

// Destaca as linhas que pedem atenção (ex.: vencidos)
function marcarLinhas(secao, destaques) {
    secao.querySelectorAll('tbody tr').forEach(function (tr, indice) {
        tr.classList.toggle('linha-destaque', Boolean(destaques[indice]));
    });
}

/* ---------- CSV ---------- */

// Número no padrão brasileiro para o Excel: 1234.5 -> "1234,50"
function numeroCsv(valor) {
    return Number(valor || 0).toFixed(2).replace('.', ',');
}

function exportarCsv() {
    if (!dadosCsv) return;
    // Ponto e vírgula + BOM: o Excel em português abre com as colunas e acentos certos
    const texto = dadosCsv.linhas.map(function (linha) {
        return linha.map(function (valor) {
            const celula = String(valor === null || valor === undefined ? '' : valor);
            return /[;"\n\r]/.test(celula) ? '"' + celula.replace(/"/g, '""') + '"' : celula;
        }).join(';');
    }).join('\r\n');
    const arquivo = new Blob(['﻿' + texto], { type: 'text/csv;charset=utf-8' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(arquivo);
    link.download = dadosCsv.nomeArquivo;
    document.body.appendChild(link);
    link.click();
    link.remove();
    setTimeout(function () { URL.revokeObjectURL(link.href); }, 1000);
}

/* ---------- Utilitários ---------- */

function moeda(valor) {
    return formatoMoedaRel.format(Number(valor || 0));
}

function dataParaTexto(data) {
    return data.getFullYear() + '-' + String(data.getMonth() + 1).padStart(2, '0') + '-' + String(data.getDate()).padStart(2, '0');
}

function formatarData(valor) {
    if (!valor) return '';
    const partes = String(valor).split('T')[0].split('-');
    return partes[2] + '/' + partes[1] + '/' + partes[0];
}

function exibirMensagem(texto, tipo) {
    const mensagem = document.createElement('div');
    mensagem.classList.add('mensagem-toast', tipo);
    mensagem.textContent = texto;
    elemento('containerMensagens').appendChild(mensagem);
    setTimeout(function () { mensagem.remove(); }, 5000);
}
