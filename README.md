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

## 🗂️ Estrutura do projeto

A API segue a arquitetura **Package by Layer**, onde o código é organizado por camada técnica. Cada pacote agrupa classes com a mesma responsabilidade, independente do domínio de negócio.

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

<p align="center">Feito com 💚 para quem quer <strong>clareza nas finanças</strong></p>