package br.pucpr.games.repository

import br.pucpr.games.model.*
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal

interface CategoriaRepository : JpaRepository<Categoria, Long> {
    fun existsByNomeIgnoreCase(nome: String): Boolean
}

interface UsuarioRepository : JpaRepository<Usuario, Long> {
    fun findByEmail(email: String): Usuario?
    fun existsByEmail(email: String): Boolean
}

interface JogoRepository : JpaRepository<Jogo, Long>, JpaSpecificationExecutor<Jogo>

interface AvaliacaoRepository : JpaRepository<Avaliacao, Long> {
    fun findByJogoId(jogoId: Long): List<Avaliacao>

    @Query("select avg(a.nota) from Avaliacao a where a.jogo.id = :jogoId")
    fun mediaPorJogo(@Param("jogoId") jogoId: Long): Double?
}

interface FavoritoRepository : JpaRepository<Favorito, Long> {
    fun findByUsuarioId(usuarioId: Long): List<Favorito>
    fun findByUsuarioIdAndJogoId(usuarioId: Long, jogoId: Long): Favorito?
    fun existsByUsuarioIdAndJogoId(usuarioId: Long, jogoId: Long): Boolean
}

// Monta a consulta dinâmica: só aplica o filtro que veio preenchido
object JogoSpecs {
    fun filtrar(
        titulo: String?,
        categoria: String?,
        anoMin: Int?,
        anoMax: Int?,
        precoMax: BigDecimal?
    ): Specification<Jogo> = Specification { root, _, cb ->
        val filtros = mutableListOf<Predicate>()

        if (!titulo.isNullOrBlank()) {
            filtros += cb.like(cb.lower(root.get<String>("titulo")), "%${titulo.lowercase()}%")
        }
        if (!categoria.isNullOrBlank()) {
            val join = root.join<Jogo, Categoria>("categorias")
            filtros += cb.equal(cb.lower(join.get<String>("nome")), categoria.lowercase())
        }
        if (anoMin != null) filtros += cb.greaterThanOrEqualTo(root.get<Int>("anoLancamento"), anoMin)
        if (anoMax != null) filtros += cb.lessThanOrEqualTo(root.get<Int>("anoLancamento"), anoMax)
        if (precoMax != null) filtros += cb.lessThanOrEqualTo(root.get<BigDecimal>("preco"), precoMax)

        cb.and(*filtros.toTypedArray())
    }
}