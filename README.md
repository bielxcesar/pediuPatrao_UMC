# Pediu Patrão — Sistema de Gestão de Pedidos e Auditoria

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.4-brightgreen?style=for-the-badge&logo=springboot)
![MongoDB](https://img.shields.io/badge/MongoDB-Atlas%2FLocal-green?style=for-the-badge&logo=mongodb)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-blue?style=for-the-badge&logo=thymeleaf)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-purple?style=for-the-badge&logo=bootstrap)

O **Pediu Patrão** é uma solução para gestão operacional de pedidos em estabelecimentos alimentícios (pizzarias e restaurantes). O sistema foi projetado com uma arquitetura robusta baseada em **Spring Boot**, **MongoDB** e **Thymeleaf**, focando na aplicação rigorosa de **Controle de Acesso Baseado em Papéis (RBAC)** no backend, **Máquina de Estados para Pedidos**, otimização da fila de atendimento e **Auditoria Imutável e Persistente** para conformidade e rastreabilidade de operações sensíveis.

---

##  Sumário

- [Visão Geral da Arquitetura](#-visão-geral-da-arquitetura)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Mapeamento dos Requisitos Funcionais (RF01 a RF05)](#-mapeamento-dos-requisitos-funcionais-rf01-a-rf05)
- [Variáveis de Ambiente e Segurança](#-variáveis-de-ambiente-e-segurança)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação e Execução](#-instalação-e-execução)
- [Credenciais de Teste](#-credenciais-de-teste)
- [Roteiro de Teste e Demonstração](#-roteiro-de-teste-e-demonstração)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Segurança e Conformidade](#-segurança-e-conformidade)

---

##  Visão Geral da Arquitetura

O projeto adota o padrão em camadas desacopladas (Layered Architecture):

* **Presentation Layer:** Views responsivas com Thymeleaf, Bootstrap 5 e Spring Security Dialect (`xmlns:sec`).
* **Security Layer:** Spring Security com `SecurityFilterChain`, proteção CSRF ativa e isolamento de rotas no backend.
* **Service Layer:** Encapsulamento completo de regras de negócio (`PedidoService`, `ClienteService`, `UsuarioService`, `AuditoriaService`), garantindo que cálculos, transições de status e validações ocorram exclusivamente no servidor.
* **Persistence Layer:** Spring Data MongoDB realizando o mapeamento NoSQL de documentos.

```text
[ Frontend (Thymeleaf + Bootstrap 5) ] 
               │  (CSRF Token + Session Authentication)
               ▼
 [ SecurityFilterChain (RBAC / Backend Route Protection) ]
               │
               ▼
  [ Service Layer (Rules, Calculations & State Machine) ] ──────► [ AuditoriaService ]
               │                                                          │
               ▼                                                          ▼
     [ MongoRepository ]                                         [ MongoDB (auditoria) ]
               │
               ▼
     [ MongoDB (pedidos, clientes, usuarios) ]
```

---

##  Tecnologias Utilizadas

* **Linguagem:** Java 17
* **Framework Principal:** Spring Boot 3.4.4
* **Segurança:** Spring Security 6.x (RBAC, BCrypt, CSRF)
* **Banco de Dados:** MongoDB (Spring Data MongoDB — Local ou MongoDB Atlas)
* **Template Engine:** Thymeleaf + Spring Security Extra Dialect
* **Interface UI:** Bootstrap 5.3 + Bootstrap Icons
* **Build & Dependency Management:** Apache Maven

---

## 📋 Mapeamento dos Requisitos Funcionais (RF01 a RF05)

### RF01 — Controle de Usuários e Permissões
* **Hierarquia de Papéis (`RoleEnum`):** `ADMIN`, `GERENTE` e `ATENDENTE`.
* **Segurança no Backend:** Proteção de endpoints validada via `SecurityConfig`.
  * `/usuarios/**`: Acesso restrito exclusivamente ao perfil `ADMIN`.
  * Operações financeiras (desconto) e cancelamentos: Restritas ao perfil `GERENTE`.
  * Fluxo operacional de pedidos e clientes: Acesso liberado para `ATENDENTE` e `GERENTE`.
* **Prevenção de Elevação de Privilégios:** Apenas administradores conseguem redefinir papéis ou alterar permissões de contas no `UsuarioService`.

### RF02 — Evolução do Cadastro de Pedidos
* **Cálculo Consistente no Servidor:** O cálculo do valor total do pedido ($subtotal = quantidade \times valorUnitario - desconto$) é processado no backend pelo `PedidoService`, ignorando valores totais manipulados pelo cliente.
* **Rastreabilidade Cadastral:** Registro de tamanho/tipo de pizza, sabor, quantidade, valor unitário, valor total, desconto concedido, responsável pelo registro e *timestamps* de entrada e saída.

### RF03 — Controle e Transições de Status dos Pedidos
* **Máquina de Estados de Pedidos:** Sequência estrita de transição:  
`RECEBIDO` ➔ `EM_PREPARACAO` ➔ `PRONTO` ➔ `SAIU_ENTREGA` ➔ `FINALIZADO`* **Validação de Transição:** Tentativas de saltar estados (ex: de `RECEBIDO` para `FINALIZADO`)

 são rejeitadas pelo `PedidoService`.
* **Gestão Dinâmica da Fila de Atendimento:**
  * **Prioridade de Tela:** Pedidos em processamento ficam fixos no topo da lista.
  * **Carrossel de Finalizados (FIFO):** A tela exibe no máximo **6 pedidos finalizados recentes** no rodapé da lista. A entrada do 7º pedido finalizado remove o mais antigo da visualização.
  * **Limpeza de Cancelados:** Pedidos cancelados são removidos da fila operacional da cozinha, mantendo seus dados e justificativas preservados na auditoria do MongoDB.

### RF04 — Cancelamentos e Descontos Exclusivos
* **Concessão de Desconto:** Função restrita ao `GERENTE`, com validação do limite percentual máximo de **20%**, aplicável antes da saída para entrega/finalização.
* **Cancelamento Operacional:** Exclusivo do `GERENTE`, exige justificativa textual obrigatória e só é permitido antes do status `SAIU_ENTREGA`.
* **Auditoria Automática:** Todas as concessões de desconto e cancelamentos disparam logs no serviço de auditoria.

### RF05 — Auditoria Persistente no MongoDB
* **Imutabilidade dos Registros:** A coleção `auditoria` no MongoDB armazena logs de forma permanente. O `AuditoriaController` disponibiliza apenas a rota de consulta (`GET`), sem endpoints de edição (`PUT`) ou exclusão (`DELETE`).
* **Rastreabilidade de Mudanças em Clientes:** O `ClienteService` compara o estado do objeto antes e depois de cada atualização, gravando um evento de auditoria granular para cada campo modificado (nome, telefone, endereço).
* **Campos Auditados:** Usuário autenticado, papel (`ROLE`), data/hora, tipo de operação (`INSERIR`, `ATUALIZAR`, `DESCONTO`, `CANCELAMENTO`), entidade afetada, ID da entidade, valores anteriores, valores novos e justificativa.

---

## 🔒 Variáveis de Ambiente e Segurança

Para evitar o vazamento de credenciais e URIs de bancos de dados no GitHub, o projeto utiliza externalização de configurações via variáveis de ambiente.

### 1. Estrutura do Arquivo `.env` (Raiz do Projeto)
Crie um arquivo `.env` na raiz da pasta `/PediuPatrao` (este arquivo está no `.gitignore` e não é enviado ao versionador):

```env
# String de conexão do MongoDB (Local ou MongoDB Atlas)
MONGODB_URI=mongodb://localhost:27017/pizzaria_db

# Porta de execução do servidor Spring Boot
SERVER_PORT=8080
```

### 2. Arquivo `.env.example`
O repositório inclui o arquivo `.env.example` para servir como modelo sintático para novos ambientes de desenvolvimento.

### 3. Configuração do `application.properties`
O Spring Boot lê as variáveis de ambiente utilizando sintaxe de *fallback*:

```properties
server.port=${SERVER_PORT:8080}
spring.data.mongodb.uri=${MONGODB_URI:mongodb://localhost:27017/pizzaria_db}
spring.thymeleaf.cache=false
spring.mvc.hiddenmethod.filter.enabled=true
```

---

## 🛠️ Pré-requisitos

Para compilar e executar o projeto localmente, certifique-se de ter instalado:

* **Java Development Kit (JDK) 17** ou superior
* **Apache Maven 3.8+** (ou utilizar o wrapper `./mvnw`)
* **MongoDB** rodando localmente (porta `27017`) ou uma conta/cluster configurado no **MongoDB Atlas**.

---

## 🚀 Instalação e Execução

### 1. Clonar o Repositório
```bash
git clone https://github.com/seu-usuario/pediu-patrao.git
cd pediu-patrao
```

### 2. Configurar o Arquivo `.env`
Copie o arquivo de exemplo e insira suas credenciais:
```bash
cp .env.example .env
```

### 3. Compilar e Executar o Projeto
Via linha de comando com Maven:
```bash
mvn clean package
mvn spring-boot:run
```

Ou execute a classe principal diretamente via IDE (IntelliJ IDEA, Eclipse ou VS Code):  
`com.umc.pediupatrao.PizzariaPedidosApplication`

Acesse no navegador: **`http://localhost:8080`**

---

## 🔑 Credenciais de Teste

A aplicação inicializa automaticamente dados e contas de teste com senhas criptografadas via BCrypt ao subir o contexto do Spring:

| Perfil | Username / E-mail | Senha | Permissões Operacionais |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin` (`admin@pediupatrao.com`) | `123456` | Acesso total, gestão de usuários/perfis e auditoria. |
| **GERENTE** | `gerente` (`gerente@pediupatrao.com`) | `123456` | Abertura de pedidos, aplicação de desconto (máx 20%), cancelamento com justificativa e auditoria. |
| **ATENDENTE** | `atendente` (`atendente@pediupatrao.com`) | `123456` | Cadastro/edição de clientes, abertura de pedidos e avanço de status operacionais. |

---

## 🧪 Roteiro de Teste e Demonstração

Siga este roteiro para validar os fluxos e requisitos do sistema:

### Fluxo 1: Abertura e Avanço de Pedido (`ATENDENTE`)
1. Faça login com o usuário `atendente` / `123456`.
2. Acesse o menu **Clientes** e cadastre um novo cliente.
3. Acesse **Pedidos** e clique em **"+ Novo Pedido"**.
4. Selecione o cliente criado, escolha o tamanho, sabor, quantidade (`1`) e valor unitário (`45.00`).
5. Confirme o pedido.  
   * **Validação:** O pedido é gerado no status `RECEBIDO` com o valor calculado pelo servidor (`R$ 45,00`).
6. Na lista de pedidos, clique em **"Ir p/ Cozinha"** $\rightarrow$ Status muda para `EM_PREPARACAO`.
7. Clique em **"Marcar Pronto"** $\rightarrow$ Status muda para `PRONTO`.

### Fluxo 2: Desconto, Cancelamento e Segurança (`GERENTE`)
1. Faça logout e entre como `gerente` / `123456`.
2. Em **Pedidos**, clique em **"+ Novo Pedido"**.
3. Selecione o cliente, informe o valor unitário (`50.00`) e defina o **Desconto (%)** em `10%`.  
   * **Validação:** O servidor valida o subtotal de `R$ 50,00` e o desconto de `R$ 5,00`, registrando o valor total final em `R$ 45,00`.
4. Em um pedido em aberto, preencha o campo **Motivo cancelamento** e confirme o cancelamento.  
   * **Validação:** O pedido é cancelado, a operação é registrada na auditoria com a justificativa, e o item é removido da fila operacional da cozinha.

### Fluxo 3: Validação da Auditoria e Segurança RBAC (`ADMIN`)
1. Acesse o sistema como `admin` / `123456`.
2. Acesse a tela de **Auditoria**.  
   * **Validação:** Todos os eventos executados nos fluxos anteriores estarão listados em tempo real (Abertura, alteração de status, desconto e cancelamento com justificativa).
3. **Teste de Segurança de Rota:** Tente acessar `/usuarios` logado com a conta `atendente`.  
   * **Resultado:** O Spring Security bloqueia o acesso retornando HTTP 403 Forbidden no backend.

---

## 📂 Estrutura do Projeto

```text
src/main/java/com/umc/pediupatrao/
├── config/                  # Configurações do Spring Security (SecurityConfig, BCrypt)
├── controller/              # Controladores Web (PedidoController, UsuarioController, AuditoriaController)
├── entity/                  # Documentos MongoDB (Pedido, Cliente, Usuario, Auditoria)
│   └── enums/               # Enumeradores de domínio (RoleEnum, StatusPedidoEnum)
├── repository/              # Repositórios Spring Data MongoDB
└── service/                 # Camada de Serviços, Regras de Negócio e Auditoria
```

---

## 🛡️ Segurança e Conformidade

* **Proteção CSRF:** Habilitada em todas as requisições de formulários Thymeleaf.
* **Criptografia de Senhas:** Senhas armazenadas no banco utilizando o algoritmo `BCryptPasswordEncoder`.
* **Sanitização de Auditoria:** Credenciais, senhas e tokens nunca são persistidos na coleção de logs de auditoria.
* **Isolamento do Git:** O arquivo `.gitignore` previne a inclusão acidental de arquivos `.env`, configurações da IDE (`.idea/`) e artefatos compilados (`target/`).
