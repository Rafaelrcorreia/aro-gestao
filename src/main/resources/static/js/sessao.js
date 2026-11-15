// Incluído em todas as páginas logadas, logo depois do api.js.
// Mostra quem está logado no rodapé do menu, com "Alterar senha" e "Sair",
// e exibe o item "Usuários" só para administradores.

let usuarioLogado = null;

// Valores usados enquanto a configuração não chega (ou se a consulta falhar)
const CONFIGURACOES_PADRAO = { nomeEmpresa: 'ARO Assessoria', diasAvisoDocumentos: 30, diasAlertaPrazo: 3 };
let promessaConfiguracoes = null;

// As telas chamam obterConfiguracoes() para saber, por exemplo, quantos dias de aviso usar
function obterConfiguracoes() {
    if (!promessaConfiguracoes) {
        promessaConfiguracoes = buscarConfiguracoes()
            .then(function (configuracoes) { return Object.assign({}, CONFIGURACOES_PADRAO, configuracoes); })
            .catch(function () { return Object.assign({}, CONFIGURACOES_PADRAO); });
    }
    return promessaConfiguracoes;
}

document.addEventListener('DOMContentLoaded', async function () {
    try {
        usuarioLogado = await buscarUsuarioLogado();
    } catch (erro) {
        // 401 já leva para o login dentro do requisitar; outros erros só deixam o rodapé vazio
        return;
    }
    montarRodapeDoMenu(usuarioLogado);
    document.querySelectorAll('.somente-admin').forEach(function (item) {
        item.classList.toggle('oculto', usuarioLogado.papel !== 'ADMIN');
    });
});

function montarRodapeDoMenu(usuario) {
    const menu = document.getElementById('menuLateral');
    if (!menu) return;

    const rodape = document.createElement('div');
    rodape.classList.add('menu-rodape');

    const nome = document.createElement('span');
    nome.classList.add('menu-usuario-nome');
    nome.textContent = usuario.nome;

    const papel = document.createElement('span');
    papel.classList.add('menu-usuario-papel');
    papel.textContent = usuario.papel === 'ADMIN' ? 'Administrador' : 'Usuário';

    const acoes = document.createElement('div');
    acoes.classList.add('menu-usuario-acoes');

    const botaoSenha = document.createElement('button');
    botaoSenha.type = 'button';
    botaoSenha.classList.add('menu-usuario-botao');
    botaoSenha.textContent = 'Alterar senha';
    botaoSenha.addEventListener('click', abrirModalMinhaSenha);

    const botaoSair = document.createElement('button');
    botaoSair.type = 'button';
    botaoSair.classList.add('menu-usuario-botao');
    botaoSair.textContent = 'Sair';
    botaoSair.addEventListener('click', async function () {
        botaoSair.disabled = true;
        try {
            await sair();
        } catch (erro) {
            // mesmo com erro, voltamos para o login
        }
        window.location.href = '/login.html';
    });

    acoes.appendChild(botaoSenha);
    acoes.appendChild(botaoSair);
    rodape.appendChild(nome);
    rodape.appendChild(papel);
    rodape.appendChild(acoes);
    menu.appendChild(rodape);
}

/* ---------- Alterar a própria senha ---------- */

function criarCampoSenha(id, rotulo, autocomplete) {
    const campo = document.createElement('div');
    campo.classList.add('campo-formulario');
    const label = document.createElement('label');
    label.htmlFor = id;
    label.textContent = rotulo;
    const input = document.createElement('input');
    input.type = 'password';
    input.id = id;
    input.maxLength = 100;
    input.autocomplete = autocomplete;
    const erro = document.createElement('span');
    erro.classList.add('mensagem-erro-campo');
    erro.id = id + 'Erro';
    campo.appendChild(label);
    campo.appendChild(input);
    campo.appendChild(erro);
    return campo;
}

