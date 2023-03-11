package com.ep.transactional_example.propagation.repository

import com.ep.transactional_example.propagation.domain.Addition
import com.ep.transactional_example.exception.NotFoundEntityException
import org.springframework.data.jpa.repository.JpaRepository

fun AdditionRepository.read(id: Long): Addition {
    val findById = this.findById(id)
    if (findById.isPresent) {
        return findById.get()
    }
    throw NotFoundEntityException()
}

interface AdditionRepository : JpaRepository<Addition, Long>