## FASE 1: ARQUITETURA BASE E SEGURANÇA
**Passo 1:** Reestruturação do Domínio (Entidades e Enums)

Status: CONCLUÍDO

- Enumeração de perfis via RoleEnum (ADMIN, GERENTE, ATENDENTE).

- Ciclo de vida de pedidos em StatusPedidoEnum (RECEBIDO, EM_PREPARACAO, PRONTO, SAIU_ENTREGA, FINALIZADO, CANCELADO).

- Atualização do modelo Usuario para utilizar o RoleEnum.

- Expansão da entidade Pedido com itens, totais, descontos, responsável e timestamps.

- Criação da entidade Auditoria para persistência de alterações no MongoDB.


**Passo 2:** Configuração de Segurança e Autorização (RF01)
Status: CONCLUÍDO

- Migração para SecurityFilterChain no SecurityConfig.

- Bloqueio de rotas administrativas /usuarios/** para perfil ADMIN.

- Restrição de cancelamento e desconto exclusivamente ao perfil GERENTE.

- Validação de elevação de privilégios e proteção CSRF ativa.

**Passo 3:** Serviço de Auditoria e Persistência de Logs (RF05)
Status: CONCLUÍDO

- Criação do AuditoriaRepository e AuditoriaService.

- Captura automática de usuário autenticado e perfil via SecurityContextHolder.

- Imutabilidade dos registros com exposição apenas via GET no AuditoriaController para ADMIN e GERENTE.

---

## FASE 2: REGRAS DE NEGÓCIO E AUDITORIA AVANÇADA
**Passo 4:** Evolução e Fluxo de Pedidos (RF02, RF03 e RF04)
Status: CONCLUÍDO

- Processamento e cálculo de totais realizado no backend.

- Controle de transição de status e bloqueios de saltos inválidos.

- Operações de cancelamento e desconto restritas ao GERENTE (limite de 20% no desconto) com registro em auditoria.

**Passo 5:** Auditoria de Clientes (RF05)
Status: CONCLUÍDO

- Integração do AuditoriaService ao ClienteService.

- Comparação de estado anterior e atual no banco de dados.

- Geração de logs granulares para alterações de nome, telefone e endereço.

---

## FASE 3: INTERFACE DE USUÁRIO E FLUXOS COMPLEMENTARES
**Passo 6:** Ajustes de Interface e Permissões Visuais
Status: CONCLUÍDO

- Integração de namespace de segurança no Thymeleaf.

- Dropdown de perfis ajustado para exibir ADMIN, GERENTE e ATENDENTE.

- Restrição e bloqueio visual de ações de acordo com o perfil logado.

**Passo 7:** Organização de Rotas e Fluxo de Recuperação de Senha
Status: CONCLUÍDO

- Mapeamento de rotas e feedback de autenticação no login.

- Fluxo completo de "Esqueceu a senha?" (solicitação por e-mail, geração de código de 6 dígitos no console/log, validação, confirmação de nova senha e bloqueio de 5 minutos após 5 erros).

- Recriação automática de contas de teste via PizzariaPedidosApplication com e-mails vinculados.