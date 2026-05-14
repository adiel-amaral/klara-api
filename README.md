# 💚 Klara — clareza nas finanças

> Chega de susto com conta esquecida. O Klara te ajuda a declarar, acompanhar e quitar suas contas com simplicidade.

![Status](https://img.shields.io/badge/status-em%20desenvolvimento-green)
![Plataforma](https://img.shields.io/badge/plataforma-web-blue)

---

## 📌 Sobre o projeto

O **Klara** é uma aplicação de controle financeiro pessoal e familiar, focada em quem quer organizar as contas do mês sem complicação.

A proposta é simples: você cadastra suas contas a pagar, o Klara te lembra antes de vencer e exporta uma planilha com tudo organizado.

---

## ✨ Funcionalidades planejadas

- 📋 **Cadastro de contas a pagar** — nome, valor, vencimento, categoria e recorrência
- 🔔 **Lembretes automáticos** — notificações antes do vencimento configuráveis
- 📊 **Dashboard financeiro** — visão mensal de entradas, saídas e saldo
- 📁 **Exportação de planilhas** — gera `.xlsx` filtrado por período ou categoria
- 🔁 **Contas recorrentes** — geração automática de contas mensais/anuais
- 🏷️ **Categorias personalizáveis** — organize do seu jeito

---

## 🏛️ Arquitetura

A API segue o padrão **Layered Architecture** (Arquitetura em Camadas), organizada pelo estilo **Package by Layer**. Cada requisição percorre um caminho bem definido de responsabilidades:

```
Requisição HTTP
      │
      ▼
 Controller        valida entrada (@Valid) e delega
      │
      ▼
  Service          aplica regras de negócio; converte DTO ↔ Entity
      │
      ▼
 Repository        abstrai o banco via Spring Data JPA
      │
      ▼
  Database         PostgreSQL (Neon)
```

O `GlobalExceptionHandler` intercepta qualquer erro em qualquer camada antes de retornar ao cliente.

### Camadas e responsabilidades

| Camada | Pacote | Responsabilidade |
| --- | --- | --- |
| **Controller** | `controller/` | Recebe a requisição HTTP, aplica `@Valid` e devolve a resposta formatada |
| **Service** | `service/` | Regras de negócio; única camada que conhece tanto DTOs quanto Entities |
| **Repository** | `repository/` | Acesso a dados via `JpaRepository`; sem lógica de negócio |
| **Entity** | `entity/` | Mapeamento da tabela; gerencia ciclo de vida com `@PrePersist` / `@PreUpdate` |
| **DTO** | `dto/` | Desacopla o contrato da API do modelo de domínio; implementados como `record` |
| **Enums** | `enums/` | Tipos de domínio com segurança em tempo de compilação (`BillStatus`, `Recurrence`) |
| **Exception** | `exception/` | `@RestControllerAdvice` centraliza erros; separa validação (422) de regra de negócio (400) |

### Decisões de design

- **`record` para DTOs** — `BillRequestDTO` e `BillResponseDTO` são `record`s Java, garantindo imutabilidade e eliminando boilerplate de getters/construtores.
- **Factory method no DTO** — `BillResponseDTO.from(Bill)` centraliza a conversão Entity → DTO, mantendo essa lógica em um único lugar.
- **Ciclo de vida na Entity** — `@PrePersist` define o status inicial como `PENDING` e registra os timestamps; `@PreUpdate` mantém `updatedAt` sincronizado automaticamente.
- **Exceções por semântica** — `BusinessException` resulta em HTTP 400 (regra de negócio violada); `MethodArgumentNotValidException` resulta em HTTP 400 (entrada inválida).

---

## 🗂️ Estrutura do projeto

```
src/main/java/com/klaraapi/
├── controller/     Recebe as requisições HTTP e delega para o service
├── service/        Contém as regras de negócio
├── repository/     Acesso ao banco de dados via Spring Data JPA
├── entity/         Entidades JPA mapeadas para as tabelas do banco
├── dto/            Objetos de entrada (Request) e saída (Response) da API
├── enums/          Enumerações de domínio (status, recorrência)
└── exception/      Tratamento centralizado de erros
```

### Stack

| Tecnologia | Versão | Uso |
| --- | --- | --- |
| Java | 21 | Linguagem principal |
| Spring Boot | 4.0.6 | Framework base |
| PostgreSQL | — | Banco de dados |

---

## 📱 Integração WhatsApp (WAHA)

Ao cadastrar um usuário via `POST /klara/users`, uma mensagem de boas-vindas é enviada automaticamente para o telefone do usuário via integração com o [WAHA](https://waha.devlike.pro/) (WhatsApp HTTP API).

A mensagem é enviada de forma **assíncrona** com **retry** (3 tentativas com backoff exponencial).

### Como usar o WAHA localmente

#### 1. Subir o container

```bash
docker-compose up -d
```

Isso sobe o container `waha-novo` na porta `3000`.

#### 2. Acessar o Dashboard

Abra no navegador: [http://localhost:3000](http://localhost:3000)

#### 3. Criar uma sessão

1. Clique em **"Start new session"**
2. Nomeie a sessão (ex.: `default`)
3. Clique em **"Create"**

#### 4. Escanear o QR Code

1. Clique no nome da sessão criada
2. Um **QR Code** será exibido na tela
3. Abra o **WhatsApp** no celular → **Configurações** → **Aparelhos conectados** → **Conectar um aparelho**
4. Escaneie o QR Code

Após escanear, o status mudará para **"CONNECTED"**.

> ⚠️ Mantenha o celular conectado à internet para que as mensagens sejam enviadas.

### Testando o cadastro

**Endpoint:**
```
POST http://localhost:8080/klara/users
```

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "name": "Maria Silva",
  "email": "maria.silva@email.com",
  "birthDate": "1995-08-15",
  "gender": "FEMALE",
  "socialName": "Maria",
  "phone": "+5548999999999"
}
```

**Resposta (201 Created):**
```json
{
  "id": 1,
  "name": "Maria Silva",
  "email": "maria.silva@email.com",
  "birthDate": "1995-08-15",
  "gender": "FEMALE",
  "socialName": "Maria",
  "phone": "+5548999999999",
  "active": true,
  "createdAt": "2026-05-11T10:30:00"
}
```

Após o cadastro, o usuário recebe automaticamente uma mensagem de boas-vindas no WhatsApp.

### Fluxo da integração

```
POST /klara/users
      │
      ▼
 UserAccountService.create()  valida unicidade (email, telefone) e salva no banco
      │
      ▼
 WahaService              envia mensagem assíncrona via WAHA
      │
      ▼
 WAHA /api/sendText       entrega a mensagem no WhatsApp do usuário
```

---

<p align="center">Feito com 💚 para quem quer <strong>clareza nas finanças</strong></p>