package com.ep.transactional_example.dto

import java.math.BigDecimal

data class CreateProduct(
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val createAddition: List<CreateAddition> = listOf()
)

data class CreateAddition(
    val id: Long,
    val quantity: Long,
    val name: String,
    val price: BigDecimal
)