package com.shohan.khatago

import com.shohan.khatago.data.BackupPayload
import com.shohan.khatago.data.BackupValidator
import com.shohan.khatago.data.BusinessRuleException
import com.shohan.khatago.data.local.ShopCreditEntity
import com.shohan.khatago.data.local.ShopEntity
import org.junit.Test

class BackupValidationTest {
    @Test(expected = BusinessRuleException::class)
    fun rejectsBrokenRelationships() {
        BackupValidator.validate(
            BackupPayload(
                shops = listOf(
                    ShopEntity(
                        id = 1,
                        name = "Store",
                        ownerName = "",
                        phone = "",
                        address = "",
                        notes = "",
                        createdAt = "2026-09-08T00:00:00"
                    )
                ),
                shopCredits = listOf(
                    ShopCreditEntity(
                        id = 1,
                        shopId = 2,
                        purchaseDate = "2026-09-08",
                        dueDate = null,
                        totalAmountMinor = 1000,
                        notes = "",
                        createdAt = "2026-09-08T00:00:00"
                    )
                )
            )
        )
    }
}
