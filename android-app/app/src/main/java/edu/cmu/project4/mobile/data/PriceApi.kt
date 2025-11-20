package edu.cmu.project4.mobile.data

import retrofit2.http.GET
import retrofit2.http.Query

interface PriceApi {
    @GET("api/price")
    suspend fun fetchPrice(
        @Query("symbol") symbol: String,
        @Query("clientId") clientId: String
    ): PriceResponse
}
