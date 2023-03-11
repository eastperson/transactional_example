package com.ep.transactional_example.isolation.repository

import com.ep.transactional_example.exception.NotFoundEntityException
import com.ep.transactional_example.isolation.domain.Product
import org.springframework.data.jpa.repository.JpaRepository

fun ProductRepository.read(id: Long): Product {
    val findById = this.findById(id)
    if (findById.isPresent) {
        return findById.get()
    }
    throw NotFoundEntityException()
}

interface ProductRepository : JpaRepository<Product, Long> {
    fun findByName(name: String): Product?
}