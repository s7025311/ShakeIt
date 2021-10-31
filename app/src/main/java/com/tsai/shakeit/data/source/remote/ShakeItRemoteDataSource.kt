package com.tsai.shakeit.data.source.remote

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.ktx.*
import com.tsai.shakeit.ShakeItApplication
import com.tsai.shakeit.data.*
import com.tsai.shakeit.data.source.ShakeItDataSource
import com.tsai.shakeit.ui.home.TAG
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val ORDERS = "orders"
private const val KEY_CREATED_TIME = "date"
private const val FAVORITE = "favorite"
private const val SHOP = "shop"
private const val ORDER_PRODUCT = "orderProduct"
private const val PRODUCT = "product"
private const val COMMENT = "Comment"

object ShakeItRemoteDataSource : ShakeItDataSource {

    override suspend fun postFavorite(shop: Shop): Result<Boolean> =
        suspendCoroutine { continuation ->

            val favorite = FirebaseFirestore.getInstance().collection(FAVORITE)
            val document = favorite.document(shop.shop_Id)

            document
                .set(shop)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i(TAG, "Publish: $shop")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {
                            Log.w(
                                TAG,
                                "[${this::class.simpleName}] Error getting documents. ${it.message}"
                            )
                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail("postFavorite Failed"))
                    }
                }
        }

    override suspend fun postOrderToFireBase(
        order: Order,
        orderProduct: OrderProduct
    ): Result<Boolean> =
        suspendCoroutine { continuation ->

            val orders = FirebaseFirestore.getInstance().collection(ORDERS)
            val document = orders.document(order.shop_Id)

            order.order_Id = order.shop_Id

            document
                .set(order)

            document.collection(ORDER_PRODUCT).document()
                .set(orderProduct)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i(TAG, "orderProduct: $order")
                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {
                            Log.w(
                                TAG,
                                "[${this::class.simpleName}] Error post documents. ${it.message}"
                            )
                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail("orderProduct Failed"))
                    }
                }
        }

    override suspend fun postProduct(product: Product): Result<Boolean> =
        suspendCoroutine { continuation ->

            val fireBaseProduct = FirebaseFirestore.getInstance().collection(PRODUCT)
            val document = fireBaseProduct.document()

            product.id = document.id

            document
                .set(product)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i(TAG, "Product: $product")
                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {
                            Log.w(
                                TAG,
                                "[${this::class.simpleName}] Error post documents. ${it.message}"
                            )
                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail("Product Failed"))
                    }
                }


        }

    override suspend fun postComment(shopId: String, comment: Comment): Result<Boolean> =
        suspendCoroutine { continuation ->

            val shopCollection = FirebaseFirestore.getInstance().collection(SHOP)
            val document = shopCollection.document(shopId).collection(COMMENT).document()

            document
                .set(comment)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(ShakeItApplication.instance, "發佈成功！", Toast.LENGTH_SHORT)
                            .show()
                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {
                            Log.w(
                                TAG,
                                "[${this::class.simpleName}] Error post comment. ${it.message}"
                            )
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail("post comment Failed"))
                    }
                }
        }

    override suspend fun deleteFavorite(shopId: String): Result<Boolean> =
        suspendCoroutine { continuation ->

            val favorite = FirebaseFirestore.getInstance().collection(FAVORITE)
            val document = favorite.document(shopId)

            document
                .delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(ShakeItApplication.instance, "已移除此收藏", Toast.LENGTH_SHORT)
                            .show()

                    } else {
                        task.exception?.let {
                            Log.w(
                                TAG,
                                "[${this::class.simpleName}] Error delete documents. ${it.message}"
                            )
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail("deleteFavorite Failed"))
                    }
                }
        }

    override suspend fun deleteOrder(orderId: String): Result<Boolean> =
        suspendCoroutine { continuation ->

            val order = FirebaseFirestore.getInstance().collection(ORDERS)
            val document = order.document(orderId)


            document
                .collection(ORDER_PRODUCT)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.documents?.forEach { it.reference.delete() }
                        Toast.makeText(ShakeItApplication.instance, "已移除訂單", Toast.LENGTH_SHORT)
                            .show()
                        document.delete()

                    } else {
                        task.exception?.let {
                            Log.w(
                                TAG,
                                "[${this::class.simpleName}] Error delete documents. ${it.message}"
                            )
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail("deleteOrder Failed"))
                    }
                }
        }


    override suspend fun getShopInfo(shopId: String): Result<Shop> =
        suspendCoroutine { continuation ->

            val shopInfo = FirebaseFirestore.getInstance().collection(SHOP)
            val document = shopInfo.document(shopId)

            document
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val shopData = task.result!!.toObject(Shop::class.java)
                        Log.d(TAG, shopData.toString())
                        continuation.resume(Result.Success(shopData!!))
                    } else {
                        task.exception?.let {
                            Log.w(
                                TAG,
                                "[${this::class.simpleName}] Error shopInfo documents. ${it.message}"
                            )
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail("getShopInfo Failed"))
                    }
                }
        }

    override suspend fun getAllShop(): Result<List<Shop>> =
        suspendCoroutine { continuation ->

            val shop = FirebaseFirestore.getInstance().collection(SHOP)

            shop
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val shopData = task.result!!.toObjects(Shop::class.java)
                        continuation.resume(Result.Success(shopData))
                    } else {
                        task.exception?.let {
                            Log.w(
                                TAG,
                                "[${this::class.simpleName}] Error shopInfo documents. ${it.message}"
                            )
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail("getShopInfo Failed"))
                    }
                }
        }

    override suspend fun getProduct(shopName: String): Result<List<Product>> =
        suspendCoroutine { continuation ->

            val branchProduct = FirebaseFirestore.getInstance().collection(PRODUCT)


            branchProduct
                .whereEqualTo("shop_Name", shopName)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val shopData = task.result!!.toObjects(Product::class.java)
//                        Log.d(TAG, shopData.toString())
                        continuation.resume(Result.Success(shopData))
                    } else {
                        task.exception?.let {
                            Log.w(
                                TAG,
                                "[${this::class.simpleName}] Error get documents. ${it.message}"
                            )
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail("getProduct Failed"))
                    }
                }
        }

    override suspend fun updateOrderTotalPrice(totalPrice: Int, shopId: String): Result<Boolean> =
        suspendCoroutine { continuation ->

            val order = FirebaseFirestore.getInstance().collection(ORDERS)
            val document = order.document(shopId)

            document
                .update("order_Price", totalPrice)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {
                            Log.w(
                                TAG,
                                "[${this::class.simpleName}] Error update documents. ${it.message}"
                            )
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail("update Failed"))
                    }
                }
        }

    override suspend fun getComment(shopId: String): Result<List<Comment>> =
        suspendCoroutine { continuation ->

            val comment = FirebaseFirestore.getInstance().collection(SHOP)
            val document = comment.document(shopId).collection(COMMENT)

            document
                .orderBy(KEY_CREATED_TIME,Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val commentList = task.result.toObjects(Comment::class.java)
                        continuation.resume(Result.Success(commentList))
                    } else {
                        task.exception?.let {
                            Log.w(
                                TAG,
                                "[${this::class.simpleName}] Error getComment documents. ${it.message}"
                            )
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail("getComment Failed"))
                    }
                }
        }

    override fun getFireBaseOrder(): MutableLiveData<List<Order>> {

        val liveData = MutableLiveData<List<Order>>()

        FirebaseFirestore.getInstance()
            .collection(ORDERS)
            .orderBy(KEY_CREATED_TIME, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->

                val list = mutableListOf<Order>()

                if (snapshot != null) {
                    for (document in snapshot) {
//                        Log.d(TAG, "Current data: ${document.data}")
                        val order = document.toObject(Order::class.java)
                        list.add(order)
                    }
                }
                liveData.value = list
            }
        return liveData
    }

    override fun getShopOrder(shopId: String): MutableLiveData<List<Order>> {

        val liveData = MutableLiveData<List<Order>>()

        FirebaseFirestore.getInstance()
            .collection(ORDERS)
            .whereEqualTo("shop_Id", shopId)
            .orderBy(KEY_CREATED_TIME, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->

                val list = mutableListOf<Order>()

                if (snapshot != null) {
                    for (document in snapshot) {
//                        Log.d(TAG, "Current data: ${document.data}")
                        val order = document.toObject(Order::class.java)
                        list.add(order)
                    }
                }
                liveData.value = list
            }
        Log.d(TAG, "return")
        return liveData
    }


    override fun getFireBaseOrderProduct(orderId: String): MutableLiveData<List<OrderProduct>> {

        val liveData = MutableLiveData<List<OrderProduct>>()

        FirebaseFirestore.getInstance()
            .collection(ORDERS)
            .document(orderId)
            .collection(ORDER_PRODUCT)
            .addSnapshotListener { snapshot, e ->

                val list = mutableListOf<OrderProduct>()

                for (document in snapshot!!) {
//                    Log.d(TAG, document.data.toString())
                    val orderProduct = document.toObject(OrderProduct::class.java)
                    list.add(orderProduct)
                }
                liveData.value = list
            }
        return liveData
    }

    override fun getFavorite(): MutableLiveData<List<Shop>> {

        val liveData = MutableLiveData<List<Shop>>()

        FirebaseFirestore.getInstance()
            .collection(FAVORITE)
            .addSnapshotListener { snapshot, e ->

                val list = mutableListOf<Shop>()

                if (snapshot != null) {
                    for (document in snapshot) {
//                        Log.d(TAG, "Current data: ${document.data}")
                        val shop = document.toObject(Shop::class.java)
                        list.add(shop)
                    }
                }
                liveData.value = list
            }
        return liveData
    }
}