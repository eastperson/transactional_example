package com.ep.transactional_example.isolation.facade

import com.ep.transactional_example.isolation.command.ProductCreateProcessor
import com.ep.transactional_example.isolation.command.ProductUpdateProcessor
import org.springframework.stereotype.Service

@Service
class DirtyReadFacade(
    private val productCreateProcessor: ProductCreateProcessor,
    private val productUpdateProcessor: ProductUpdateProcessor
) {

    fun withReadCommitted() {


    }
}