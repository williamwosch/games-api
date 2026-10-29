package br.pucpr.games.dto

import br.pucpr.games.model.*
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDateTime

// ---------- ENTRADA (com validações) ----------
data class CategoriaRequest(
    @field:NotBlank(message = "O nome é obrigatório")
    @field:Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
    val nome: String
)

data class JogoRequest(
    @field:NotBlank(message = "O título é obrigatório")
    val titulo: String,
    @field:Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
    val descricao: String?,
    @field:Min(value = 1950, message = "Ano mínimo é 1950")
    @field:Max(value = 2100, message = "Ano máximo é 2100")
    val anoLancamento: Int,
    @field:DecimalMin(value = "0.0", message = "O preço não pode ser negativo")
    val preco: BigDecimal,
    val categoriaIds: Set<Long> = emptySet()
)

data class UsuarioRequest(
    @field:NotBlank(message = "O nome é obrigatório")
    val nome: String,
    @field:NotBlank(message = "O email é obrigatório")
    @field:Email(message = "Email inválido")
    val email: String,
    @field:Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    val senha: String
)

data class AvaliacaoRequest(
    @field:Min(value = 1, message = "A nota mínima é 1")
    @field:Max(value = 5, message = "A nota máxima é 5")
    val nota: Int,
    @field:Size(max = 500, message = "O comentário deve ter no máximo 500 caracteres")
    val comentario: String?
)

// ---------- SAÍDA (nunca devolve a entidade nem a senha) ----------
data class CategoriaResponse(val id: Long, val nome: String)

data class JogoResponse(
    val id: Long,
    val titulo: String,
    val descricao: String?,
    val anoLancamento: Int,
    val preco: BigDecimal,
    val categorias: List<CategoriaResponse>,
    val notaMedia: Double?
)

data class UsuarioResponse(val id: Long, val nome: String, val email: String)

data class AvaliacaoResponse(
    val id: Long,
    val nota: Int,
    val comentario: String?,
    val dataAvaliacao: LocalDateTime,
    val jogoId: Long,
    val usuarioNome: String
)

data class FavoritoResponse(val jogoId: Long, val titulo: String, val dataFavoritado: LocalDateTime)

// ---------- CONVERSORES entidade -> resposta ----------
fun Categoria.toResponse() = CategoriaResponse(id!!, nome)

fun Jogo.toResponse(notaMedia: Double?) = JogoResponse(
    id!!, titulo, descricao, anoLancamento, preco,
    categorias.map { it.toResponse() }.sortedBy { it.nome },
    notaMedia?.let { Math.round(it * 10) / 10.0 }
)

fun Usuario.toResponse() = UsuarioResponse(id!!, nome, email)

fun Avaliacao.toResponse() = AvaliacaoResponse(id!!, nota, comentario, dataAvaliacao, jogo.id!!, usuario.nome)

fun Favorito.toResponse() = FavoritoResponse(jogo.id!!, jogo.titulo, dataFavoritado)