dia 10/10 realizando passo 1 de 6

Passo 1: Reestruturação de Domínio (Entidades e Enums)
Crie os enumeradores para padronizar as regras de negócio antes de alterar as entidades.

Criar RoleEnum com os valores ADMIN, GERENTE, ATENDENTE.

Criar StatusPedidoEnum com os valores RECEBIDO, EM_PREPARACAO, PRONTO, SAIU_ENTREGA, FINALIZADO, CANCELADO.

Atualizar a entidade Usuario para utilizar o RoleEnum.

Atualizar a entidade Pedido adicionando: lista de itens, status, valor total, desconto, responsável pelo registro, data/hora de entrada e de saída.

Criar a entidade Auditoria contendo: id, usuario, role, dataHora, operacao, entidadeAfetada, idEntidade, campoModificado, valorAnterior, valorNovo, justificativa.

(concluido)


---

Passo 2: Configuração de Segurança e Autorização (RF01)
A proteção deve ocorrer no backend para evitar manipulações de requisições.

Modificar SecurityConfig para utilizar SecurityFilterChain.

Restringir endpoints no SecurityConfig (ex: /usuarios/** apenas para ADMIN).

Restringir endpoints de desconto e cancelamento de pedidos apenas para GERENTE.

Habilitar proteção CSRF padrão do Spring Security e garantir que os formulários Thymeleaf enviem o token.

No UsuarioService, validar que apenas perfis ADMIN podem alterar a role de outros usuários.

---

Passo 3: Serviço de Auditoria (RF05)
Implemente a auditoria primeiro para injetá-la nos demais serviços posteriormente.

Criar AuditoriaRepository.

Criar AuditoriaService com um método principal para registrar eventos.

Capturar o usuário autenticado via SecurityContextHolder dentro do serviço de auditoria.

Garantir que não haja endpoints de exclusão ou alteração (PUT/DELETE) no AuditoriaController, apenas GET para ADMIN e GERENTE.

---

Passo 4: Evolução e Fluxo de Pedidos (RF02, RF03 e RF04)
Toda a lógica de transição e cálculo deve residir no PedidoService.

Implementar o cálculo do valor total no momento de salvar o pedido, somando os itens e subtraindo o desconto, ignorando valores enviados pelo front-end.

Criar validação de transição de status utilizando um switch-case ou estrutura similar para impedir saltos inválidos (ex: RECEBIDO não pode ir direto para FINALIZADO).

Criar método cancelarPedido(Long id, String justificativa) restrito ao GERENTE. Validar se o status atual permite cancelamento (não pode ter saído para entrega) e registrar na auditoria.

Criar método aplicarDesconto(Long id, BigDecimal percentual) restrito ao GERENTE. Validar limite de 20%, status do pedido, recalcular o total e registrar na auditoria.

---

Passo 5: Auditoria de Clientes (RF05)
Integrar o serviço de auditoria nas operações de cliente.

No ClienteService, ao executar o método de atualização, buscar o cliente atual no banco antes de salvar.

Comparar os campos alterados (telefone, logradouro, etc.) entre o objeto antigo e o novo.

Para cada campo modificado, disparar o registro no AuditoriaService.

---

Passo 6: Ajustes de Interface (Thymeleaf)
Refletir as regras do backend na camada de visualização.

Adicionar o namespace do Spring Security no Thymeleaf (xmlns:sec="[http://www.thymeleaf.org/extras/spring-security](http://www.thymeleaf.org/extras/spring-security)").

Utilizar sec:authorize="hasAuthority('GERENTE')" para exibir os botões de desconto e cancelamento.

Desabilitar campos de formulário e ocultar botões de transição de status que não sejam compatíveis com o estado atual do pedido.
