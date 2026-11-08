package br.pucpr.games.service

import br.pucpr.games.dto.LoginRequest
import br.pucpr.games.dto.TokenResponse
import br.pucpr.games.exception.CredenciaisInvalidasException
import br.pucpr.games.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val repo: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun login(req: LoginRequest): TokenResponse {
        val usuario = repo.findByEmail(req.email)
        if (usuario == null || !passwordEncoder.matches(req.senha, usuario.senha)) {
            log.warn("Tentativa de login inválida para o email {}", req.email)
            throw CredenciaisInvalidasException("Email ou senha inválidos")
        }
        log.info("Login realizado: usuario={}, perfil={}", usuario.id, usuario.perfil)
        return TokenResponse(
            token = jwtService.gerar(usuario),
            expiraEmMinutos = jwtService.minutos,
            perfil = usuario.perfil
        )
    }
}