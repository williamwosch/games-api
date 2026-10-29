package br.pucpr.games.controller


import br.pucpr.games.dto.*
import br.pucpr.games.service.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/categorias")
class CategoriaController(private val service: CategoriaService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(@Valid @RequestBody req: CategoriaRequest) = service.criar(req)

    @GetMapping
    fun listar() = service.listar()

    @GetMapping("/{id}")
    fun buscar(@PathVariable id: Long) = service.buscar(id)

    @PutMapping("/{id}")
    fun atualizar(@PathVariable id: Long, @Valid @RequestBody req: CategoriaRequest) =
        service.atualizar(id, req)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletar(@PathVariable id: Long) = service.deletar(id)
}

@RestController
@RequestMapping("/jogos")
class JogoController(
    private val service: JogoService,
    private val avaliacaoService: AvaliacaoService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(@Valid @RequestBody req: JogoRequest) = service.criar(req)

    // Filtro + ordenação por query params. Ex:
    // GET /jogos?categoria=RPG&anoMin=2015&precoMax=200&ordenarPor=preco&direcao=desc
    @GetMapping
    fun listar(
        @RequestParam(required = false) titulo: String?,
        @RequestParam(required = false) categoria: String?,
        @RequestParam(required = false) anoMin: Int?,
        @RequestParam(required = false) anoMax: Int?,
        @RequestParam(required = false) precoMax: BigDecimal?,
        @RequestParam(defaultValue = "id") ordenarPor: String,
        @RequestParam(defaultValue = "asc") direcao: String
    ) = service.listar(titulo, categoria, anoMin, anoMax, precoMax, ordenarPor, direcao)

    @GetMapping("/{id}")
    fun buscar(@PathVariable id: Long) = service.buscar(id)

    @PutMapping("/{id}")
    fun atualizar(@PathVariable id: Long, @Valid @RequestBody req: JogoRequest) =
        service.atualizar(id, req)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletar(@PathVariable id: Long) = service.deletar(id)

    // ----- associar jogo <-> categoria -----
    @PostMapping("/{jogoId}/categorias/{categoriaId}")
    fun adicionarCategoria(@PathVariable jogoId: Long, @PathVariable categoriaId: Long) =
        service.adicionarCategoria(jogoId, categoriaId)

    @DeleteMapping("/{jogoId}/categorias/{categoriaId}")
    fun removerCategoria(@PathVariable jogoId: Long, @PathVariable categoriaId: Long) =
        service.removerCategoria(jogoId, categoriaId)

    // ----- avaliações do jogo -----
    @PostMapping("/{jogoId}/avaliacoes")
    @ResponseStatus(HttpStatus.CREATED)
    fun avaliar(
        @PathVariable jogoId: Long,
        @Valid @RequestBody req: AvaliacaoRequest,
        auth: Authentication
    ) = avaliacaoService.avaliar(jogoId, auth.name, req)

    @GetMapping("/{jogoId}/avaliacoes")
    fun listarAvaliacoes(@PathVariable jogoId: Long) = avaliacaoService.listarPorJogo(jogoId)
}

@RestController
@RequestMapping("/avaliacoes")
class AvaliacaoController(private val service: AvaliacaoService) {

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun remover(@PathVariable id: Long, auth: Authentication) = service.remover(id, auth.name)
}

@RestController
@RequestMapping("/usuarios")
class UsuarioController(private val service: UsuarioService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(@Valid @RequestBody req: UsuarioRequest) = service.criar(req)

    @GetMapping("/{id}")
    fun buscar(@PathVariable id: Long) = service.buscar(id)
}

@RestController
@RequestMapping("/favoritos")
class FavoritoController(private val service: FavoritoService) {

    @GetMapping
    fun listar(auth: Authentication) = service.listar(auth.name)

    @PostMapping("/{jogoId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun favoritar(@PathVariable jogoId: Long, auth: Authentication) = service.favoritar(auth.name, jogoId)

    @DeleteMapping("/{jogoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun desfavoritar(@PathVariable jogoId: Long, auth: Authentication) =
        service.desfavoritar(auth.name, jogoId)
}