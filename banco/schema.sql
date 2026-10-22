-- =====================================================================
-- Tramity — criação das tabelas (MySQL 8.4)
-- Baseado no modelo de dados definido pelo grupo, com os ajustes
-- combinados: senha_hash, sem empresacol, ids UUID, ativo BOOLEAN,
-- tabela recuperacao_senha e campos únicos.
-- Rodar com o banco `tramity` selecionado.
-- =====================================================================

-- Apaga na ordem inversa das dependências, para poder rodar de novo
DROP TABLE IF EXISTS evento;
DROP TABLE IF EXISTS envio;
DROP TABLE IF EXISTS documento_processo;
DROP TABLE IF EXISTS convite;
DROP TABLE IF EXISTS processo;
DROP TABLE IF EXISTS tipo_documento;
DROP TABLE IF EXISTS recuperacao_senha;
DROP TABLE IF EXISTS colaborador;
DROP TABLE IF EXISTS usuario_rh_empresa;
DROP TABLE IF EXISTS empresa;

-- ---------------------------------------------------------------------
-- Empresas clientes do Tramity
-- ---------------------------------------------------------------------
CREATE TABLE empresa (
    id              CHAR(36)     NOT NULL,
    cnpj            CHAR(14)     NOT NULL,
    razao_social    VARCHAR(150) NOT NULL,
    email           VARCHAR(150) NOT NULL,
    data_cadastro   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo           BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_empresa_cnpj (cnpj)
);

-- ---------------------------------------------------------------------
-- Usuários do RH (cada um pertence a uma empresa)
-- ---------------------------------------------------------------------
CREATE TABLE usuario_rh_empresa (
    id              CHAR(36)     NOT NULL,
    id_empresa      CHAR(36)     NOT NULL,
    nome            VARCHAR(150) NOT NULL,
    email           VARCHAR(150) NOT NULL,
    senha_hash      VARCHAR(100) NOT NULL,
    data_criacao    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo           BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_rh_email (email),
    CONSTRAINT fk_rh_empresa FOREIGN KEY (id_empresa) REFERENCES empresa (id)
);

-- ---------------------------------------------------------------------
-- Colaboradores (não pertencem a uma empresa: a ligação é pelo processo)
-- ---------------------------------------------------------------------
CREATE TABLE colaborador (
    id              CHAR(36)     NOT NULL,
    nome            VARCHAR(150) NOT NULL,
    email           VARCHAR(150) NOT NULL,
    cpf             CHAR(11)     NOT NULL,
    senha_hash      VARCHAR(100) NOT NULL,
    data_criacao    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo           BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_colaborador_email (email),
    UNIQUE KEY uk_colaborador_cpf (cpf)
);

-- ---------------------------------------------------------------------
-- Códigos de recuperação de senha (RF02, RN18)
-- ---------------------------------------------------------------------
CREATE TABLE recuperacao_senha (
    id              CHAR(36)     NOT NULL,
    email           VARCHAR(150) NOT NULL,
    codigo          CHAR(6)      NOT NULL,
    data_expiracao  DATETIME     NOT NULL,
    usado_em        DATETIME     NULL,
    data_criacao    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_recuperacao_email (email)
);

-- ---------------------------------------------------------------------
-- Catálogo de tipos de documento (global, sem empresa)
-- ---------------------------------------------------------------------
CREATE TABLE tipo_documento (
    id              CHAR(36)     NOT NULL,
    nome            VARCHAR(150) NOT NULL,
    categoria       VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tipo_documento_nome (nome)
);

