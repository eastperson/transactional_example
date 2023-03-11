package com.ep.transactional_example.isolation.command

import com.ep.transactional_example.isolation.domain.Product
import com.ep.transactional_example.isolation.repository.ProductRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Component
class ProductCreateProcessor(
    private val productRepository: ProductRepository
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun create(name: String, price: BigDecimal) {
        val product = Product(name = name, price = price)
        productRepository.save(product)
    }
}