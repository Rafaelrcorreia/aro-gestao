package com.aro.aro_gestao.service;

import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.exception.ResourceNotFoundException;
import com.aro.aro_gestao.service.ArmazenamentoDocumentosService.ArquivoSalvo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmazenamentoDocumentosServiceTest {

    @TempDir
    Path pastaTemporaria;

    private ArmazenamentoDocumentosService armazenamento;
    private Path pastaDocumentos;

    @BeforeEach
    void configurar() {
        // A pasta ainda nao existe: o servico deve cria-la no primeiro envio
        pastaDocumentos = pastaTemporaria.resolve("documentos");
        armazenamento = new ArmazenamentoDocumentosService(pastaDocumentos.toString());
    }

    private MockMultipartFile arquivo(String nome, String conteudo) {
        return new MockMultipartFile("arquivo", nome, "application/octet-stream",
                conteudo.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void deveSalvarComNomeAleatorioEGuardarNomeOriginal() throws Exception {
        ArquivoSalvo salvo = armazenamento.salvar(arquivo("Matrícula 123.PDF", "conteudo"));

        assertEquals("Matrícula 123.PDF", salvo.nomeOriginal());
        assertEquals(8L, salvo.tamanho());
        assertTrue(salvo.nomeArmazenado().matches("[0-9a-f-]{36}\\.pdf"));
        Path caminho = armazenamento.caminhoDe(salvo.nomeArmazenado());
        assertEquals(pastaDocumentos.toAbsolutePath().normalize(), caminho.getParent());
        assertEquals("conteudo", Files.readString(caminho));
    }

    @Test
    void deveDescartarCaminhoEnviadoNoNomeDoArquivo() {
        ArquivoSalvo salvo = armazenamento.salvar(arquivo("..\\..\\C:\\pasta\\planta.dwg", "x"));

        assertEquals("planta.dwg", salvo.nomeOriginal());
        assertTrue(salvo.nomeArmazenado().endsWith(".dwg"));
    }

    @Test
    void deveRecusarExtensaoNaoPermitida() {
        assertThrows(BusinessException.class, () -> armazenamento.salvar(arquivo("pagina.html", "<script>")));
        assertThrows(BusinessException.class, () -> armazenamento.salvar(arquivo("sem-extensao", "x")));
        assertThrows(BusinessException.class, () -> armazenamento.salvar(arquivo("programa.exe", "x")));
    }

    @Test
    void deveRecusarArquivoVazio() {
        assertThrows(BusinessException.class, () -> armazenamento.salvar(arquivo("vazio.pdf", "")));
    }

    @Test
    void naoDevePermitirSairDaPastaDeDocumentos() {
        assertThrows(BusinessException.class, () -> armazenamento.caminhoDe("../segredo.txt"));
        assertThrows(BusinessException.class, () -> armazenamento.caminhoDe("sub/arquivo.pdf"));
    }

    @Test
    void deveExcluirArquivoEIgnorarArquivoInexistente() {
        ArquivoSalvo salvo = armazenamento.salvar(arquivo("foto.png", "imagem"));

        armazenamento.excluir(salvo.nomeArmazenado());
        armazenamento.excluir(salvo.nomeArmazenado());
        armazenamento.excluir(null);

        assertFalse(Files.exists(pastaDocumentos.resolve(salvo.nomeArmazenado())));
        assertThrows(ResourceNotFoundException.class, () -> armazenamento.caminhoDe(salvo.nomeArmazenado()));
    }

    @Test
    void soPdfEImagensSaoExibidosNoNavegador() {
        assertEquals("application/pdf", armazenamento.tipoExibivel("abc.pdf"));
        assertEquals("image/jpeg", armazenamento.tipoExibivel("abc.JPG"));
        assertNull(armazenamento.tipoExibivel("abc.docx"));
        assertNull(armazenamento.tipoExibivel("abc.txt"));
    }
}
