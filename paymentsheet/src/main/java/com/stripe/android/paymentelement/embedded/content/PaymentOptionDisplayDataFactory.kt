package com.stripe.android.paymentelement.embedded.content

import android.content.Context
import androidx.compose.ui.text.AnnotatedString
import com.stripe.android.lpmfoundations.paymentmethod.PaymentMethodMetadata
import com.stripe.android.paymentelement.EmbeddedPaymentElement
import com.stripe.android.paymentsheet.model.PaymentSelection
import com.stripe.android.paymentsheet.model.billingDetails
import com.stripe.android.paymentsheet.model.darkThemeIconUrl
import com.stripe.android.paymentsheet.model.drawableResourceId
import com.stripe.android.paymentsheet.model.label
import com.stripe.android.paymentsheet.model.lightThemeIconUrl
import com.stripe.android.paymentsheet.model.mandateTextFromPaymentMethodMetadata
import com.stripe.android.paymentsheet.model.paymentMethodType
import com.stripe.android.paymentsheet.model.shippingDetails
import com.stripe.android.paymentsheet.model.toPaymentSheetBillingDetails
import javax.inject.Inject

internal class PaymentOptionDisplayDataFactory @Inject constructor(
    private val iconLoader: PaymentSelection.IconLoader,
    private val context: Context,
) {
    fun create(
        selection: PaymentSelection?,
        paymentMethodMetadata: PaymentMethodMetadata,
    ): EmbeddedPaymentElement.PaymentOptionDisplayData? {
        if (selection == null) {
            return null
        }

        val mandate = when (selection) {
            is PaymentSelection.New -> {
                paymentMethodMetadata.formElementsForCode(
                    code = selection.paymentMethodType,
                    uiDefinitionFactoryArgumentsFactory = NullUiDefinitionFactoryHelper.nullEmbeddedUiDefinitionFactory
                )?.firstNotNullOfOrNull { it.mandateText }
            }
            is PaymentSelection.Saved -> selection.mandateTextFromPaymentMethodMetadata(paymentMethodMetadata)
            is PaymentSelection.CustomPaymentMethod,
            is PaymentSelection.ExternalPaymentMethod,
            is PaymentSelection.GooglePay,
            is PaymentSelection.Link,
            is PaymentSelection.ShopPay -> null
        }

        return EmbeddedPaymentElement.PaymentOptionDisplayData(
            label = selection.label.resolve(context),
            imageLoader = {
                iconLoader.load(
                    drawableResourceId = selection.drawableResourceId,
                    lightThemeIconUrl = selection.lightThemeIconUrl,
                    darkThemeIconUrl = selection.darkThemeIconUrl,
                )
            },
            billingDetails = selection.billingDetails?.toPaymentSheetBillingDetails(),
            paymentMethodType = selection.paymentMethodType,
            mandateText = if (mandate == null) null else AnnotatedString(mandate.resolve(context)),
            _shippingDetails = selection.shippingDetails,
        )
    }
}
