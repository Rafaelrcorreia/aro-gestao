// Tela de login. Se o sistema ainda não tem usuários, mostra o cadastro do primeiro administrador.

function elemento(id) {
    return document.getElementById(id);
}

// Para onde ir depois de entrar. Só aceita caminhos deste mesmo site (evita redirecionar para fora).
function destinoDepoisDoLogin() {
    const voltar = new URLSearchParams(window.location.search).get('voltar');
    if (voltar && voltar.startsWith('/') && !voltar.startsWith('//') && !voltar.startsWith('/login.html')) {
        return voltar;
    }
    return '/index.html';
}

document.addEventListener('DOMContentLoaded', async function () {
    elemento('formularioLogin').addEventListener('submit', fazerLogin);
    elemento('formularioPrimeiroAcesso').addEventListener('submit', criarAdministrador);

    try {
        const status = await buscarStatusAutenticacao();
        if (status && status.configurado === false) {
            elemento('formularioPrimeiroAcesso').classList.remove('oculto');
            elemento('inputNomeAdmin').focus();
        } else {
            elemento('formularioLogin').classList.remove('oculto');
            elemento('inputLogin').focus();
        }
    } catch (erro) {
        elemento('formularioLogin').classList.remove('oculto');
        elemento('erroLogin').textContent = erro.mensagem || 'Não foi possível falar com o servidor.';
    } finally {
        elemento('indicadorCarregamento').classList.add('oculto');
    }
});

async function fazerLogin(evento) {
    evento.preventDefault();
    const botao = elemento('btnEntrar');
    if (botao.disabled) return;
    const login = elemento('inputLogin').value.trim();
    const senha = elemento('inputSenha').value;
    elemento('erroLogin').textContent = '';

    if (!login || !senha) {
        elemento('erroLogin').textContent = 'Informe o login e a senha.';
        return;
    }

    botao.disabled = true;
    botao.textContent = 'Entrando...';
    try {
        await entrar(login, senha);
        window.location.href = destinoDepoisDoLogin();
    } catch (erro) {
        elemento('erroLogin').textContent = erro.mensagem || 'Não foi possível entrar.';
        elemento('inputSenha').value = '';
        elemento('inputSenha').focus();
        botao.disabled = false;
        botao.textContent = 'Entrar';
    }
}

async function criarAdministrador(evento) {
    evento.preventDefault();
    const botao = elemento('btnCriarAdmin');
    if (botao.disabled) return;

    ['erroNome', 'erroLoginAdmin', 'erroSenhaAdmin', 'erroConfirmarSenhaAdmin', 'erroPrimeiroAcesso']
        .forEach(function (id) { elemento(id).textContent = ''; });

    const dados = {
        nome: elemento('inputNomeAdmin').value.trim(),
        login: elemento('inputLoginAdmin').value.trim(),
        senha: elemento('inputSenhaAdmin').value
    };
    let valido = true;
    if (!dados.nome) {
        elemento('erroNome').textContent = 'Informe o seu nome';
        valido = false;
    }
    if (!/^[A-Za-z0-9._@-]{3,50}$/.test(dados.login)) {
        elemento('erroLoginAdmin').textContent = 'Use de 3 a 50 caracteres: letras, números, ponto, hífen, sublinhado ou @';
        valido = false;
    }
    if (dados.senha.length < 8) {
        elemento('erroSenhaAdmin').textContent = 'A senha precisa ter pelo menos 8 caracteres';
        valido = false;
    }
    if (dados.senha !== elemento('inputConfirmarSenhaAdmin').value) {
        elemento('erroConfirmarSenhaAdmin').textContent = 'As senhas não conferem';
        valido = false;
    }
    if (!valido) return;

    botao.disabled = true;
    try {
        await criarPrimeiroAdministrador(dados);
        await entrar(dados.login, dados.senha);
        window.location.href = destinoDepoisDoLogin();
    } catch (erro) {
        const mapa = { nome: 'erroNome', login: 'erroLoginAdmin', senha: 'erroSenhaAdmin' };
        let marcou = false;
        Object.entries(erro.erros || {}).forEach(function ([campo, mensagem]) {
            if (mapa[campo]) {
                elemento(mapa[campo]).textContent = mensagem;
                marcou = true;
            }
        });
        if (!marcou) {
            elemento('erroPrimeiroAcesso').textContent = erro.mensagem || 'Não foi possível criar o administrador.';
        }
        botao.disabled = false;
    }
}
