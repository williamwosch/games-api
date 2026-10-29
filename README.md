# 🎮 API de Gerenciamento de Jogos

Back-end REST para gerenciar **usuários, jogos, categorias, avaliações e favoritos**, desenvolvido como trabalho da disciplina de back-end (PUCPR).

## Tecnologias

- Kotlin
- Java 21
- Spring Boot 4.1.2 (Spring Web MVC, Spring Data JPA, Spring Security, Bean Validation)
- Banco de dados H2 (modo arquivo)
- SpringDoc OpenAPI (Swagger UI)
- Gradle (Kotlin DSL)

## Como executar

### Pré-requisitos
- JDK 21 instalado
- IntelliJ IDEA (ou outro terminal com Gradle Wrapper)

### Passos
1. Clone o repositório:
   ```bash
   git clone https://github.com/williamwosch/games-api
   cd games-api
   ```
2. Execute a aplicação:
   - **IntelliJ:** abra o projeto, aguarde o Gradle sincronizar e execute a classe `GamesApplication.kt`.
   - **Terminal (Windows):** `.\gradlew.bat bootRun`
   - **Terminal (Linux/Mac):** `./gradlew bootRun`
3. Acesse a documentação interativa (Swagger):
   **http://localhost:8080/swagger-ui.html**

O banco de dados é criado automaticamente na pasta `data/` na primeira execução.

## Autenticação

A API usa **HTTP Basic**. O login é o **e-mail** e a **senha** do usuário cadastrado.

| Acesso | Endpoints |
|---|---|
| Público | `GET /jogos/**`, `GET /categorias/**`, `POST /usuarios`, Swagger |
| Autenticado | Todo o restante (criar, editar, remover, avaliar, favoritar) |

No Swagger, cadastre um usuário em `POST /usuarios` e clique em **Authorize** para informar e-mail e senha.

As senhas são armazenadas com **BCrypt**.

## Modelo de dados

| Entidade | Descrição |
|---|---|
| `Usuario` | nome, email (único), senha |
| `Categoria` | nome (único) |
| `Jogo` | título, descrição, ano de lançamento, preço |
| `Avaliacao` | nota (1 a 5), comentário, data |
| `Favorito` | usuário, jogo, data em que favoritou |

### Relacionamentos

- **Jogo ↔ Categoria:** many-to-many (tabela `jogo_categoria`)
- **Jogo → Avaliações:** one-to-many
- **Usuário → Avaliações:** one-to-many
- **Usuário ↔ Jogo (Favorito):** many-to-many com tabela intermediária (`Favorito` possui atributo próprio: data)

## Endpoints

### Categorias (CRUD completo)
| Método | Rota | Descrição | Auth |
|---|---|---|---|
| POST | `/categorias` | Cria categoria | Sim |
| GET | `/categorias` | Lista categorias | Não |
| GET | `/categorias/{id}` | Busca por id | Não |
| PUT | `/categorias/{id}` | Atualiza | Sim |
| DELETE | `/categorias/{id}` | Remove | Sim |

### Jogos (CRUD completo)
| Método | Rota | Descrição | Auth |
|---|---|---|---|
| POST | `/jogos` | Cria jogo | Sim |
| GET | `/jogos` | Lista com filtros e ordenação | Não |
| GET | `/jogos/{id}` | Busca por id | Não |
| PUT | `/jogos/{id}` | Atualiza | Sim |
| DELETE | `/jogos/{id}` | Remove | Sim |

### Associações
| Método | Rota | Descrição | Auth |
|---|---|---|---|
| POST | `/jogos/{jogoId}/categorias/{categoriaId}` | Adiciona categoria ao jogo | Sim |
| DELETE | `/jogos/{jogoId}/categorias/{categoriaId}` | Remove categoria do jogo | Sim |
| POST | `/jogos/{jogoId}/avaliacoes` | Avalia um jogo | Sim |
| GET | `/jogos/{jogoId}/avaliacoes` | Lista avaliações do jogo | Sim |
| DELETE | `/avaliacoes/{id}` | Remove a própria avaliação | Sim |
| POST | `/favoritos/{jogoId}` | Favorita um jogo | Sim |
| GET | `/favoritos` | Lista favoritos do usuário logado | Sim |
| DELETE | `/favoritos/{jogoId}` | Remove dos favoritos | Sim |

### Usuários
| Método | Rota | Descrição | Auth |
|---|---|---|---|
| POST | `/usuarios` | Cadastra usuário | Não |
| GET | `/usuarios/{id}` | Busca por id | Sim |

### Consulta com filtro e ordenação

`GET /jogos` aceita os seguintes parâmetros de query (todos opcionais):

| Parâmetro | Descrição |
|---|---|
| `titulo` | Contém o texto (sem diferenciar maiúsculas) |
| `categoria` | Nome exato da categoria |
| `anoMin` / `anoMax` | Intervalo do ano de lançamento |
| `precoMax` | Preço máximo |
| `ordenarPor` | `id`, `titulo`, `anoLancamento` ou `preco` (padrão: `id`) |
| `direcao` | `asc` ou `desc` (padrão: `asc`) |

Exemplo:
```
GET /jogos?categoria=RPG&anoMin=2015&precoMax=200&ordenarPor=preco&direcao=desc
```

## Tratamento de erros

Os erros são devolvidos em JSON padronizado por um `@RestControllerAdvice`:

| Status | Situação |
|---|---|
| 400 | Dados inválidos (inclui os campos com erro) |
| 403 | Tentar remover avaliação de outro usuário |
| 404 | Recurso não encontrado |
| 409 | Conflito (e-mail duplicado, favorito repetido, registro em uso) |

Exceções próprias: `RecursoNaoEncontradoException`, `ConflitoException`, `AcessoNegadoException`.

## Logs

Os services registram criações, atualizações e remoções (`INFO`), além de situações de erro (`WARN`) e detalhes de consulta (`DEBUG`), usando SLF4J.

## Estrutura do projeto

```
src/main/kotlin/br/pucpr/games
├── config        # Segurança e OpenAPI
├── controller    # Endpoints REST
├── dto           # Objetos de entrada/saída e validações
├── exception     # Exceções e handler global
├── model         # Entidades JPA
├── repository    # Acesso ao banco (Spring Data)
└── service       # Regras de negócio
```

## Autor

william wosch e matheus glowaski, disciplina de Back-end, PUCPR.
