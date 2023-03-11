package com.ep.transactional_example.isolation.query

import com.ep.transactional_example.exception.NotFoundEntityException
import com.ep.transactional_example.isolation.domain.Product
import com.ep.transactional_example.isolation.repository.ProductRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class ProductQuery(
    private val productRepository: ProductRepository
) {
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    fun read(name: String): Product = productRepository.findByName(name) ?: throw NotFoundEntityException()
}