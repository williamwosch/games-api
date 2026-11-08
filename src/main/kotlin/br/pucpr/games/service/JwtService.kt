package br.pucpr.games.service

import br.pucpr.games.model.Usuario
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class JwtService(
    private val encoder: JwtEncoder,
    @Value("\${jwt.expiration-minutes}") val minutos: Long
) {
    fun gerar(usuario: Usuario): String {
        val agora = Instant.now()
        val claims = JwtClaimsSet.builder()
            .issuer("games-api")
            .issuedAt(agora)
            .expiresAt(agora.plus(minutos, ChronoUnit.MINUTES))
            .subject(usuario.email)
            .claim("roles", listOf(usuario.perfil))
            .build()
        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        return encoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
    }
}