function obterModalMinhaSenha() {
    let modal = document.getElementById('modalMinhaSenha');
    if (modal) return modal;

    modal = document.createElement('dialog');
    modal.id = 'modalMinhaSenha';
    modal.classList.add('modal', 'modal-pequeno');

    const form = document.createElement('form');
    form.method = 'dialog';
    form.noValidate = true;

    const cabecalho = document.createElement('div');
    cabecalho.classList.add('modal-cabecalho');
    const titulo = document.createElement('h2');
    titulo.textContent = 'Alterar minha senha';
    cabecalho.appendChild(titulo);

    const corpo = document.createElement('div');
    corpo.classList.add('modal-corpo');
    corpo.appendChild(criarCampoSenha('inputSenhaAtual', 'Senha atual', 'current-password'));
    corpo.appendChild(criarCampoSenha('inputNovaSenha', 'Nova senha (mínimo 8 caracteres)', 'new-password'));
    corpo.appendChild(criarCampoSenha('inputConfirmarNovaSenha', 'Confirmar nova senha', 'new-password'));

    const rodape = document.createElement('div');
    rodape.classList.add('modal-rodape');
    const cancelar = document.createElement('button');
    cancelar.type = 'button';
    cancelar.classList.add('botao', 'botao-secundario');
    cancelar.textContent = 'Cancelar';
    cancelar.addEventListener('click', function () { modal.close(); });
    const salvar = document.createElement('button');
    salvar.type = 'submit';
    salvar.classList.add('botao', 'botao-primario');
    salvar.textContent = 'Salvar';
    rodape.appendChild(cancelar);
    rodape.appendChild(salvar);

    form.appendChild(cabecalho);
    form.appendChild(corpo);
    form.appendChild(rodape);
    modal.appendChild(form);
    document.body.appendChild(modal);

    form.addEventListener('submit', async function (evento) {
        evento.preventDefault();
        if (salvar.disabled) return;
        ['inputSenhaAtualErro', 'inputNovaSenhaErro', 'inputConfirmarNovaSenhaErro']
            .forEach(function (id) { document.getElementById(id).textContent = ''; });

        const atual = document.getElementById('inputSenhaAtual').value;
        const nova = document.getElementById('inputNovaSenha').value;
        const confirmar = document.getElementById('inputConfirmarNovaSenha').value;
        let valido = true;
        if (!atual) {
            document.getElementById('inputSenhaAtualErro').textContent = 'Informe a senha atual';
            valido = false;
        }
        if (nova.length < 8) {
            document.getElementById('inputNovaSenhaErro').textContent = 'A nova senha precisa ter pelo menos 8 caracteres';
            valido = false;
        }
        if (nova !== confirmar) {
            document.getElementById('inputConfirmarNovaSenhaErro').textContent = 'As senhas não conferem';
            valido = false;
        }
        if (!valido) return;

        salvar.disabled = true;
        try {
            await alterarMinhaSenha(atual, nova);
            modal.close();
            mostrarAvisoSessao('Senha alterada com sucesso.', 'sucesso');
        } catch (erro) {
            const destino = erro.erros && erro.erros.novaSenha ? 'inputNovaSenhaErro' : 'inputSenhaAtualErro';
            document.getElementById(destino).textContent =
                (erro.erros && erro.erros.novaSenha) || erro.mensagem || 'Não foi possível alterar a senha.';
        } finally {
            salvar.disabled = false;
        }
    });

    modal.addEventListener('close', function () { form.reset(); });
    return modal;
}

function abrirModalMinhaSenha() {
    obterModalMinhaSenha().showModal();
    document.getElementById('inputSenhaAtual').focus();
}

function mostrarAvisoSessao(texto, tipo) {
    const container = document.getElementById('containerMensagens');
    if (!container) return;
    const aviso = document.createElement('div');
    aviso.classList.add('mensagem-toast', tipo);
    aviso.textContent = texto;
    container.appendChild(aviso);
    setTimeout(function () { aviso.remove(); }, 4000);
}
