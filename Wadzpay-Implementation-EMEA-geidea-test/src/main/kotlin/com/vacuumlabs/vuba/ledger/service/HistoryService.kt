package com.vacuumlabs.vuba.ledger.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vacuumlabs.vuba.ledger.data.HistoryRepository
import com.vacuumlabs.vuba.ledger.model.ResponseHistory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@Service
class HistoryService(
    private val json: ObjectMapper,
    private val historyRepo: HistoryRepository,
    private val entityManager: EntityManager
) {
    val retryInterval = 500L
    val retryLimit = 15

    @Transactional
    fun getHistory(namespace: String, key: String) =
        historyRepo.findByIdempotencyNamespaceAndIdempotencyKey(namespace, key)

    @Transactional
    fun <T : Any> createHistory(namespace: String, key: String, request: T) = try {
        historyRepo.save(
            ResponseHistory(
                namespace,
                key,
                json.writeValueAsString(request),
                null,
                null
            )
        )
    } catch (e: DataIntegrityViolationException) {
        throw DuplicateResponseException(e.message)
    }

    @Transactional
    fun updateHistory(record: ResponseHistory, response: ResponseEntity<String>) {
        record.responseBody = response.body
        record.responseCode = response.statusCodeValue
        historyRepo.save(record)
    }

    private fun error500() = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error")

    fun <T : Any> idempotent(
        namespace: String,
        key: String,
        request: T,
        createResponse: () -> ResponseEntity<String>
    ): ResponseEntity<String> {
        val (historyRecord, isNew) = try {
            getHistory(namespace, key)?.let { it to false }
                ?: createHistory(namespace, key, request) to true
        } catch (e: DuplicateResponseException) {
            // race condition: same request already processing
            getHistory(namespace, key)!! to false
        }

        if (isNew) {
            return createResponse().also { updateHistory(historyRecord, it) }
        } else {
            // validate request body
            val isMatch = try {
                // TODO more advanced / pluggable equality check
                json.readValue(historyRecord.requestBody, request::class.java) == request
            } catch (e: Exception) {
                false
            }
            if (!isMatch) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mismatch with previous request")

            // return previous response if already calculated
            historyRecord.let {
                if (it.responseBody != null)
                    return ResponseEntity.status(it.responseCode!!).body(it.responseBody)
            }

            // wait for response to be available
            // detach fetched entity from EntityManager to trigger re-fetch
            entityManager.detach(historyRecord)
            repeat(retryLimit) {
                Thread.sleep(retryInterval)
                getHistory(namespace, key)?.let {
                    if (it.responseBody != null)
                        return ResponseEntity.status(it.responseCode!!).body(it.responseBody)
                    entityManager.detach(it)
                } ?: return error500()
            }
            // maximum number of retries reached
            return error500()
        }
    }
}
