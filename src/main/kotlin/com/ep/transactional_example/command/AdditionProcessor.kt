package com.ep.transactional_example.command

import com.ep.transactional_example.domain.Addition
import com.ep.transactional_example.dto.CreateAddition
import com.ep.transactional_example.exception.AdditionException
import com.ep.transactional_example.repository.AdditionRepository
import com.ep.transactional_example.repository.read
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class AdditionProcessor(
    private val additionRepository: AdditionRepository
) {

    @Transactional(propagation = Propagation.REQUIRED)
    fun create(createAdditionList: List<CreateAddition>) {
        createAdditionList.forEach {
            val addition = Addition(id = it.id, quantity = it.quantity, name = it.name, price = it.price)
            additionRepository.save(addition)
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    fun createForRequiredExceptionCatch(createAdditionList: List<CreateAddition>) {
        createAdditionList.forEach {
            val addition = Addition(id = it.id, quantity = it.quantity, name = it.name, price = it.price)
            additionRepository.save(addition)
        }
        try {
            throw AdditionException()
        } catch (e: RuntimeException) {
            println("Runtime Catch")
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    fun createForRequiredRollback(createadditionList: List<CreateAddition>) {
        createadditionList.forEach {
            val addition = Addition(id = it.id, quantity = it.quantity, name = it.name, price = it.price)
            additionRepository.save(addition)
        }
        throw AdditionException()
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createForRequiresNewRollback(createadditionList: List<CreateAddition>) {
        createadditionList.forEach {
            val addition = Addition(id = it.id, quantity = it.quantity, name = it.name, price = it.price)
            additionRepository.save(addition)
        }
        throw AdditionException()
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun updateNameForRollbackMark(additionId: Long, additionName: String) {
        try {
            val addition = additionRepository.read(additionId)
            addition.updateName(additionName)
        } catch (e: RuntimeException) {
            println("Runtime Catch")
        }
    }

    fun createWithRuntimeExceptionWithoutTransactional(createAdditionList: List<CreateAddition>) {
        createAdditionList.forEach {
            val addition = Addition(id = it.id, quantity = it.quantity, name = it.name, price = it.price)
            additionRepository.save(addition)
        }
        throw AdditionException()
    }
}