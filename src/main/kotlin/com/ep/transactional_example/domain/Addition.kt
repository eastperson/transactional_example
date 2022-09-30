package com.ep.transactional_example.domain

import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Addition(
    @Id
    val id: Long? = null,
    val quantity: Long,
    @Column(length = 20)
    var name: String,
    val price: BigDecimal
) {
    fun updateName(additionName: String) {
        this.name = additionName
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Addition

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}