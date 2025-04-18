package com.vacuumlabs.wadzpay.usermanagement.controller

import com.vacuumlabs.wadzpay.usermanagement.service.LoginService
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Login Controller")
@RequestMapping("/login")
class LoginController(
    val loginService: LoginService
) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)
}
