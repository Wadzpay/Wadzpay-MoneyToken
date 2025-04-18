package com.vacuumlabs.wadzpay.configuration

import com.vacuumlabs.wadzpay.acquireradministration.model.ErrorDetails
import com.vacuumlabs.wadzpay.acquireradministration.model.Violation
import com.vacuumlabs.wadzpay.common.DuplicateEntityException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErroValidationResponse
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.INVALID_INPUT_FORMAT
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.common.ForbiddenException
import com.vacuumlabs.wadzpay.common.ServiceUnavailableException
import com.vacuumlabs.wadzpay.common.UnauthorizedException
import com.vacuumlabs.wadzpay.common.UnprocessableEntityException
import com.vacuumlabs.wadzpay.common.ValidationError
import com.vacuumlabs.wadzpay.common.ValidationErrorResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.LocalDateTime
import java.util.StringJoiner
import javax.validation.ConstraintViolationException

@ControllerAdvice
class WadzpayExceptionHandlers {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(validationException: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val validationErrors: MutableList<ValidationError> = mutableListOf()
        validationException.bindingResult.allErrors.map {
            val validationError: ValidationError = when (it) {
                is FieldError -> ValidationError(
                    field = it.field,
                    value = it.rejectedValue?.toString() ?: "null",
                    message = it.defaultMessage ?: "N/A"
                )
                else -> ValidationError(
                    field = "",
                    value = "",
                    message = it.defaultMessage ?: "N/A"
                )
            }
            validationErrors.add(validationError)
        }

        return ResponseEntity.badRequest().body(
            ValidationErrorResponse(INVALID_INPUT_FORMAT, validationErrors)
        )
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(exception: EntityNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(HttpStatus.NOT_FOUND.value(), exception.message))
    }

    @ExceptionHandler(UnprocessableEntityException::class)
    fun handleUnprocessableEntityException(exception: UnprocessableEntityException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), exception.message))
    }

    @ExceptionHandler(DuplicateEntityException::class)
    fun handleDuplicateEntityException(exception: DuplicateEntityException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(HttpStatus.CONFLICT.value(), exception.message))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(exception: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), ErrorCodes.BAD_REQUEST))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseBody
    fun handleConstraintViolationException(exception: ConstraintViolationException): ResponseEntity<ErroValidationResponse> {
        var error = com.vacuumlabs.wadzpay.acquireradministration.model.ValidationErrorResponseCustom(mutableListOf())
        var list = error.violations
        var message: StringBuilder = StringBuilder("Following fields are not valid (")
        var messageList: MutableList<String> = mutableListOf<String>()
        val joiner = StringJoiner(",")

        for (violation in exception.getConstraintViolations()) {
            list.add(
                Violation(violation.propertyPath.toString(), violation.messageTemplate)
            )
            joiner.add(violation.propertyPath.toString())
            messageList.add(violation.propertyPath.toString())
        }
        message.append(joiner.toString())
        message.append(")")
        error.violations = list
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErroValidationResponse(HttpStatus.BAD_REQUEST.value(), message.toString(), error))
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(exception: ForbiddenException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(HttpStatus.FORBIDDEN.value(), exception.message))
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(exception: UnauthorizedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(HttpStatus.UNAUTHORIZED.value(), exception.message))
    }

    /*@ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(exception: BadRequestException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.message))
    }*/

    @ExceptionHandler(ServiceUnavailableException::class)
    fun handleServiceUnavailableException(exception: ServiceUnavailableException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), exception.message))
    }
}
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class AggregatorExceptionHandler : ResponseEntityExceptionHandler() {
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: org.springframework.http.HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val fieldErrors: List<FieldError> = ex.fieldErrors
        val errorMapping = fieldErrors.associate { it.field to it.defaultMessage }

        val errorDetails = ErrorDetails(
            timestamp = LocalDateTime.now(),
            message = "Validation failed for one or more fields",
            details = errorMapping
        )
        return ResponseEntity(errorDetails, HttpStatus.BAD_REQUEST)
    }
}
