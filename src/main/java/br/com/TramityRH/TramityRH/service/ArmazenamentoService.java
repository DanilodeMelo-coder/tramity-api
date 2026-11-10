package br.com.TramityRH.TramityRH.service;

import br.com.TramityRH.TramityRH.model.Colaborador;
import br.com.TramityRH.TramityRH.model.Processo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.Map;
import java.util.UUID;


@Service
public class ArmazenamentoService {

    private static final long TAMANHO_MAXIMO = 5 * 1024 * 1024;

    private static final Map<String, String> TIPOS_ACEITOS = Map.of(
            "application/pdf", "pdf",
            "image/jpeg", "jpg",
            "image/png", "png"
    );

    private final Path raiz;

    public ArmazenamentoService(@Value("${tramity.arquivos.diretorio}") String diretorio) {
        this.raiz = Paths.get(diretorio).toAbsolutePath().normalize();
    }

    public String salvar(MultipartFile arquivo, Processo processo, String nomeTipo) {
        // 1. Validação (RNF13)
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie um arquivo");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O arquivo deve ter até 5 MB");
        }
        String extensao = TIPOS_ACEITOS.get(arquivo.getContentType());
        if (extensao == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato não aceito. Envie PDF, JPG ou PNG");
        }

        // 2. A pasta: arquivos/{id_empresa}/{nome-do-colaborador}_{id_colaborador}/
        Colaborador colaborador = processo.getColaborador();
        String pastaColaborador = simplificar(colaborador.getNome()) + "_" + colaborador.getId();
        Path pasta = raiz.resolve(processo.getEmpresa().getId().toString()).resolve(pastaColaborador);

        // 3. O nome do arquivo, gerado pela API
        String nomeArquivo = simplificar(nomeTipo) + "_" + UUID.randomUUID() + "." + extensao;
        Path destino = pasta.resolve(nomeArquivo);

        // 4. Grava no disco
        try {
            Files.createDirectories(pasta);
            Files.copy(arquivo.getInputStream(), destino);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível salvar o arquivo");
        }

        // 5. Devolve o caminho relativo, que vai para a coluna arquivo_url
        return raiz.relativize(destino).toString();
    }

    public Resource carregar(String caminhoRelativo) {
        Path arquivo = raiz.resolve(caminhoRelativo).normalize();

        if (!arquivo.startsWith(raiz) || !Files.exists(arquivo)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo não encontrado");
        }

        return new FileSystemResource(arquivo);
    }

    private String simplificar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}