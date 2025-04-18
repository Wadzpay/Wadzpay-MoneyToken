package com.vacuumlabs.wadzpay.rolemanagement.service

import com.vacuumlabs.wadzpay.rolemanagement.model.Level
import com.vacuumlabs.wadzpay.rolemanagement.model.LevelRepository
import com.vacuumlabs.wadzpay.rolemanagement.model.LevelTransaction
import com.vacuumlabs.wadzpay.rolemanagement.model.LevelTransactionRepository
import com.vacuumlabs.wadzpay.user.UserAccountRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.NoSuchElementException

@Service
class LevelService(val levelRepository: LevelRepository, val levelTransactionRepository: LevelTransactionRepository, val userAccountRepository: UserAccountRepository) {
    fun createLevel(level: Level): Level {
        if (level.createdBy <= 0) {
            level.updatedBy = userAccountRepository.findAll().toList().last().id
            level.createdBy = userAccountRepository.findAll().toList().last().id
        }
        val currentTime = Instant.now()
        level.createdAt = currentTime
        level.updatedAt = currentTime
        val levelSaved = levelRepository.save(level)
        val levelTransaction = LevelTransaction(levelSaved.levelId, levelSaved.levelName, levelSaved.levelNumber)
        levelTransaction.createdUpdatedBy = levelSaved.updatedBy
        levelTransaction.createdUpdatedAt = currentTime
        if (levelSaved.status != null) {
            levelTransaction.status = levelSaved.status!!
        }
        levelTransaction.imageUrl = levelSaved.imageUrl
        createLevelTransaction(levelTransaction)
        return levelSaved
    }

    fun createLevelTransaction(levelTransaction: LevelTransaction) {
        levelTransactionRepository.save(levelTransaction)
    }
    fun fetchLevels(): MutableList<Level> {
        return levelRepository.findAllLevels()
    }
    fun fetchLevelsTransaction(): MutableList<LevelTransaction> {
        return levelTransactionRepository.findAll() as MutableList<LevelTransaction>
    }

    fun updateLevel(level: Level): Level {
        val levelDb = levelRepository.findById(level.levelId).get()
        try {
            levelDb.levelName = level.levelName
            levelDb.levelNumber = level.levelNumber
            levelDb.imageUrl = level.imageUrl
            if (level.status != null) { levelDb.status = level.status!! }
            val currentTime = Instant.now()
            levelDb.updatedAt = currentTime
            // userAccountRepository.findById()
            if (level.updatedBy <= 0) { levelDb.updatedBy = userAccountRepository.findAll().toList().last().id }
            val levelSaved = levelRepository.save(levelDb)
            val levelTransaction = LevelTransaction(levelSaved.levelId, levelSaved.levelName, levelSaved.levelNumber)
            levelTransaction.createdUpdatedBy = levelSaved.updatedBy
            levelTransaction.createdUpdatedAt = currentTime
            if (levelSaved.status != null) {
                println("working condition???")
                levelTransaction.status = levelSaved.status!!
            }

            levelTransaction.imageUrl = levelSaved.imageUrl
            createLevelTransaction(levelTransaction)
            return levelSaved
        } catch (ex: NoSuchElementException) {
            throw NoSuchElementException("No  Level found with Id ${level.levelId}")
        }
    }
    fun deleteLevel(level: Level) {
        levelRepository.deleteById(level.levelId)
        val levelTransaction = LevelTransaction(level.levelId, level.levelName, level.levelNumber)
        createLevelTransaction(levelTransaction)
    }

    fun getByLevelIdAndStatus(levelId: Short, status: Boolean): MutableList<Level> {
        return levelRepository.getByLevelIdAndStatus(levelId, status)
    }
}
