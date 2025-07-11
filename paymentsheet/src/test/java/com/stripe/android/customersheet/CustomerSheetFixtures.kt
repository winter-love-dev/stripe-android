package com.stripe.android.customersheet

import androidx.compose.ui.graphics.Color
import com.stripe.android.ExperimentalAllowsRemovalOfLastSavedPaymentMethodApi
import com.stripe.android.model.CardBrand
import com.stripe.android.paymentsheet.PaymentSheet

internal object CustomerSheetFixtures {
    val MINIMUM_CONFIG = CustomerSheet.Configuration
        .builder(merchantDisplayName = "Merchant, Inc")
        .build()

    val CONFIG_WITH_GOOGLE_PAY_ENABLED = CustomerSheet.Configuration
        .builder(merchantDisplayName = "Merchant, Inc")
        .googlePayEnabled(googlePayEnabled = true)
        .build()

    @OptIn(ExperimentalAllowsRemovalOfLastSavedPaymentMethodApi::class)
    val CONFIG_WITH_EVERYTHING = CustomerSheet.Configuration
        .builder(merchantDisplayName = "Merchant, Inc")
        .defaultBillingDetails(PaymentSheet.BillingDetails(name = "Skyler"))
        .headerTextForSelectionScreen("Select a payment method!")
        .appearance(
            PaymentSheet.Appearance(
                colorsLight = PaymentSheet.Colors.configureDefaultLight(primary = Color(0)),
                colorsDark = PaymentSheet.Colors.configureDefaultDark(primary = Color(0)),
                shapes = PaymentSheet.Shapes(
                    cornerRadiusDp = 0.0f,
                    borderStrokeWidthDp = 0.0f
                ),
                typography = PaymentSheet.Typography(
                    sizeScaleFactor = 1.1f,
                    fontResId = 0
                ),
                primaryButton = PaymentSheet.PrimaryButton(
                    colorsLight = PaymentSheet.PrimaryButtonColors(background = 0, onBackground = 0, border = 0),
                    colorsDark = PaymentSheet.PrimaryButtonColors(background = 0, onBackground = 0, border = 0),
                    shape = PaymentSheet.PrimaryButtonShape(
                        cornerRadiusDp = 0.0f,
                        borderStrokeWidthDp = 20.0f
                    ),
                    typography = PaymentSheet.PrimaryButtonTypography(
                        fontResId = 0
                    )
                )
            )
        )
        .billingDetailsCollectionConfiguration(
            PaymentSheet.BillingDetailsCollectionConfiguration(
                name = PaymentSheet.BillingDetailsCollectionConfiguration.CollectionMode.Always,
                email = PaymentSheet.BillingDetailsCollectionConfiguration.CollectionMode.Always,
                phone = PaymentSheet.BillingDetailsCollectionConfiguration.CollectionMode.Always,
                address = PaymentSheet.BillingDetailsCollectionConfiguration.AddressCollectionMode.Full,
                attachDefaultsToPaymentMethod = true,
            )
        )
        .preferredNetworks(listOf(CardBrand.CartesBancaires, CardBrand.Visa))
        .allowsRemovalOfLastSavedPaymentMethod(false)
        .paymentMethodOrder(listOf("klarna", "afterpay", "card"))
        .googlePayEnabled(googlePayEnabled = true)
        .build()
}
