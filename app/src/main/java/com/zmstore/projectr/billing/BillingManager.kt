package com.zmstore.projectr.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.zmstore.projectr.ui.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BillingManager handles Google Play Billing logic locally.
 * Secure server-side verification is disabled to maintain compatibility with Firebase Spark (Free) plan.
 */
class BillingManager(private val context: Context, private val viewModel: MainViewModel) : PurchasesUpdatedListener {

    private val billingClient: BillingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .build()

    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        val SKU_MONTHLY = "remedio_vip_monthly"
        val SKU_YEARLY = "remedio_vip_yearly"
        val SKU_LIFETIME = "remedio_vip_lifetime"
        val SUPPORTED_SKUS = listOf(SKU_MONTHLY, SKU_YEARLY, SKU_LIFETIME)
    }

    // Map productId -> ProductDetails (new Play Billing API)
    private var productDetailsMap: Map<String, ProductDetails> = emptyMap()

    init {
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                // Potential retry logic
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails()
                    checkExistingPurchases()
                }
            }
        })
    }

    private fun queryProductDetails() {
        // Build product list for subscriptions
        val subsProducts = listOf(
            QueryProductDetailsParams.Product.newBuilder().setProductId(SKU_MONTHLY).setProductType(BillingClient.ProductType.SUBS).build(),
            QueryProductDetailsParams.Product.newBuilder().setProductId(SKU_YEARLY).setProductType(BillingClient.ProductType.SUBS).build()
        )
        val subsParams = QueryProductDetailsParams.newBuilder().setProductList(subsProducts).build()
        billingClient.queryProductDetailsAsync(subsParams) { billingResult: BillingResult, result: QueryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val tmp = productDetailsMap.toMutableMap()
                val list = result.productDetailsList
                if (list != null) {
                    for (pd in list) {
                        tmp[pd.productId] = pd
                    }
                }
                productDetailsMap = tmp.toMap()
            }
        }

        // In-app / one-time products
        val inappProducts = listOf(
            QueryProductDetailsParams.Product.newBuilder().setProductId(SKU_LIFETIME).setProductType(BillingClient.ProductType.INAPP).build()
        )
        val inappParams = QueryProductDetailsParams.newBuilder().setProductList(inappProducts).build()
        billingClient.queryProductDetailsAsync(inappParams) { billingResult: BillingResult, result: QueryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val tmp2 = productDetailsMap.toMutableMap()
                val list = result.productDetailsList
                if (list != null) {
                    for (pd in list) {
                        tmp2[pd.productId] = pd
                    }
                }
                productDetailsMap = tmp2.toMap()
            }
        }
    }

    fun launchPurchase(activity: Activity, skuId: String) {
        val productDetails = productDetailsMap[skuId]
        if (productDetails == null) {
            queryProductDetails()
            return
        }

        // Create ProductDetailsParams - for subscriptions prefer the first available offer token
        val productDetailsParams = productDetails.subscriptionOfferDetails?.firstOrNull()?.let { offer ->
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offer.offerToken)
                .build()
        } ?: BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    /**
     * Checks if the user already has active purchases/subscriptions on app start.
     */
    fun checkExistingPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { result: BillingResult, purchases: List<Purchase> ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasActiveSub = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                if (hasActiveSub) {
                    scope.launch { viewModel.setPremium(true) }
                }
            }
        }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { result: BillingResult, purchases: List<Purchase> ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasLifetime = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                if (hasLifetime) {
                    scope.launch { viewModel.setPremium(true) }
                }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Local verification & acknowledgement
            if (!purchase.isAcknowledged) {
                val ackParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                
                billingClient.acknowledgePurchase(ackParams) { billingResult: BillingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        scope.launch { 
                            viewModel.setPremium(true)
                            viewModel.notifyPurchaseResult(true, "VIP Ativado com sucesso!") 
                        }
                    } else {
                        scope.launch { 
                            viewModel.notifyPurchaseResult(false, "Erro ao confirmar compra no Google Play.") 
                        }
                    }
                }
            } else {
                scope.launch { 
                    viewModel.setPremium(true) 
                }
            }
        }
    }

    fun disconnect() {
        if (billingClient.isReady) billingClient.endConnection()
    }
}
