package com.ep.transactional_example.propagation.command

import com.ep.transactional_example.propagation.domain.Additional
import com.ep.transactional_example.exception.AdditionalException
import com.ep.transactional_example.propagation.dto.CreateAddition
import com.ep.transactional_example.propagation.repository.AdditionalRepository
import com.ep.transactional_example.propagation.repository.read
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class AdditionalProcessor(
    private val additionalRepository: AdditionalRepository
) {

    @Transactional(propagation = Propagation.REQUIRED)
    fun create(createAdditionalList: List<CreateAddition>) {
        createAdditionalList.forEach {
            val additional = Additional(id = it.id, quantity = it.quantity, name = it.name, price = it.price)
            additionalRepository.save(additional)
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    fun createForRequiredExceptionCatch(createAdditionalList: List<CreateAddition>) {
        createAdditionalList.forEach {
            val additional = Additional(id = it.id, quantity = it.quantity, name = it.name, price = it.price)
            additionalRepository.save(additional)
        }
        try {
            throw AdditionalException()
        } catch (e: RuntimeException) {
            println("Runtime Catch")
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    fun createForRequiredRollback(createAdditionalList: List<CreateAddition>) {
        createAdditionalList.forEach {
            val additional = Additional(id = it.id, quantity = it.quantity, name = it.name, price = it.price)
            additionalRepository.save(additional)
        }
        throw AdditionalException()
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createForRequiresNewRollback(createAdditionalList: List<CreateAddition>) {
        createAdditionalList.forEach {
            val additional = Additional(id = it.id, quantity = it.quantity, name = it.name, price = it.price)
            additionalRepository.save(additional)
        }
        throw AdditionalException()
    }

    @Transactional(propagation = Propagation.REQUIRED)
    fun updateNameForRollbackMark(additionalId: Long, additionalName: String) {
        try {
            val additional = additionalRepository.read(additionalId)
            additional.updateName(additionalName)
        } catch (e: RuntimeException) {
            println("Runtime Catch")
        }
    }

    fun createWithRuntimeExceptionWithoutTransactional(createAdditionalList: List<CreateAddition>) {
        createAdditionalList.forEach {
            val additional = Additional(id = it.id, quantity = it.quantity, name = it.name, price = it.price)
            additionalRepository.save(additional)
        }
        throw AdditionalException()
    }
}