# API de Gerenciamento de Jogos

Back-end REST para gerenciar **usuários, jogos, categorias, avaliações e favoritos**, desenvolvido como trabalho da disciplina de Back-End: Web Services (PUCPR).

## Vídeo de demonstração

Em breve: link do vídeo no YouTube.

## Autores

- [William Wosch](https://github.com/williamwosch)
- Matheus Glowaski

## Tecnologias

- Kotlin
- Java 21
- Spring Boot 4.1.2 (Spring Web MVC, Spring Data JPA, Spring Security, Bean Validation)
- Autenticação com JWT (Spring Security OAuth2 Resource Server)
- Banco de dados H2 (modo arquivo)
- SpringDoc OpenAPI (Swagger UI)
- Gradle (Kotlin DSL)

## Como executar

### Pré-requisitos

- JDK 21 instalado
- Não é necessário instalar o Gradle, pois o projeto usa o Gradle Wrapper (`gradlew`)
- IntelliJ IDEA (opcional)

### Passos

1. Clone o repositório:
   ```bash
   git clone https://github.com/williamwosch/games-api.git
   cd games-api
   ```
2. Execute a aplicação:
   - **IntelliJ:** abra o projeto, aguarde o Gradle sincronizar e execute a classe `GamesApplication.kt`.
   - **Terminal (Windows):** `.\gradlew.bat bootRun`
   - **Terminal (Linux/Mac):** `./gradlew bootRun`
3. Acesse a documentação interativa (Swagger):
   http://localhost:8080/swagger-ui.html

O banco de dados é criado automaticamente na pasta `data/` na primeira execução.

### Variáveis de ambiente (opcionais)

| Variável | Função | Padrão |
|---|---|---|
| `ADMIN_PASSWORD` | Senha do administrador padrão | Se não for definida, uma senha aleatória é gerada e exibida no console na primeira execução |
| `ADMIN_EMAIL` | E-mail do administrador padrão | `admin@games.com` |
| `JWT_SECRET` | Chave de assinatura dos tokens (mínimo de 32 caracteres) | Valor de desenvolvimento definido no `application.yaml` |

No IntelliJ: **Run > Edit Configurations > Environment variables**, por exemplo `ADMIN_PASSWORD=sua-senha`.

No PowerShell: `$env:ADMIN_PASSWORD="sua-senha"; .\gradlew.bat bootRun`

## Autenticação (JWT)

A API usa **JWT** (token Bearer). O perfil do usuário (`USER` ou `ADMIN`) vai dentro do token.

### Como autenticar no Swagger

1. Faça login em `POST /auth/login` com e-mail e senha:
   ```json
   { "email": "admin@games.com", "senha": "sua-senha" }
   ```
2. Copie o valor do campo `token` da resposta.
3. Clique no botão **Authorize**, cole o token (sem escrever "Bearer") e confirme.

O token expira em 60 minutos. Depois disso, basta fazer login novamente.

### Perfis e permissões

| Perfil | Permissões |
|---|---|
| Sem login | Apenas `POST /auth/login`, `POST /usuarios` (cadastro) e a documentação Swagger |
| USER | Todos os `GET`, avaliar jogos, favoritar e remover a própria avaliação |
| ADMIN | Tudo o que o USER faz, mais criar, alterar e remover jogos e categorias |

Todo usuário cadastrado em `POST /usuarios` recebe o perfil **USER**. Não é possível se cadastrar como administrador.

### Administrador padrão

É criado automaticamente na primeira execução (se ainda não existir):

- E-mail: `admin@games.com`
- Senha: definida pela variável de ambiente `ADMIN_PASSWORD`. Se ela não existir, a senha gerada aparece no console, na linha `ADMIN_PASSWORD não definida...`.

As senhas são armazenadas com **BCrypt**.

## Acessar o banco de dados (H2 Console)

Com a aplicação rodando, abra http://localhost:8080/h2-console e informe:

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/gamesdb` |
| User Name | `sa` |
| Password | (vazio) |

## Modelo de dados

| Entidade | Descrição |
|---|---|
| `Usuario` | nome, email (único), senha (BCrypt), perfil |
| `Categoria` | nome (único) |
| `Jogo` | título, descrição, ano de lançamento, preço |
| `Avaliacao` | nota (1 a 5), comentário, data |
| `Favorito` | usuário, jogo, data em que favoritou |

### Relacionamentos

- **Jogo e Categoria:** many-to-many (tabela `jogo_categoria`)
- **Jogo e Avaliações:** one-to-many
- **Usuário e Avaliações:** one-to-many
- **Usuário e Jogo (Favorito):** many-to-many com tabela intermediária (a entidade `Favorito` possui atributo próprio: a data)

## Endpoints

A coluna **Acesso** indica quem pode chamar o endpoint.

### Autenticação

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| POST | `/auth/login` | Gera o token JWT | Público |

### Usuários

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| POST | `/usuarios` | Cadastra usuário (perfil USER) | Público |
| GET | `/usuarios/{id}` | Busca por id | Logado |

### Categorias (CRUD completo)

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| POST | `/categorias` | Cria categoria | ADMIN |
| GET | `/categorias` | Lista categorias | Logado |
| GET | `/categorias/{id}` | Busca por id | Logado |
| PUT | `/categorias/{id}` | Atualiza | ADMIN |
| DELETE | `/categorias/{id}` | Remove | ADMIN |

### Jogos (CRUD completo)

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| POST | `/jogos` | Cria jogo | ADMIN |
| GET | `/jogos` | Lista com filtros e ordenação | Logado |
| GET | `/jogos/{id}` | Busca por id | Logado |
| PUT | `/jogos/{id}` | Atualiza | ADMIN |
| DELETE | `/jogos/{id}` | Remove | ADMIN |

### Associações

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| POST | `/jogos/{jogoId}/categorias/{categoriaId}` | Adiciona categoria ao jogo | ADMIN |
| DELETE | `/jogos/{jogoId}/categorias/{categoriaId}` | Remove categoria do jogo | ADMIN |
| POST | `/jogos/{jogoId}/avaliacoes` | Avalia um jogo | Logado |
| GET | `/jogos/{jogoId}/avaliacoes` | Lista avaliações do jogo | Logado |
| DELETE | `/avaliacoes/{id}` | Remove a própria avaliação | Logado |
| POST | `/favoritos/{jogoId}` | Favorita um jogo | Logado |
| GET | `/favoritos` | Lista favoritos do usuário logado | Logado |
| DELETE | `/favoritos/{jogoId}` | Remove dos favoritos | Logado |

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
| 401 | Sem token, token inválido/expirado ou credenciais incorretas no login |
| 403 | Usuário sem permissão (ex.: USER tentando ação de ADMIN, ou remover avaliação de outro usuário) |
| 404 | Recurso não encontrado |
| 409 | Conflito (e-mail duplicado, favorito repetido, registro em uso) |

Exceções próprias: `RecursoNaoEncontradoException`, `ConflitoException`, `AcessoNegadoException` e `CredenciaisInvalidasException`.

## Logs

Os services registram criações, atualizações e remoções (`INFO`), situações de erro e tentativas de login inválidas (`WARN`) e detalhes de consulta (`DEBUG`), usando SLF4J.

## Estrutura do projeto

```
src/main/kotlin/br/pucpr/games
├── config        # Segurança (JWT), OpenAPI e criação do admin padrão
├── controller    # Endpoints REST
├── dto           # Objetos de entrada/saída e validações
├── exception     # Exceções e handler global
├── model         # Entidades JPA
├── repository    # Acesso ao banco (Spring Data)
└── service       # Regras de negócio
```