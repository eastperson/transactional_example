package com.ep.transactional_example.propagation.query

import com.ep.transactional_example.propagation.repository.AdditionRepository
import com.ep.transactional_example.propagation.repository.read
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class AdditionQuery(
    private val additionRepository: AdditionRepository
) {

    fun read(id: Long) = additionRepository.read(id)
    fun count() = additionRepository.count()
}