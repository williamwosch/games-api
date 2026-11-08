package br.pucpr.games.config

import br.pucpr.games.model.Usuario
import br.pucpr.games.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

@Configuration
class DataInitializer(
    @Value("\${admin.email}") private val adminEmail: String,
    @Value("\${admin.password}") private val adminSenha: String
) {
    private val log = LoggerFactory.getLogger(DataInitializer::class.java)

    @Bean
    fun criarAdminPadrao(repo: UsuarioRepository, encoder: PasswordEncoder) = CommandLineRunner {
        if (!repo.existsByEmail(adminEmail)) {
            val gerada = adminSenha.isBlank()
            val senha: String = if (gerada) UUID.randomUUID().toString().take(12) else adminSenha
            val hash: String = encoder.encode(senha) ?: error("Falha ao criptografar a senha do admin")

            val admin = Usuario("Administrador", adminEmail, hash)
            admin.perfil = "ADMIN"
            repo.save(admin)

            if (gerada) {
                log.warn("ADMIN_PASSWORD não definida. Senha do admin ({}) gerada: {}", adminEmail, senha)
            } else {
                log.info("Usuário administrador criado: {}", adminEmail)
            }
        }
    }
}