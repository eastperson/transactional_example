package com.ep.transactional_example.isolation

import com.ep.transactional_example.isolation.command.ProductCreateProcessor
import com.ep.transactional_example.isolation.command.ProductUpdateProcessor
import com.ep.transactional_example.isolation.query.ProductQuery
import org.assertj.core.api.Assertions.assertThat
import org.junit.ClassRule
import org.junit.Test
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.MySQLContainer
import java.math.BigDecimal

/**
 * @see <a href=https://www.baeldung.com/spring-transactional-propagation-isolation>Transaction Propagation and Isolation in Spring @Transactional</a>
 * <pre>
 *  Isolation is one of the common ACID properties: Atomicity, Consistency, Isolation, and Durability. Isolation describes how changes applied by concurrent transactions are visible to each other. Each isolation level prevents zero or more concurrency side effects on a transaction:
 *  <br/>
 *  - Dirty read: read the uncommitted change of a concurrent transaction
 *  - Nonrepeatable read: get different value on re-read of a row if a concurrent transaction updates the same row and commits
 *  - Phantom read: get different rows after re-execution of a range query if another transaction adds or removes some rows in the range and commits
 *  <br/>
 *  We can set the isolation level of a transaction by @Transactional::isolation. It has these five enumerations in Spring: DEFAULT, READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE.
 * </pre>
 */
@ActiveProfiles("test")
@SpringBootTest
@ContextConfiguration(initializers = [TransactionalIsolationExampleApplicationTests.Companion.Initializer::class])
class TransactionalIsolationExampleApplicationTests {

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
    private lateinit var productCreateProcessor: ProductCreateProcessor

    @Autowired
    private lateinit var productUpdateProcessor: ProductUpdateProcessor

    @Autowired
    private lateinit var productQuery: ProductQuery

    /**
     * 트랜잭션 격리수준 default 설정
     * the isolation level will be the default isolation of our RDBMS. Therefore, we should be careful if we change the database.
     * @see org.springframework.transaction.annotation.Isolation.DEFAULT
     */
    fun `isolation_default`() {

    }

    /**
     * 트랜잭션 발생 문제
     * Dirty read: read the uncommitted change of a concurrent transaction
     * 동시에 트랜잭션이 일어났을 때 커밋되지 않은 트랜잭션을 읽어서 발생하는 오류
     */
    @Test
    fun `dirty_read_with_isolation_READ_UNCOMMITTED`() {
        val name = "ep"
        val price = BigDecimal.valueOf(100)
        productCreateProcessor.create(name = name, price = price)
        val product = productQuery.read(name)
        assertThat(product).isNotNull
    }

    fun `dirty_read_with_isolation_READ_COMMITTED`() {

    }

    fun `dirty_read_with_isolation_REPEATABLE_READ`() {

    }

    fun `dirty_read_with_isolation_SERIALIZABLE`() {

    }

    fun `read_skew_with_isolation_READ_UNCOMMITTED`() {

    }

    fun `read_skew_with_isolation_READ_COMMITTED`() {

    }

    fun `read_skew_with_isolation_REPEATABLE_READ`() {

    }

    fun `read_skew_with_isolation_SERIALIZABLE`() {

    }

    fun `dirty_write_with_isolation_READ_UNCOMMITTED`() {

    }

    fun `dirty_write_with_isolation_READ_COMMITTED`() {

    }

    fun `dirty_write_with_isolation_REPEATABLE_READ`() {

    }

    fun `dirty_write_with_isolation_SERIALIZABLE`() {

    }

    fun `lost_update_with_isolation_READ_UNCOMMITTED`() {

    }

    fun `lost_update_with_isolation_READ_COMMITTED`() {

    }

    fun `lost_update_with_isolation_REPEATABLE_READ`() {

    }

    fun `lost_update_with_isolation_SERIALIZABLE`() {

    }

    fun `write_skew_with_isolation_READ_UNCOMMITTED`() {

    }

    fun `write_skew_with_isolation_READ_COMMITTED`() {

    }

    fun `write_skew_with_isolation_REPEATABLE_READ`() {

    }

    fun `write_skew_with_isolation_SERIALIZABLE`() {

    }
}
