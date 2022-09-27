package com.ep.transactional_example.query

import com.ep.transactional_example.repository.AdditionalRepository
import com.ep.transactional_example.repository.read
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class AdditionalQuery(
    private val additionalRepository: AdditionalRepository
) {

    fun read(id: Long) = additionalRepository.read(id)
    fun count() = additionalRepository.count()
}