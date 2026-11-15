// Gestão de usuários (somente administradores; o servidor também garante isso).

let usuarioEmEdicaoId = null;
let usuarioParaRedefinirId = null;

function elemento(id) {
    return document.getElementById(id);
}

document.addEventListener('DOMContentLoaded', function () {
    elemento('btnNovoUsuario').addEventListener('click', abrirModalNovo);
    elemento('btnFecharModalFormulario').addEventListener('click', fecharModalFormulario);
    elemento('btnCancelarFormulario').addEventListener('click', fecharModalFormulario);
    elemento('formularioUsuario').addEventListener('submit', salvarUsuario);
    elemento('modalFormulario').addEventListener('close', function () {
        elemento('formularioUsuario').reset();
        limparErros('formularioUsuario');
        usuarioEmEdicaoId = null;
    });

    elemento('formularioRedefinirSenha').addEventListener('submit', confirmarRedefinicao);
    elemento('btnFecharModalRedefinir').addEventListener('click', fecharModalRedefinir);
    elemento('btnCancelarRedefinir').addEventListener('click', fecharModalRedefinir);
    elemento('modalRedefinirSenha').addEventListener('close', function () {
        elemento('formularioRedefinirSenha').reset();
        limparErros('formularioRedefinirSenha');
        usuarioParaRedefinirId = null;
    });

    carregarUsuarios();
});

async function carregarUsuarios() {
    elemento('indicadorCarregamento').classList.remove('oculto');
    try {
        const usuarios = await listarUsuarios();
        renderizarUsuarios(usuarios || []);
    } catch (erro) {
        if (erro.status === 403) {
            // Usuário comum chegou aqui pelo endereço: mostra o aviso e esconde o resto
            elemento('avisoSemPermissao').classList.remove('oculto');
            elemento('areaUsuarios').classList.add('oculto');
            elemento('btnNovoUsuario').classList.add('oculto');
        } else {
            exibirMensagem(erro.mensagem || 'Não foi possível carregar os usuários.', 'erro');
        }
    } finally {
        elemento('indicadorCarregamento').classList.add('oculto');
    }
}

function renderizarUsuarios(usuarios) {
    const corpo = elemento('corpoTabelaUsuarios');
    corpo.replaceChildren();

    usuarios.forEach(function (usuario) {
        const linha = document.createElement('tr');
        const ehVoce = typeof usuarioLogado !== 'undefined' && usuarioLogado && usuarioLogado.id === usuario.id;

        linha.appendChild(criarCelula('Nome', usuario.nome + (ehVoce ? ' (você)' : '')));
        linha.appendChild(criarCelula('Login', usuario.login));
        linha.appendChild(criarCelula('Papel', usuario.papel === 'ADMIN' ? 'Administrador' : 'Usuário'));

        const celulaSituacao = criarCelula('Situação', '');
        const selo = document.createElement('span');
        selo.classList.add('selo-status', usuario.ativo ? 'status-ativo' : 'status-inativo');
        selo.textContent = usuario.ativo ? 'Ativo' : 'Desativado';
        celulaSituacao.replaceChildren(selo);
        linha.appendChild(celulaSituacao);

        linha.appendChild(criarCelula('Cadastro', formatarDataHora(usuario.dataCadastro)));

        const celulaAcoes = criarCelula('Ações', '');
        celulaAcoes.textContent = '';
        const container = document.createElement('div');
        container.classList.add('acoes-tabela');
        [
            ['Editar', function () { abrirModalEdicao(usuario); }],
            ['Redefinir senha', function () { abrirModalRedefinir(usuario); }]
        ].forEach(function (acao) {
            const botao = document.createElement('button');
            botao.type = 'button';
            botao.classList.add('botao-icone');
            botao.textContent = acao[0];
            botao.addEventListener('click', acao[1]);
            container.appendChild(botao);
        });
        celulaAcoes.appendChild(container);
        linha.appendChild(celulaAcoes);

        corpo.appendChild(linha);
    });
}

function criarCelula(rotulo, valor) {
    const celula = document.createElement('td');
    celula.setAttribute('data-rotulo', rotulo);
    celula.textContent = valor || '—';
    return celula;
}

// "2026-11-15T10:30:00" -> "15/11/2026"
function formatarDataHora(valor) {
    if (!valor) return '';
    const partes = valor.split('T')[0].split('-');
    return partes[2] + '/' + partes[1] + '/' + partes[0];
}

/* ---------- Criar / editar ---------- */

function abrirModalNovo() {
    usuarioEmEdicaoId = null;
    elemento('formularioUsuario').reset();
    limparErros('formularioUsuario');
    elemento('tituloModalFormulario').textContent = 'Novo usuário';
    elemento('grupoSenhaInicial').classList.remove('oculto');
    elemento('selectPapel').value = 'USUARIO';
    elemento('selectAtivo').value = 'true';
    elemento('modalFormulario').showModal();
    elemento('inputNome').focus();
}

