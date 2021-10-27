package com.tsai.shakeit.data.source

import androidx.lifecycle.MutableLiveData
import com.tsai.shakeit.data.*

interface ShakeItDataSource {

    suspend fun postFavorite(shop: Shop): Result<Boolean>

    suspend fun postOrderToFireBase(order: Order, orderProduct: OrderProduct): Result<Boolean>

    suspend fun postProduct(product: Product): Result<Boolean>

    suspend fun deleteFavorite(shopId: String): Result<Boolean>

    suspend fun deleteOrder(orderId: String): Result<Boolean>

    suspend fun getShopInfo(shopId: String): Result<Shop>

    suspend fun getAllShop():Result<List<Shop>>

    suspend fun getProduct(shopId: String): Result<List<Product>>

    suspend fun updateOrderTotalPrice(totalPrice:Int , shopId: String):Result<Boolean>

    fun getFireBaseOrder(): MutableLiveData<List<Order>>

    fun getShopOrder(shopId: String): MutableLiveData<List<Order>>

    fun getFireBaseOrderProduct(orderId: String): MutableLiveData<List<OrderProduct>>

    fun getFavorite(): MutableLiveData<List<Shop>>

}