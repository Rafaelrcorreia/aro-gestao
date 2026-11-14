package com.aro.aro_gestao.controller;

import com.aro.aro_gestao.dto.DocumentoRequestDTO;
import com.aro.aro_gestao.dto.DocumentoResponseDTO;
import com.aro.aro_gestao.entity.TipoDocumento;
import com.aro.aro_gestao.service.DocumentoService;
import com.aro.aro_gestao.service.DocumentoService.ArquivoDocumento;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/documentos")
public class DocumentoController {

    private final DocumentoService documentoService;

    public DocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    // multipart/form-data: parte "dados" (JSON) e parte opcional "arquivo"
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoResponseDTO> criar(
            @Valid @RequestPart("dados") DocumentoRequestDTO dto,
            @RequestPart(name = "arquivo", required = false) MultipartFile arquivo) {

        return ResponseEntity.status(HttpStatus.CREATED).body(documentoService.criar(dto, arquivo));
    }

    @GetMapping
    public ResponseEntity<List<DocumentoResponseDTO>> listar(
            @RequestParam(name = "clienteId", required = false) Long clienteId,
            @RequestParam(name = "tipo", required = false) TipoDocumento tipo,
            @RequestParam(name = "busca", required = false) String busca) {

        return ResponseEntity.ok(documentoService.listar(clienteId, tipo, busca));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(documentoService.buscarPorId(id));
    }

    // Enviar um arquivo substitui o atual; sem arquivo, o atual e mantido
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestPart("dados") DocumentoRequestDTO dto,
            @RequestPart(name = "arquivo", required = false) MultipartFile arquivo) {

        return ResponseEntity.ok(documentoService.atualizar(id, dto, arquivo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        documentoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // PDF e imagens abrem no navegador; os demais (ou ?download=true) sao baixados
    @GetMapping("/{id}/arquivo")
    public ResponseEntity<Resource> baixarArquivo(
            @PathVariable Long id,
            @RequestParam(name = "download", defaultValue = "false") boolean download) {

        ArquivoDocumento arquivo = documentoService.buscarArquivo(id);
        boolean exibir = !download && arquivo.tipoExibivel() != null;

        ContentDisposition disposicao = (exibir ? ContentDisposition.inline() : ContentDisposition.attachment())
                .filename(arquivo.nomeOriginal(), StandardCharsets.UTF_8)
                .build();
        MediaType tipo = exibir
                ? MediaType.parseMediaType(arquivo.tipoExibivel())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicao.toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(new FileSystemResource(arquivo.caminho()));
    }
}
