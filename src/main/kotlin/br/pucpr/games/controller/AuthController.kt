package br.pucpr.games.controller

import br.pucpr.games.dto.LoginRequest
import br.pucpr.games.service.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(private val service: AuthService) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest) = service.login(req)
}