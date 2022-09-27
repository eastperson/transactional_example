package com.ep.transactional_example.dto

import java.math.BigDecimal

data class CreateProduct(
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val createAdditionalList: List<CreateAdditional> = listOf()
)

data class CreateAdditional(
    val id: Long,
    val quantity: Long,
    val name: String,
    val price: BigDecimal
)