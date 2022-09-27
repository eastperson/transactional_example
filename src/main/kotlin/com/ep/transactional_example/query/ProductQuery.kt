package com.ep.transactional_example.query

import com.ep.transactional_example.repository.ProductRepository
import com.ep.transactional_example.repository.read
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class ProductQuery(
    private val productRepository: ProductRepository
) {

    fun read(id: Long) = productRepository.read(id)
}