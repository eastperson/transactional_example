package com.ep.transactional_example.propagation.repository

import com.ep.transactional_example.propagation.domain.Additional
import com.ep.transactional_example.exception.NotFoundEntityException
import org.springframework.data.jpa.repository.JpaRepository

fun AdditionalRepository.read(id: Long): Additional {
    val findById = this.findById(id)
    if (findById.isPresent) {
        return findById.get()
    }
    throw NotFoundEntityException()
}

interface AdditionalRepository : JpaRepository<Additional, Long>