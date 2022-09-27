package com.ep.transactional_example

import com.ep.transactional_example.command.ProductProcessor
import com.ep.transactional_example.dto.CreateAdditional
import com.ep.transactional_example.dto.CreateProduct
import com.ep.transactional_example.exception.NotFoundEntityException
import com.ep.transactional_example.exception.ProductException
import com.ep.transactional_example.query.AdditionalQuery
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
    private lateinit var additionalQuery: AdditionalQuery

    @Test
    fun `case-1 | 정상 처리(commit)`() {
        val id = 1L
        productProcessor.create(id, "product name", BigDecimal.valueOf(10_000))
        val newProduct = productQuery.read(id)
        assertThat(newProduct.id).isEqualTo(id)
    }

    @Test
    fun `case-2 | 롤백 | 스프링 트랜잭션의 commit, rollback`() {
        val id = 2L
        assertThrows<ProductException> { productProcessor.createWithRuntimeException(id, "product name", BigDecimal.valueOf(10_000)) }
        assertThrows<NotFoundEntityException> { productQuery.read(id) }
    }

    // https://eocoding.tistory.com/94
    @Test
    fun `case-3 | 롤백이 왜 안되지? | 프록시 객체와 Spring AOP`() {
        val id = 3L
        assertThrows<ProductException> { productProcessor.createWithRuntimeExceptionAndInnerMethod(id, "product name", BigDecimal.valueOf(10_000)) }
        val newProduct = productQuery.read(id)
        assertThat(newProduct.id).isEqualTo(id)
    }

    @Test
    fun `case-4 | 트랜잭션 병합(REQUIRED) 정상처리`() {
        val id = 4L
        val additional = CreateAdditional(id, 2, "additional name", BigDecimal.valueOf(5_000))
        val createProduct = CreateProduct(id, "product name", BigDecimal.valueOf(10_000), listOf(additional))
        productProcessor.create(createProduct)

        val newProduct = productQuery.read(id)
        assertThat(newProduct.id).isEqualTo(id)

        val newAdditional1 = additionalQuery.read(id)
        assertThat(newAdditional1.id).isEqualTo(id)
    }

    @Test
    fun `case-5 | 트랜잭션 Exception catch | 트랜잭션 Exception`() {
        val id = 5L
        productProcessor.createForExceptionCatch(id, "product name", BigDecimal.valueOf(10_000))
        val newProduct = productQuery.read(id)
        assertThat(newProduct.id).isEqualTo(id)
    }

    @Test
    fun `case-6 | 트랜잭션 병합 Exception catch | 트랜잭션 Exception`() {
        val id = 6L
        val additional = CreateAdditional(id, 2, "additional name", BigDecimal.valueOf(5_000))
        val createProduct = CreateProduct(id, "product name", BigDecimal.valueOf(10_000), listOf(additional))

        productProcessor.createForRequiredExceptionCatch(createProduct)

        val newProduct = productQuery.read(id)
        assertThat(newProduct.id).isEqualTo(id)

        val newAdditional1 = additionalQuery.read(id)
        assertThat(newAdditional1.id).isEqualTo(id)
    }

    // https://techblog.woowahan.com/2606/
    @Test
    fun `case-7 | 트랜잭션 병합 UnexpectedRollbackException 예외 발생 | 트랜잭션 전파 속성`() {
        val id = 7L
        val additional = CreateAdditional(id, 2, "additional name", BigDecimal.valueOf(5_000))
        val createProduct = CreateProduct(id, "product name", BigDecimal.valueOf(10_000), listOf(additional))

        assertThrows<UnexpectedRollbackException> { productProcessor.createForRequiredRollback(createProduct) }

        assertThrows<NotFoundEntityException> { productQuery.read(id) }
        assertThrows<NotFoundEntityException> { additionalQuery.read(id) }
    }

    @Test
    fun `case-8 | 트랜잭션 분리(REQUIRES_NEW) 롤백 | 트랜잭션 전파 속성`() {
        val id = 8L
        val additional = CreateAdditional(id, 2, "additional name", BigDecimal.valueOf(5_000))
        val createProduct = CreateProduct(id, "product name", BigDecimal.valueOf(10_000), listOf(additional))

        productProcessor.createForRequiresNewRollback(createProduct)

        val newProduct = productQuery.read(id)
        assertThat(newProduct.id).isEqualTo(id)

        assertThrows<NotFoundEntityException> { additionalQuery.read(id) }
    }

    @Test
    fun `case-9 | 트랜잭션 noRollbackFor | 트랜잭션 전파 속성`() {
        val id = 9L
        val additional = CreateAdditional(id, 2, "additional name", BigDecimal.valueOf(5_000))
        val createProduct = CreateProduct(id, "product name", BigDecimal.valueOf(10_000), listOf(additional))

        assertThrows<ProductException> { productProcessor.createForRequiresNewNoRollbackFor(createProduct) }

        val newProduct = productQuery.read(id)
        assertThat(newProduct.id).isEqualTo(id)

        val newAdditional1 = additionalQuery.read(id)
        assertThat(newAdditional1.id).isEqualTo(id)
    }

    @Test
    fun `case-10 | 이건 왜 롤백이 안되지? | 트랜잭션 CheckedException`() {
        val id = 10L
        val additional = CreateAdditional(id, 2, "additional name", BigDecimal.valueOf(5_000))
        val createProduct = CreateProduct(id, "product name", BigDecimal.valueOf(10_000), listOf(additional))

        assertThrows<IOException> { productProcessor.createWithCheckedException(createProduct) }

        val newProduct = productQuery.read(id)
        assertThat(newProduct.id).isEqualTo(id)

        val newAdditional1 = additionalQuery.read(id)
        assertThat(newAdditional1.id).isEqualTo(id)
    }

    @Test
    fun `case-11 | 트랜잭션 RollbackFor | 트랜잭션 CheckedException`() {
        val id = 11L
        val additional = CreateAdditional(id, 2, "additional name", BigDecimal.valueOf(5_000))
        val createProduct = CreateProduct(id, "product name", BigDecimal.valueOf(10_000), listOf(additional))

        assertThrows<IOException> { productProcessor.createWithCheckedExceptionRollbackFor(createProduct) }
        assertThrows<NotFoundEntityException> { productQuery.read(id) }
        assertThrows<NotFoundEntityException> { additionalQuery.read(id) }
    }

    @Test
    fun `case-12 | 이게 왜 롤백이 되지? | DataIntegrityViolationException, dirty checking`() {
        val id = 12L
        val additional = CreateAdditional(id, 2, "additional name", BigDecimal.valueOf(5_000))
        val createProduct = CreateProduct(id, "product name", BigDecimal.valueOf(10_000), listOf(additional))
        productProcessor.create(createProduct)
        val newProduct = productQuery.read(id)
        assertThat(newProduct.id).isEqualTo(id)

        assertThrows<DataIntegrityViolationException> { productProcessor.updateNameForRollbackMark(productId = id, productName = "updated product name", additionalId = id, additionalName = "updated additional name updated additional name") }

        val updatedProduct = productQuery.read(id)
        assertThat(updatedProduct.name).isEqualTo("product name")

        val updatedAdditional = additionalQuery.read(id)
        assertThat(updatedAdditional.name).isEqualTo("additional name")
    }
}
