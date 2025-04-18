package com.vacuumlabs.wadzpay.common

class EntityNotFoundException(override val message: String) : Exception(message)

class UnprocessableEntityException(override val message: String) : Exception(message)

class DuplicateEntityException(override val message: String) : Exception(message)

class ForbiddenException(override val message: String) : Exception(message)

class UnauthorizedException(override val message: String) : Exception(message)

class BadRequestException(override val message: String) : Exception(message)

class ServiceUnavailableException(override val message: String) : Exception(message)

class TriggerRetryException(override val message: String?) : Exception()
