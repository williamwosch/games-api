package br.pucpr.games.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    // transforma o claim "roles" do token em ROLE_USER / ROLE_ADMIN
    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val authorities = JwtGrantedAuthoritiesConverter().apply {
            setAuthoritiesClaimName("roles")
            setAuthorityPrefix("ROLE_")
        }
        return JwtAuthenticationConverter().apply {
            setJwtGrantedAuthoritiesConverter(authorities)
        }
    }

    @Bean
    fun filterChain(http: HttpSecurity, converter: JwtAuthenticationConverter): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .headers { it.frameOptions { frame -> frame.sameOrigin() } } // console do H2
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/error",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/h2-console/**"
                    ).permitAll()
                    // públicos: só login e cadastro
                    .requestMatchers(HttpMethod.POST, "/auth/login", "/usuarios").permitAll()
                    // avaliar jogo: qualquer usuário logado (precisa vir ANTES das regras de ADMIN)
                    .requestMatchers(HttpMethod.POST, "/jogos/*/avaliacoes").authenticated()
                    // criar, alterar e remover jogos e categorias: só ADMIN
                    .requestMatchers(HttpMethod.POST, "/jogos/**", "/categorias/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/jogos/**", "/categorias/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/jogos/**", "/categorias/**").hasRole("ADMIN")
                    // todo o resto exige apenas estar logado
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth ->
                oauth.jwt { jwt -> jwt.jwtAuthenticationConverter(converter) }
            }
        return http.build()
    }
}