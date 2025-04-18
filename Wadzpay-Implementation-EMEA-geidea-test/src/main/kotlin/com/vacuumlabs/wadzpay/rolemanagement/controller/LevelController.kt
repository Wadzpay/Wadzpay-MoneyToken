package com.vacuumlabs.wadzpay.rolemanagement.controller
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.rolemanagement.model.Level
import com.vacuumlabs.wadzpay.rolemanagement.service.LevelService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping
@Tag(name = "Role Management  Module level Methods")
class LevelController(
    val levelService: LevelService

) {
    @PostMapping(
        value = [
            "/merchant/role/levels"
        ]
    )
    @Operation(summary = "Get list of levels")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun fetchLevels(
        principal: Authentication,
    ): MutableList<Level>? {
        return levelService.fetchLevels()
    }

    @PostMapping(
        value = [
            "/merchant/role/createLevel"
        ]
    )
    @Operation(summary = "create Level")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun createLevel(@RequestBody level: Level): Level {
        if (level.levelName.isEmpty()) {
            throw BadRequestException(ErrorCodes.BAD_REQUEST)
        }
        return levelService.createLevel(level)
    }
    @PostMapping(
        value = [
            "/merchant/role/updateLevel"
        ]
    )
    @Operation(summary = "update Level")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun updateLevel(@RequestBody level: Level): Level {
        if (level.levelId <= 0) {
            throw BadRequestException(ErrorCodes.BAD_REQUEST)
        }
        return levelService.updateLevel(level)
    }
}
