-- =====================================================================
-- Tramity — dados de teste
-- Rodar depois do schema.sql. Apaga e recria os dados de teste.
-- Usuário do RH fica para a fase 2 (a senha precisa estar em hash BCrypt).
-- =====================================================================

DELETE FROM tipo_documento;
DELETE FROM empresa;

-- ---------------------------------------------------------------------
-- Empresa fictícia de teste
-- ---------------------------------------------------------------------
INSERT INTO empresa (id, cnpj, razao_social, email) VALUES
('11111111-1111-1111-1111-111111111111', '00000000000191', 'Empresa Exemplo Ltda.', 'rh@empresaexemplo.com.br');

-- ---------------------------------------------------------------------
-- Catálogo de documentos (lista definida pelo grupo)
-- ---------------------------------------------------------------------
INSERT INTO tipo_documento (id, nome, categoria) VALUES
-- 1. Documentos pessoais
(UUID(), 'RG ou CNH',                                          'Documentos pessoais'),
(UUID(), 'CPF',                                                'Documentos pessoais'),
(UUID(), 'CTPS (Carteira de Trabalho)',                        'Documentos pessoais'),
(UUID(), 'Certidão de nascimento ou casamento',                'Documentos pessoais'),
(UUID(), 'Comprovante de residência',                          'Documentos pessoais'),
(UUID(), 'Foto 3x4',                                           'Documentos pessoais'),
-- 2. Documentos do trabalhador e obrigações civis
(UUID(), 'PIS/PASEP ou Cartão Cidadão',                        'Trabalhador e obrigações civis'),
(UUID(), 'Título de eleitor e quitação eleitoral',             'Trabalhador e obrigações civis'),
(UUID(), 'Certificado de reservista',                          'Trabalhador e obrigações civis'),
-- 3. Documentação acadêmica e profissional
(UUID(), 'Comprovante de escolaridade',                        'Acadêmica e profissional'),
(UUID(), 'Registro na entidade de classe',                     'Acadêmica e profissional'),
(UUID(), 'Cursos técnicos ou certificações',                   'Acadêmica e profissional'),
-- 4. Dados financeiros
(UUID(), 'Comprovante de conta bancária',                      'Dados financeiros'),
-- 5. Dependentes
(UUID(), 'CPF do dependente',                                  'Dependentes'),
(UUID(), 'Certidão de nascimento do dependente',               'Dependentes'),
(UUID(), 'Caderneta de vacinação',                             'Dependentes'),
(UUID(), 'Comprovante de frequência escolar',                  'Dependentes'),
(UUID(), 'Laudo médico do dependente',                         'Dependentes'),
-- 6. Documentos da empresa no processo
(UUID(), 'ASO (Atestado de Saúde Ocupacional)',                'Documentos da empresa'),
(UUID(), 'Ficha de cadastramento admissional',                 'Documentos da empresa'),
(UUID(), 'Declaração de opção de vale-transporte',             'Documentos da empresa'),
(UUID(), 'Declaração de dependentes para imposto de renda',    'Documentos da empresa');
