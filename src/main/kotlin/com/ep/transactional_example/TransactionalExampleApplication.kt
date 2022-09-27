package com.ep.transactional_example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TransactionalExampleApplication

fun main(args: Array<String>) {
    runApplication<TransactionalExampleApplication>(*args)
}
