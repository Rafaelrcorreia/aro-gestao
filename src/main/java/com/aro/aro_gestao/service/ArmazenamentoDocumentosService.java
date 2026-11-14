package com.aro.aro_gestao.service;

import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Guarda os arquivos dos documentos numa pasta do servidor.
 * No disco, cada arquivo recebe um nome aleatorio (UUID): o nome original
 * enviado pelo usuario nunca e usado para montar caminhos.
 */
@Service
public class ArmazenamentoDocumentosService {

    private static final Logger log = LoggerFactory.getLogger(ArmazenamentoDocumentosService.class);

    // Extensoes aceitas. Arquivos que o navegador poderia executar (html, svg, js...) ficam de fora.
    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of(
            "pdf", "jpg", "jpeg", "png", "webp", "gif",
            "doc", "docx", "xls", "xlsx", "odt", "ods", "txt", "csv",
            "zip", "rar", "7z",
            "kml", "kmz", "dwg", "dxf", "shp", "dbf", "shx", "prj");

    // So estes tipos sao abertos direto no navegador; o resto e sempre baixado
    private static final Map<String, String> TIPOS_EXIBIVEIS = Map.of(
            "pdf", "application/pdf",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp",
            "gif", "image/gif");

    private final Path diretorio;

    public ArmazenamentoDocumentosService(@Value("${aro.documentos.diretorio}") String diretorio) {
        this.diretorio = Path.of(diretorio).toAbsolutePath().normalize();
    }

    public record ArquivoSalvo(String nomeArmazenado, String nomeOriginal, long tamanho) {
    }

    public ArquivoSalvo salvar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new BusinessException("O arquivo enviado esta vazio.");
        }
        String nomeOriginal = limparNomeOriginal(arquivo.getOriginalFilename());
        String extensao = extensaoDe(nomeOriginal);
        if (!EXTENSOES_PERMITIDAS.contains(extensao)) {
            throw new BusinessException("Tipo de arquivo nao permitido. Envie PDF, imagem, documento do "
                    + "Office/LibreOffice, planilha, arquivo compactado ou arquivo de mapa (KML, KMZ, DWG, DXF, SHP).");
        }

        String nomeArmazenado = UUID.randomUUID() + "." + extensao;
        Path destino = resolver(nomeArmazenado);
        try (InputStream entrada = arquivo.getInputStream()) {
            Files.createDirectories(diretorio);
            Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new UncheckedIOException("Falha ao gravar o arquivo do documento", ex);
        }
        return new ArquivoSalvo(nomeArmazenado, nomeOriginal, arquivo.getSize());
    }

    public Path caminhoDe(String nomeArmazenado) {
        Path caminho = resolver(nomeArmazenado);
        if (!Files.isRegularFile(caminho)) {
            throw new ResourceNotFoundException(
                    "O arquivo deste documento nao foi encontrado no servidor.");
        }
        return caminho;
    }

    // Apagar e "melhor esforco": se falhar, o documento ja foi atualizado/excluido no banco
    public void excluir(String nomeArmazenado) {
        if (nomeArmazenado == null) {
            return;
        }
        try {
            Files.deleteIfExists(resolver(nomeArmazenado));
        } catch (IOException | RuntimeException ex) {
            log.warn("Nao foi possivel apagar o arquivo {}", nomeArmazenado, ex);
        }
    }

    /** Tipo para abrir no navegador, ou null quando o arquivo deve ser baixado. */
    public String tipoExibivel(String nomeArmazenado) {
        return TIPOS_EXIBIVEIS.get(extensaoDe(nomeArmazenado));
    }

    private Path resolver(String nomeArmazenado) {
        Path caminho = diretorio.resolve(nomeArmazenado).normalize();
        // Garante que nunca saimos da pasta de documentos
        if (!caminho.getParent().equals(diretorio)) {
            throw new BusinessException("Nome de arquivo invalido.");
        }
        return caminho;
    }

    private static String limparNomeOriginal(String nome) {
        if (nome == null || nome.isBlank()) {
            return "arquivo";
        }
        // Navegadores antigos mandam o caminho completo; fica so o nome
        String semCaminho = nome.replace('\\', '/');
        semCaminho = semCaminho.substring(semCaminho.lastIndexOf('/') + 1).strip();
        // Remove caracteres de controle
        semCaminho = semCaminho.replaceAll("\\p{Cntrl}", "");
        if (semCaminho.isEmpty()) {
            return "arquivo";
        }
        return semCaminho.length() > 255 ? semCaminho.substring(semCaminho.length() - 255) : semCaminho;
    }

    private static String extensaoDe(String nome) {
        int ponto = nome.lastIndexOf('.');
        return ponto < 0 ? "" : nome.substring(ponto + 1).toLowerCase(Locale.ROOT);
    }
}
