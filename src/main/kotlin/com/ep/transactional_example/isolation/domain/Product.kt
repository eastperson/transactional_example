package com.ep.transactional_example.isolation.domain

import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Product(
    @Id
    val id: Long? = null,
    @Column(unique = true)
    var name: String,
    val price: BigDecimal
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Product) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Product(id=$id, name='$name', price=$price)"
    }
}