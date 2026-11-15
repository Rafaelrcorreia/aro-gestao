// Configurações do sistema. Todos veem; só administradores salvam (o servidor também garante).

// Nome do campo na API -> id do campo na tela
const camposConfiguracao = {
    nomeEmpresa: 'inputNomeEmpresa',
    cnpj: 'inputCnpj',
    telefone: 'inputTelefone',
    email: 'inputEmail',
    endereco: 'inputEndereco',
    cidade: 'inputCidade',
    estado: 'inputEstado',
    diasAvisoDocumentos: 'inputDiasAvisoDocumentos',
    diasAlertaPrazo: 'inputDiasAlertaPrazo'
};

function elemento(id) {
    return document.getElementById(id);
}

document.addEventListener('DOMContentLoaded', async function () {
    elemento('formularioConfiguracoes').addEventListener('submit', salvar);
    elemento('inputEstado').addEventListener('input', function () {
        this.value = this.value.replace(/[^a-z]/gi, '').toUpperCase().slice(0, 2);
    });

    try {
        const [configuracoes, usuario] = await Promise.all([buscarConfiguracoes(), buscarUsuarioLogado()]);
        preencher(configuracoes);
        if (usuario.papel !== 'ADMIN') {
            ativarSomenteLeitura();
        }
        elemento('formularioConfiguracoes').classList.remove('oculto');
    } catch (erro) {
        exibirMensagem(erro.mensagem || 'Não foi possível carregar as configurações.', 'erro');
    } finally {
        elemento('indicadorCarregamento').classList.add('oculto');
    }
});

function preencher(configuracoes) {
    Object.entries(camposConfiguracao).forEach(function ([campo, idCampo]) {
        elemento(idCampo).value = configuracoes[campo] ?? '';
    });
    const texto = elemento('textoUltimaAlteracao');
    if (configuracoes.atualizadoEm) {
        const partes = configuracoes.atualizadoEm.split('T');
        const data = partes[0].split('-').reverse().join('/');
        const hora = (partes[1] || '').slice(0, 5);
        texto.textContent = 'Última alteração: ' + data + ' ' + hora
            + (configuracoes.atualizadoPor ? ' por ' + configuracoes.atualizadoPor : '');
    } else {
        texto.textContent = 'Usando os valores padrão (ainda não foram salvas).';
    }
}

function ativarSomenteLeitura() {
    elemento('avisoSomenteLeitura').classList.remove('oculto');
    elemento('btnSalvarConfiguracoes').classList.add('oculto');
    Object.values(camposConfiguracao).forEach(function (idCampo) {
        elemento(idCampo).disabled = true;
    });
}

function limparErros() {
    document.querySelectorAll('#formularioConfiguracoes .mensagem-erro-campo').forEach(function (campo) {
        campo.textContent = '';
    });
}

function marcarErro(campo, mensagem) {
    const id = 'erro' + campo.charAt(0).toUpperCase() + campo.slice(1);
    const destino = elemento(id);
    if (!destino) return false;
    destino.textContent = mensagem;
    return true;
}

function inteiroEntre(texto, minimo, maximo) {
    if (!/^\d+$/.test(String(texto).trim())) return null;
    const numero = Number(texto);
    return numero >= minimo && numero <= maximo ? numero : null;
}

async function salvar(evento) {
    evento.preventDefault();
    const botao = elemento('btnSalvarConfiguracoes');
    if (botao.disabled) return;
    limparErros();

    const dados = {};
    Object.entries(camposConfiguracao).forEach(function ([campo, idCampo]) {
        dados[campo] = elemento(idCampo).value.trim();
    });

    let valido = true;
    if (!dados.nomeEmpresa) {
        marcarErro('nomeEmpresa', 'Informe o nome da empresa');
        valido = false;
    }
    if (dados.estado && !/^[A-Z]{2}$/.test(dados.estado)) {
        marcarErro('estado', 'Use a sigla com 2 letras');
        valido = false;
    }
    dados.diasAvisoDocumentos = inteiroEntre(dados.diasAvisoDocumentos, 1, 365);
    if (dados.diasAvisoDocumentos === null) {
        marcarErro('diasAvisoDocumentos', 'Informe um número inteiro de 1 a 365');
        valido = false;
    }
    dados.diasAlertaPrazo = inteiroEntre(dados.diasAlertaPrazo, 0, 60);
    if (dados.diasAlertaPrazo === null) {
        marcarErro('diasAlertaPrazo', 'Informe um número inteiro de 0 a 60');
        valido = false;
    }
    if (!valido) return;

    botao.disabled = true;
    try {
        const salvas = await salvarConfiguracoes(dados);
        preencher(salvas);
        // As outras telas passam a usar os valores novos
        promessaConfiguracoes = null;
        exibirMensagem('Configurações salvas.', 'sucesso');
    } catch (erro) {
        let marcou = false;
        Object.entries(erro.erros || {}).forEach(function ([campo, mensagem]) {
            if (marcarErro(campo, mensagem)) marcou = true;
        });
        if (!marcou) {
            exibirMensagem(erro.mensagem || 'Não foi possível salvar as configurações.', 'erro');
        }
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
