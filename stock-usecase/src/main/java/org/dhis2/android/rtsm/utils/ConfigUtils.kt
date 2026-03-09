package org.dhis2.android.rtsm.utils

import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.bindings.stockCount
import org.dhis2.bindings.stockDiscarded
import org.dhis2.bindings.stockDistribution
import org.hisp.dhis.android.core.usecase.stock.StockUseCase

object ConfigUtils {
    @JvmStatic
    fun getTransactionDataElement(
        transactionType: TransactionType,
        stockUseCase: StockUseCase,
    ): String {
        val dataElementUid =
            when (transactionType) {
                TransactionType.DISTRIBUTION -> stockUseCase.stockDistribution()
                TransactionType.CORRECTION -> stockUseCase.stockCount()
                TransactionType.DISCARD -> stockUseCase.stockDiscarded()
            }

        return dataElementUid
    }
}