-- ---------------------------------------------------------------------
-- Processos de admissão ou demissão
-- data_conclusao vazia = processo em andamento
-- ---------------------------------------------------------------------
CREATE TABLE processo (
    id              CHAR(36)     NOT NULL,
    id_empresa      CHAR(36)     NOT NULL,
    criado_por      CHAR(36)     NOT NULL,
    id_colaborador  CHAR(36)     NULL,
    tipo            VARCHAR(20)  NOT NULL,
    data_criacao    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_conclusao  DATETIME     NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_processo_tipo CHECK (tipo IN ('ADMISSAO', 'DEMISSAO')),
    CONSTRAINT fk_processo_empresa     FOREIGN KEY (id_empresa)     REFERENCES empresa (id),
    CONSTRAINT fk_processo_criado_por  FOREIGN KEY (criado_por)     REFERENCES usuario_rh_empresa (id),
    CONSTRAINT fk_processo_colaborador FOREIGN KEY (id_colaborador) REFERENCES colaborador (id)
);

-- ---------------------------------------------------------------------
-- Convites (código que liga o colaborador ao processo — RN01 a RN04)
-- ---------------------------------------------------------------------
CREATE TABLE convite (
    id              CHAR(36)     NOT NULL,
    id_processo     CHAR(36)     NOT NULL,
    email           VARCHAR(150) NOT NULL,
    codigo          CHAR(6)      NOT NULL,
    data_expiracao  DATETIME     NOT NULL,
    usado_em        DATETIME     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_convite_codigo (codigo),
    CONSTRAINT fk_convite_processo FOREIGN KEY (id_processo) REFERENCES processo (id)
);

-- ---------------------------------------------------------------------
-- Documentos pedidos em cada processo
-- ---------------------------------------------------------------------
CREATE TABLE documento_processo (
    id                  CHAR(36)     NOT NULL,
    id_processo         CHAR(36)     NOT NULL,
    id_tipo_documento   CHAR(36)     NOT NULL,
    descricao           VARCHAR(255) NULL,
    direcao             VARCHAR(20)  NOT NULL DEFAULT 'COLABORADOR_ENVIA',
    status              VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE',
    aceito_em           DATETIME     NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_documento_direcao CHECK (direcao IN ('COLABORADOR_ENVIA', 'EMPRESA_ENVIA')),
    CONSTRAINT ck_documento_status  CHECK (status  IN ('PENDENTE', 'EM_ANALISE', 'APROVADO', 'RECUSADO', 'ACEITO')),
    CONSTRAINT fk_documento_processo FOREIGN KEY (id_processo)       REFERENCES processo (id),
    CONSTRAINT fk_documento_tipo     FOREIGN KEY (id_tipo_documento) REFERENCES tipo_documento (id)
);

-- ---------------------------------------------------------------------
-- Envios: cada tentativa fica guardada (RN13)
-- ---------------------------------------------------------------------
CREATE TABLE envio (
    id                      CHAR(36)     NOT NULL,
    id_documento_processo   CHAR(36)     NOT NULL,
    arquivo_url             VARCHAR(500) NOT NULL,
    enviado_em              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resultado               VARCHAR(20)  NULL,
    motivo_recusa           VARCHAR(500) NULL,
    avaliado_por            CHAR(36)     NULL,
    data_avaliacao          DATETIME     NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_envio_resultado CHECK (resultado IS NULL OR resultado IN ('APROVADO', 'RECUSADO')),
    CONSTRAINT ck_envio_motivo    CHECK (resultado IS NULL OR resultado <> 'RECUSADO' OR motivo_recusa IS NOT NULL),
    CONSTRAINT fk_envio_documento  FOREIGN KEY (id_documento_processo) REFERENCES documento_processo (id),
    CONSTRAINT fk_envio_avaliador  FOREIGN KEY (avaliado_por)          REFERENCES usuario_rh_empresa (id)
);

-- ---------------------------------------------------------------------
-- Histórico para auditoria (RNF08)
-- ---------------------------------------------------------------------
CREATE TABLE evento (
    id              CHAR(36)     NOT NULL,
    id_processo     CHAR(36)     NOT NULL,
    autor           VARCHAR(150) NOT NULL,
    acao            VARCHAR(255) NOT NULL,
    data_criacao    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_evento_processo FOREIGN KEY (id_processo) REFERENCES processo (id)
);
