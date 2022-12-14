package com.ep.transactional_example

import com.ep.transactional_example.command.ProductProcessor
import com.ep.transactional_example.dto.CreateAddition
import com.ep.transactional_example.dto.CreateProduct
import com.ep.transactional_example.exception.NotFoundEntityException
import com.ep.transactional_example.exception.ProductException
import com.ep.transactional_example.query.AdditionQuery
import com.ep.transactional_example.query.ProductQuery
import org.assertj.core.api.Assertions.assertThat
import org.junit.ClassRule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.UnexpectedRollbackException
import org.testcontainers.containers.MySQLContainer
import java.io.IOException
import java.math.BigDecimal

@ActiveProfiles("test")
@SpringBootTest
@ContextConfiguration(initializers = [IntegrationTest.Companion.Initializer::class])
class IntegrationTest {

    companion object {

        @kotlin.jvm.JvmField
        @ClassRule
        val mySQLContainer = MySQLContainer<Nothing>("mysql").apply {
            withDatabaseName("transaction-container")
            withUsername("ep")
            withPassword("1234")
        }

        class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
            override fun initialize(applicationContext: ConfigurableApplicationContext) {
                TestPropertyValues.of(
                    "spring.datasource.url=${mySQLContainer.jdbcUrl}",
                    "spring.datasource.username=${mySQLContainer.username}",
                    "spring.datasource.password=${mySQLContainer.password}"
                ).applyTo(applicationContext.environment)
            }
        }

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            mySQLContainer.start()
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            mySQLContainer.stop()
        }
    }

    @Autowired
    private lateinit var productProcessor: ProductProcessor

    @Autowired
    private lateinit var productQuery: ProductQuery

    @Autowired
    private lateinit var additionQuery: AdditionQuery

    @Test
    fun `case-1 | ?????? ??????(commit)`() {
        val id = 1L
        productProcessor.create(id, "product name", BigDecimal.valueOf(10_000))
        val newProduct = productQuery.read(id)
        assertThat(newProduct.id).isEqualTo(id)
    }

    @Test
    fun `case-2 | ?????? | ????????? ??????????????? commit, rollback`() {
        val id = 2L
        assertThrows<ProductException> { productProcessor.createWithRuntimeException(id, "product name", BigDecimal.valueOf(10_000)) }
        assertThrows<NotFoundEntityException> { productQuery.read(id) }
    }

    @Test
    fun `case-3 | ???????????? Exception catch | ???????????? Exception`() {
        val id = 3L
        productProcessor.createForExceptionCatch(id, "product name", BigDecimal.valueOf(10_000))
        val newProduct = productQuery.read(id)
        assertThat(newProduct.id).isEqualTo(id)
    }

    @Test
    fun `case-4 | ???????????? ??????(REQUIRED) ????????????`() {
        val id = 4L
        val addition = CreateAddition(id, 2, "addition name", BigDecimal.valueOf(5_000))
        val createProduct = CreateProduct(id, "product name", BigDecimal.valueOf(10_000), listOf(addition))
        productProcessor.create(createProduct)

        val newProduct = productQuery.read(id)
        assertThat(newProduct.id).isEqualTo(id)

        val newAddition = additionQuery.read(id)
        assertThat(newAddition.id).isEqualTo(id)
    }

    @Test
    fun `case-5 | ???????????? ?????? Exception catch | ???????????? Exception`() {
        val id = 5L
        val addition = CreateAddition(id, 2, "addition name", BigDecimal.valueOf(5_000))
        val createProduct = CreateProduct(id, "product name", BigDecimal.valueOf(10_000), listOf(addition))

        productProcessor.createForRequiredExceptionCatch(createProduct)

        val newProduct = productQuery.read(id)
        assertThat(newProduct.id).isEqualTo(id)

        val newAddition = additionQuery.read(id)
        assertThat(newAddition.id).isEqualTo(id)
    }

    @Test
    fun `case-6 | ???????????? ?????? ?????? try-catch | ???????????? ?????? ??????`() {
        val id = 6L
        val addition = CreateAddition(id, 2, "addition name", BigDecimal.valueOf(5_000))
        val createProduct = CreateProduct(id, "product name", BigDecimal.valueOf(10_000), listOf(addition))

        productProcessor.createWithChildMethod(createProduct)

        val newProduct = productQuery.read(id)
        assertThat(newProduct.id).isEqualTo(id)

        val newAddition = additionQuery.read(id)
        assertThat(newAddition.id).isEqualTo(id)
    }

    // https://eocoding.tistory.com/94
    @Test
    fun `case-7 | ????????? ??? ?????????? | ????????? ????????? Spring AOP`() {
        val id = 7L
        assertThrows<ProductException> { productProcessor.createWithRuntimeExceptionAndInnerMethod(id, "product name", BigDecimal.valueOf(10_000)) }
        val newProduct = productQuery.read(id)
        assertThat(newProduct.id).isEqualTo(id)
    }

    // https://techblog.woowahan.com/2606/
    @Test
    fun `case-8 | ???????????? ?????? UnexpectedRollbackException ?????? ?????? | ???????????? ?????? ??????`() {
        val id = 8L
        val addition = CreateAddition(id, 2, "addition name", BigDecimal.valueOf(5_000))
        val createProduct = CreateProduct(id, "product name", BigDecimal.valueOf(10_000), listOf(addition))

        assertThrows<UnexpectedRollbackException> { productProcessor.createForRequiredRollback(createProduct) }

        assertThrows<NotFoundEntityException> { productQuery.read(id) }
        assertThrows<NotFoundEntityException> { additionQuery.read(id) }
    }

    @Test
    fun `case-9 | ???????????? ??????(REQUIRES_NEW) ?????? | ???????????? ?????? ??????`() {
        val id = 9L
        val addition = CreateAddition(id, 2, "addition name", BigDecimal.valueOf(5_000))
        val createProduct = CreateProduct(id, "product name", BigDecimal.valueOf(10_000), listOf(addition))

        productProcessor.createForRequiresNewRollback(createProduct)

        val newProduct = productQuery.read(id)
        assertThat(newProduct.id).isEqualTo(id)

        assertThrows<NotFoundEntityException> { additionQuery.read(id) }
    }

    @Test
    fun `case-10 | ?????? ??? ????????? ?????????? | ???????????? CheckedException`() {
        val id = 10L
        val addition = CreateAddition(id, 2, "addition name", BigDecimal.valueOf(5_000))
        val createProduct = CreateProduct(id, "product name", BigDecimal.valueOf(10_000), listOf(addition))

        assertThrows<IOException> { productProcessor.createWithCheckedException(createProduct) }

        val newProduct = productQuery.read(id)
        assertThat(newProduct.id).isEqualTo(id)

        val newAddition = additionQuery.read(id)
        assertThat(newAddition.id).isEqualTo(id)
    }

    @Test
    fun `case-11 | ???????????? RollbackFor | ???????????? CheckedException`() {
        val id = 11L
        val addition = CreateAddition(id, 2, "addition name", BigDecimal.valueOf(5_000))
        val createProduct = CreateProduct(id, "product name", BigDecimal.valueOf(10_000), listOf(addition))

        assertThrows<IOException> { productProcessor.createWithCheckedExceptionRollbackFor(createProduct) }
        assertThrows<NotFoundEntityException> { productQuery.read(id) }
        assertThrows<NotFoundEntityException> { additionQuery.read(id) }
    }

    @Test
    fun `case-12 | ???????????? noRollbackFor | ???????????? ?????? ??????`() {
        val id = 9L
        val addition = CreateAddition(id, 2, "addition name", BigDecimal.valueOf(5_000))
        val createProduct = CreateProduct(id, "product name", BigDecimal.valueOf(10_000), listOf(addition))

        assertThrows<ProductException> { productProcessor.createForRequiresNewNoRollbackFor(createProduct) }

        val newProduct = productQuery.read(id)
        assertThat(newProduct.id).isEqualTo(id)

        val newAddition = additionQuery.read(id)
        assertThat(newAddition.id).isEqualTo(id)
    }

    @Test
    fun `case-13 | ?????? ??? ????????? ??????? | DataIntegrityViolationException, dirty checking`() {
        val id = 12L
        val addition = CreateAddition(id, 2, "addition name", BigDecimal.valueOf(5_000))
        val createProduct = CreateProduct(id, "product name", BigDecimal.valueOf(10_000), listOf(addition))
        productProcessor.create(createProduct)
        val newProduct = productQuery.read(id)
        assertThat(newProduct.id).isEqualTo(id)

        assertThrows<DataIntegrityViolationException> { productProcessor.updateNameForRollbackMark(productId = id, productName = "updated product name", additionId = id, additionName = "updated addition name updated addition name") }

        val updatedProduct = productQuery.read(id)
        assertThat(updatedProduct.name).isEqualTo("product name")

        val updatedAddition = additionQuery.read(id)
        assertThat(updatedAddition.name).isEqualTo("addition name")
    }
}
