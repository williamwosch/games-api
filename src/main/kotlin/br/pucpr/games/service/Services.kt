package br.pucpr.games.service

import br.pucpr.games.dto.*
import br.pucpr.games.exception.*
import br.pucpr.games.model.*
import br.pucpr.games.repository.*
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional
class CategoriaService(private val repo: CategoriaRepository) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun criar(req: CategoriaRequest): CategoriaResponse {
        if (repo.existsByNomeIgnoreCase(req.nome.trim())) {
            throw ConflitoException("Categoria '${req.nome}' já existe")
        }
        val salva = repo.save(Categoria(req.nome.trim()))
        log.info("Categoria criada: id={}, nome={}", salva.id, salva.nome)
        return salva.toResponse()
    }

    @Transactional(readOnly = true)
    fun listar(): List<CategoriaResponse> = repo.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun buscar(id: Long): CategoriaResponse = buscarEntidade(id).toResponse()

    fun atualizar(id: Long, req: CategoriaRequest): CategoriaResponse {
        val categoria = buscarEntidade(id)
        categoria.nome = req.nome.trim()
        repo.flush()
        log.info("Categoria atualizada: id={}", id)
        return categoria.toResponse()
    }

    fun deletar(id: Long) {
        val categoria = buscarEntidade(id)
        repo.delete(categoria)
        repo.flush() // força o erro aqui se a categoria estiver em uso
        log.info("Categoria removida: id={}", id)
    }

    private fun buscarEntidade(id: Long) =
        repo.findByIdOrNull(id) ?: throw RecursoNaoEncontradoException("Categoria $id não encontrada")
}

@Service
@Transactional
class JogoService(
    private val jogoRepo: JogoRepository,
    private val categoriaRepo: CategoriaRepository,
    private val avaliacaoRepo: AvaliacaoRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val camposOrdenaveis = setOf("id", "titulo", "anoLancamento", "preco")

    fun criar(req: JogoRequest): JogoResponse {
        val jogo = Jogo(req.titulo.trim(), req.descricao, req.anoLancamento, req.preco)
        jogo.categorias = carregarCategorias(req.categoriaIds)
        val salvo = jogoRepo.save(jogo)
        log.info("Jogo criado: id={}, titulo={}", salvo.id, salvo.titulo)
        return resposta(salvo)
    }

    @Transactional(readOnly = true)
    fun listar(
        titulo: String?, categoria: String?, anoMin: Int?, anoMax: Int?,
        precoMax: BigDecimal?, ordenarPor: String, direcao: String
    ): List<JogoResponse> {
        val campo = if (ordenarPor in camposOrdenaveis) ordenarPor else {
            log.warn("Campo de ordenação inválido '{}', usando 'id'", ordenarPor)
            "id"
        }
        val sentido = if (direcao.equals("desc", ignoreCase = true)) Sort.Direction.DESC else Sort.Direction.ASC
        val spec = JogoSpecs.filtrar(titulo, categoria, anoMin, anoMax, precoMax)
        log.debug("Listando jogos ordenando por {} {}", campo, sentido)
        return jogoRepo.findAll(spec, Sort.by(sentido, campo)).map { resposta(it) }
    }

    @Transactional(readOnly = true)
    fun buscar(id: Long): JogoResponse = resposta(buscarEntidade(id))

    fun atualizar(id: Long, req: JogoRequest): JogoResponse {
        val jogo = buscarEntidade(id)
        jogo.titulo = req.titulo.trim()
        jogo.descricao = req.descricao
        jogo.anoLancamento = req.anoLancamento
        jogo.preco = req.preco
        jogo.categorias = carregarCategorias(req.categoriaIds)
        log.info("Jogo atualizado: id={}", id)
        return resposta(jogo)
    }

    fun deletar(id: Long) {
        val jogo = buscarEntidade(id)
        jogoRepo.delete(jogo)
        log.info("Jogo removido: id={}", id)
    }

    // ----- associação Jogo <-> Categoria -----
    fun adicionarCategoria(jogoId: Long, categoriaId: Long): JogoResponse {
        val jogo = buscarEntidade(jogoId)
        val categoria = categoriaRepo.findByIdOrNull(categoriaId)
            ?: throw RecursoNaoEncontradoException("Categoria $categoriaId não encontrada")
        jogo.categorias.add(categoria)
        log.info("Categoria {} adicionada ao jogo {}", categoriaId, jogoId)
        return resposta(jogo)
    }

    fun removerCategoria(jogoId: Long, categoriaId: Long): JogoResponse {
        val jogo = buscarEntidade(jogoId)
        val removida = jogo.categorias.removeIf { it.id == categoriaId }
        if (!removida) throw RecursoNaoEncontradoException("O jogo $jogoId não possui a categoria $categoriaId")
        log.info("Categoria {} removida do jogo {}", categoriaId, jogoId)
        return resposta(jogo)
    }

    private fun carregarCategorias(ids: Set<Long>): MutableSet<Categoria> {
        val categorias = categoriaRepo.findAllById(ids)
        if (categorias.size != ids.size) throw RecursoNaoEncontradoException("Uma ou mais categorias não existem")
        return categorias.toMutableSet()
    }

    private fun resposta(jogo: Jogo) = jogo.toResponse(avaliacaoRepo.mediaPorJogo(jogo.id!!))

    private fun buscarEntidade(id: Long) =
        jogoRepo.findByIdOrNull(id) ?: throw RecursoNaoEncontradoException("Jogo $id não encontrado")
}

