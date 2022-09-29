package com.ep.transactional_example.command

import com.ep.transactional_example.domain.Product
import com.ep.transactional_example.dto.CreateProduct
import com.ep.transactional_example.exception.ProductException
import com.ep.transactional_example.repository.ProductRepository
import com.ep.transactional_example.repository.read
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.math.BigDecimal

@Component
class ProductProcessor(
    private val productRepository: ProductRepository,
    private val additionalProcessor: AdditionalProcessor
) {

    @Transactional
    fun create(id: Long, name: String, price: BigDecimal) {
        val product = Product(id = id, name = name, price = price)
        productRepository.save(product)
    }

    @Transactional
    fun create(createProduct: CreateProduct) {
        val product = Product(id = createProduct.id, name = createProduct.name, price = createProduct.price)
        productRepository.save(product)
        additionalProcessor.create(createProduct.createAdditionalList)
    }

    @Transactional
    fun createWithRuntimeException(id: Long, name: String, price: BigDecimal) {
        val product = Product(id = id, name = name, price = price)
        productRepository.save(product)
        throw ProductException()
    }

    fun createWithRuntimeExceptionAndInnerMethod(id: Long, name: String, price: BigDecimal) {
        val product = Product(id = id, name = name, price = price)
        createWithInnerMethod(product)
    }

    @Transactional
    fun createWithInnerMethod(product: Product) {
        productRepository.save(product)
        throw ProductException()
    }

    @Transactional
    fun createForExceptionCatch(id: Long, name: String, price: BigDecimal) {
        val product = Product(id = id, name = name, price = price)
        productRepository.save(product)
        try {
            throw ProductException()
        } catch (e: RuntimeException) {
            println("Runtime Exception catch")
        }
    }

    @Transactional
    fun createForRequiredExceptionCatch(createProduct: CreateProduct) {
        val product = Product(id = createProduct.id, name = createProduct.name, price = createProduct.price)
        productRepository.save(product)
        additionalProcessor.createForRequiredExceptionCatch(createProduct.createAdditionalList)
    }

    @Transactional
    fun createForRequiredRollback(createProduct: CreateProduct) {
        val product = Product(id = createProduct.id, name = createProduct.name, price = createProduct.price)
        productRepository.save(product)
        try {
            additionalProcessor.createForRequiredRollback(createProduct.createAdditionalList)
        } catch (e: RuntimeException) {
            println("RuntimeException catch")
        }
    }

    @Transactional
    fun createForRequiresNewRollback(createProduct: CreateProduct) {
        val product = Product(id = createProduct.id, name = createProduct.name, price = createProduct.price)
        productRepository.save(product)
        try {
            additionalProcessor.createForRequiresNewRollback(createProduct.createAdditionalList)
        } catch (e: RuntimeException) {
            println("RuntimeException catch")
        }
    }

    @Transactional(noRollbackFor = [ProductException::class])
    fun createForRequiresNewNoRollbackFor(createProduct: CreateProduct) {
        val product = Product(id = createProduct.id, name = createProduct.name, price = createProduct.price)
        productRepository.save(product)
        additionalProcessor.create(createProduct.createAdditionalList)
        throw ProductException()
    }

    @Transactional
    fun createWithCheckedException(createProduct: CreateProduct) {
        val product = Product(id = createProduct.id, name = createProduct.name, price = createProduct.price)
        productRepository.save(product)
        additionalProcessor.create(createProduct.createAdditionalList)
        throw IOException()
    }

    @Transactional(rollbackFor = [IOException::class])
    fun createWithCheckedExceptionRollbackFor(createProduct: CreateProduct) {
        val product = Product(id = createProduct.id, name = createProduct.name, price = createProduct.price)
        productRepository.save(product)
        additionalProcessor.create(createProduct.createAdditionalList)
        throw IOException()
    }

    @Transactional
    fun createWithChildMethod(createProduct: CreateProduct) {
        val product = Product(id = createProduct.id, name = createProduct.name, price = createProduct.price)
        productRepository.save(product)
        try {
            additionalProcessor.createWithRuntimeExceptionWithoutTransactional(createProduct.createAdditionalList)
        } catch (e: RuntimeException) {
            println("RuntimeException catch")
        }
    }

    @Transactional
    fun updateNameForRollbackMark(productId: Long, productName: String, additionalId: Long, additionalName: String) {
        val product = productRepository.read(productId)
        product.updateName(productName)
        additionalProcessor.updateNameForRollbackMark(additionalId, additionalName)
    }
}