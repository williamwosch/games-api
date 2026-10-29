package br.pucpr.games.exception

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

class RecursoNaoEncontradoException(mensagem: String) : RuntimeException(mensagem)
class ConflitoException(mensagem: String) : RuntimeException(mensagem)
class AcessoNegadoException(mensagem: String) : RuntimeException(mensagem)

data class ErroResponse(
    val status: Int,
    val erro: String,
    val mensagem: String,
    val campos: Map<String, String>? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(RecursoNaoEncontradoException::class)
    fun naoEncontrado(ex: RecursoNaoEncontradoException): ResponseEntity<ErroResponse> {
        log.warn("404 - {}", ex.message)
        return resposta(HttpStatus.NOT_FOUND, ex.message)
    }

    @ExceptionHandler(ConflitoException::class)
    fun conflito(ex: ConflitoException): ResponseEntity<ErroResponse> {
        log.warn("409 - {}", ex.message)
        return resposta(HttpStatus.CONFLICT, ex.message)
    }

    @ExceptionHandler(AcessoNegadoException::class)
    fun acessoNegado(ex: AcessoNegadoException): ResponseEntity<ErroResponse> {
        log.warn("403 - {}", ex.message)
        return resposta(HttpStatus.FORBIDDEN, ex.message)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun integridade(ex: DataIntegrityViolationException): ResponseEntity<ErroResponse> {
        log.warn("409 - violação de integridade: {}", ex.mostSpecificCause.message)
        return resposta(HttpStatus.CONFLICT, "Operação não permitida: o registro está em uso ou é duplicado")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validacao(ex: MethodArgumentNotValidException): ResponseEntity<ErroResponse> {
        val campos = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "inválido") }
        log.warn("400 - validação falhou: {}", campos)
        return ResponseEntity.badRequest().body(
            ErroResponse(400, "Requisição inválida", "Existem campos inválidos", campos)
        )
    }

    private fun resposta(status: HttpStatus, mensagem: String?) =
        ResponseEntity.status(status).body(ErroResponse(status.value(), status.reasonPhrase, mensagem ?: ""))
}