@Service
@Transactional
class UsuarioService(
    private val repo: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun criar(req: UsuarioRequest): UsuarioResponse {
        if (repo.existsByEmail(req.email)) throw ConflitoException("Email '${req.email}' já cadastrado")
        val usuario = repo.save(Usuario(req.nome.trim(), req.email.trim(), passwordEncoder.encode(req.senha)))
        log.info("Usuário criado: id={}", usuario.id)
        return usuario.toResponse()
    }

    @Transactional(readOnly = true)
    fun buscar(id: Long): UsuarioResponse =
        (repo.findByIdOrNull(id) ?: throw RecursoNaoEncontradoException("Usuário $id não encontrado")).toResponse()
}

@Service
@Transactional
class AvaliacaoService(
    private val avaliacaoRepo: AvaliacaoRepository,
    private val jogoRepo: JogoRepository,
    private val usuarioRepo: UsuarioRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun avaliar(jogoId: Long, email: String, req: AvaliacaoRequest): AvaliacaoResponse {
        val jogo = jogoRepo.findByIdOrNull(jogoId)
            ?: throw RecursoNaoEncontradoException("Jogo $jogoId não encontrado")
        val usuario = usuarioRepo.findByEmail(email)
            ?: throw RecursoNaoEncontradoException("Usuário não encontrado")
        val salva = avaliacaoRepo.save(Avaliacao(req.nota, req.comentario, jogo, usuario))
        log.info("Avaliação criada: id={}, jogo={}, usuario={}", salva.id, jogoId, usuario.id)
        return salva.toResponse()
    }

    @Transactional(readOnly = true)
    fun listarPorJogo(jogoId: Long): List<AvaliacaoResponse> {
        if (!jogoRepo.existsById(jogoId)) throw RecursoNaoEncontradoException("Jogo $jogoId não encontrado")
        return avaliacaoRepo.findByJogoId(jogoId).map { it.toResponse() }
    }

    fun remover(id: Long, email: String) {
        val avaliacao = avaliacaoRepo.findByIdOrNull(id)
            ?: throw RecursoNaoEncontradoException("Avaliação $id não encontrada")
        if (avaliacao.usuario.email != email) {
            throw AcessoNegadoException("Você só pode remover as suas próprias avaliações")
        }
        avaliacaoRepo.delete(avaliacao)
        log.info("Avaliação removida: id={}", id)
    }
}

@Service
@Transactional
class FavoritoService(
    private val favoritoRepo: FavoritoRepository,
    private val jogoRepo: JogoRepository,
    private val usuarioRepo: UsuarioRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun favoritar(email: String, jogoId: Long): FavoritoResponse {
        val usuario = buscarUsuario(email)
        val jogo = jogoRepo.findByIdOrNull(jogoId)
            ?: throw RecursoNaoEncontradoException("Jogo $jogoId não encontrado")
        if (favoritoRepo.existsByUsuarioIdAndJogoId(usuario.id!!, jogoId)) {
            throw ConflitoException("O jogo $jogoId já está nos seus favoritos")
        }
        val favorito = favoritoRepo.save(Favorito(usuario, jogo))
        log.info("Usuário {} favoritou o jogo {}", usuario.id, jogoId)
        return favorito.toResponse()
    }

    fun desfavoritar(email: String, jogoId: Long) {
        val usuario = buscarUsuario(email)
        val favorito = favoritoRepo.findByUsuarioIdAndJogoId(usuario.id!!, jogoId)
            ?: throw RecursoNaoEncontradoException("O jogo $jogoId não está nos seus favoritos")
        favoritoRepo.delete(favorito)
        log.info("Usuário {} removeu o jogo {} dos favoritos", usuario.id, jogoId)
    }

    @Transactional(readOnly = true)
    fun listar(email: String): List<FavoritoResponse> =
        favoritoRepo.findByUsuarioId(buscarUsuario(email).id!!).map { it.toResponse() }

    private fun buscarUsuario(email: String) =
        usuarioRepo.findByEmail(email) ?: throw RecursoNaoEncontradoException("Usuário não encontrado")
}