function abrirModalEdicao(usuario) {
    usuarioEmEdicaoId = usuario.id;
    elemento('formularioUsuario').reset();
    limparErros('formularioUsuario');
    elemento('tituloModalFormulario').textContent = 'Editar usuário';
    // A senha de quem já existe é trocada por "Redefinir senha"
    elemento('grupoSenhaInicial').classList.add('oculto');
    elemento('inputNome').value = usuario.nome;
    elemento('inputLogin').value = usuario.login;
    elemento('selectPapel').value = usuario.papel;
    elemento('selectAtivo').value = String(usuario.ativo);
    elemento('modalFormulario').showModal();
}

function fecharModalFormulario() {
    elemento('modalFormulario').close();
}

async function salvarUsuario(evento) {
    evento.preventDefault();
    const botao = elemento('btnSalvarUsuario');
    if (botao.disabled) return;
    limparErros('formularioUsuario');

    const criando = usuarioEmEdicaoId === null;
    const dados = {
        nome: elemento('inputNome').value.trim(),
        login: elemento('inputLogin').value.trim(),
        papel: elemento('selectPapel').value,
        ativo: elemento('selectAtivo').value === 'true'
    };

    let valido = true;
    if (!dados.nome) {
        elemento('erroNome').textContent = 'Informe o nome';
        valido = false;
    }
    if (!/^[A-Za-z0-9._@-]{3,50}$/.test(dados.login)) {
        elemento('erroLogin').textContent = 'Use de 3 a 50 caracteres: letras, números, ponto, hífen, sublinhado ou @';
        valido = false;
    }
    if (criando) {
        dados.senha = elemento('inputSenha').value;
        if (dados.senha.length < 8) {
            elemento('erroSenha').textContent = 'A senha precisa ter pelo menos 8 caracteres';
            valido = false;
        }
        if (dados.senha !== elemento('inputConfirmarSenha').value) {
            elemento('erroConfirmarSenha').textContent = 'As senhas não conferem';
            valido = false;
        }
    }
    if (!valido) return;

    botao.disabled = true;
    try {
        if (criando) {
            await criarUsuario(dados);
        } else {
            await atualizarUsuario(usuarioEmEdicaoId, dados);
        }
        fecharModalFormulario();
        exibirMensagem('Usuário salvo com sucesso.', 'sucesso');
        carregarUsuarios();
    } catch (erro) {
        const mapa = { nome: 'erroNome', login: 'erroLogin', papel: 'erroPapel', senha: 'erroSenha' };
        let marcou = false;
        Object.entries(erro.erros || {}).forEach(function ([campo, mensagem]) {
            if (mapa[campo]) {
                elemento(mapa[campo]).textContent = mensagem;
                marcou = true;
            }
        });
        if (!marcou) {
            exibirMensagem(erro.mensagem || 'Não foi possível salvar o usuário.', 'erro');
        }
    } finally {
        botao.disabled = false;
    }
}

/* ---------- Redefinir senha ---------- */

function abrirModalRedefinir(usuario) {
    usuarioParaRedefinirId = usuario.id;
    elemento('textoRedefinirSenha').textContent = 'Nova senha para ' + usuario.nome + ' (' + usuario.login + ').'
        + ' Se a pessoa estiver usando o sistema, ela precisará entrar de novo.';
    elemento('modalRedefinirSenha').showModal();
    elemento('inputNovaSenhaUsuario').focus();
}

function fecharModalRedefinir() {
    elemento('modalRedefinirSenha').close();
}

async function confirmarRedefinicao(evento) {
    evento.preventDefault();
    const botao = elemento('btnConfirmarRedefinir');
    if (botao.disabled || usuarioParaRedefinirId === null) return;
    limparErros('formularioRedefinirSenha');

    const nova = elemento('inputNovaSenhaUsuario').value;
    if (nova.length < 8) {
        elemento('erroNovaSenhaUsuario').textContent = 'A senha precisa ter pelo menos 8 caracteres';
        return;
    }
    if (nova !== elemento('inputConfirmarNovaSenhaUsuario').value) {
        elemento('erroConfirmarNovaSenhaUsuario').textContent = 'As senhas não conferem';
        return;
    }

    botao.disabled = true;
    try {
        await redefinirSenhaUsuario(usuarioParaRedefinirId, nova);
        fecharModalRedefinir();
        exibirMensagem('Senha redefinida.', 'sucesso');
    } catch (erro) {
        elemento('erroNovaSenhaUsuario').textContent =
            (erro.erros && erro.erros.novaSenha) || erro.mensagem || 'Não foi possível redefinir a senha.';
    } finally {
        botao.disabled = false;
    }
}

function limparErros(idFormulario) {
    document.querySelectorAll('#' + idFormulario + ' .mensagem-erro-campo').forEach(function (campo) {
        campo.textContent = '';
    });
}

function exibirMensagem(texto, tipo) {
    const mensagem = document.createElement('div');
    mensagem.classList.add('mensagem-toast', tipo);
    mensagem.textContent = texto;
    elemento('containerMensagens').appendChild(mensagem);
    setTimeout(function () { mensagem.remove(); }, 5000);
}
