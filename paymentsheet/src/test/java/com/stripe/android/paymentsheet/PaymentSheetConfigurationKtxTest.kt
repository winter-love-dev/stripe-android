package com.stripe.android.paymentsheet

import android.content.res.ColorStateList
import android.graphics.Color
import com.stripe.android.common.model.CommonConfiguration
import com.stripe.android.common.model.asCommonConfiguration
import com.stripe.android.paymentsheet.addresselement.AddressDetails
import org.junit.Test
import kotlin.test.assertFailsWith

class PaymentSheetConfigurationKtxTest {
    @Test
    fun `'validate' should fail when ephemeral key secret is blank`() {
        val configWithBlankEphemeralKeySecret = configuration.newBuilder()
            .customer(
                PaymentSheet.CustomerConfiguration(
                    id = "cus_1",
                    ephemeralKeySecret = "   "
                )
            )
            .build()
            .asCommonConfiguration()

        assertFailsWith(
            IllegalArgumentException::class,
            message = "When a CustomerConfiguration is passed to PaymentSheet, " +
                "the ephemeralKeySecret cannot be an empty string."
        ) {
            configWithBlankEphemeralKeySecret.validate()
        }
    }

    private fun getConfig(eKey: String): CommonConfiguration {
        return configuration.newBuilder()
            .customer(
                PaymentSheet.CustomerConfiguration(
                    id = "cus_1",
                    ephemeralKeySecret = eKey
                ),
            ).build().asCommonConfiguration()
    }

    @Test
    fun `'validate' should succeed when ephemeral key secret is of correct format`() {
        getConfig("ek_askljdlkasfhgasdfjls").validate()
        getConfig("ek_test_iiuwfhdaiuhasdvkcjn32n").validate()
    }

    @Test
    fun `'validate' should fail when ephemeral key secret is of wrong format`() {
        fun assertFailsWithEphemeralKeySecret(ephemeralKeySecret: String) {
            assertFailsWith(
                IllegalArgumentException::class,
                message = "`ephemeralKeySecret` format does not match expected client secret formatting"
            ) {
                getConfig(ephemeralKeySecret).validate()
            }
        }

        assertFailsWithEphemeralKeySecret("eph_askjdfhajkshdfjkashdjkfhsakjdhfkjashfd")
        assertFailsWithEphemeralKeySecret("eph_test_askjdfhajkshdfjkashdjkfhsakjdhfkjashfd")
        assertFailsWithEphemeralKeySecret("sk_askjdfhajkshdfjkashdjkfhsakjdhfkjashfd")
        assertFailsWithEphemeralKeySecret("ek_")
        assertFailsWithEphemeralKeySecret("ek")
        assertFailsWithEphemeralKeySecret("eeek_aldkfjalskdjflkasjbvdkjds")
    }

    @OptIn(ExperimentalCustomerSessionApi::class)
    @Test
    fun `'validate' should fail when customer client secret key is secret is blank`() {
        val configWithBlankCustomerSessionClientSecret = configuration.newBuilder()
            .customer(
                PaymentSheet.CustomerConfiguration.createWithCustomerSession(
                    id = "cus_1",
                    clientSecret = "   "
                ),
            ).build().asCommonConfiguration()

        assertFailsWith(
            IllegalArgumentException::class,
            message = "When a CustomerConfiguration is passed to PaymentSheet, " +
                "the customerSessionClientSecret cannot be an empty string."
        ) {
            configWithBlankCustomerSessionClientSecret.validate()
        }
    }

    @OptIn(ExperimentalCustomerSessionApi::class)
    @Test
    fun `'validate' should fail when provided argument has an ephemeral key secret format`() {
        val configWithEphemeralKeySecretAsCustomerSessionClientSecret = configuration.newBuilder()
            .customer(
                PaymentSheet.CustomerConfiguration.createWithCustomerSession(
                    id = "cus_1",
                    clientSecret = "ek_12345"
                ),
            ).build().asCommonConfiguration()

        assertFailsWith(
            IllegalArgumentException::class,
            message = "Argument looks like an Ephemeral Key secret, but expecting a CustomerSession client " +
                "secret. See CustomerSession API: https://docs.stripe.com/api/customer_sessions/create"
        ) {
            configWithEphemeralKeySecretAsCustomerSessionClientSecret.validate()
        }
    }

    @OptIn(ExperimentalCustomerSessionApi::class)
    @Test
    fun `'validate' should fail when provided argument is not a recognized customer session client secret format`() {
        val configWithInvalidCustomerSessionClientSecret = configuration.newBuilder()
            .customer(
                PaymentSheet.CustomerConfiguration.createWithCustomerSession(
                    id = "cus_1",
                    clientSecret = "total_12345"
                ),
            ).build().asCommonConfiguration()

        assertFailsWith(
            IllegalArgumentException::class,
            message = "Argument does not look like a CustomerSession client secret. " +
                "See CustomerSession API: https://docs.stripe.com/api/customer_sessions/create"
        ) {
            configWithInvalidCustomerSessionClientSecret.validate()
        }
    }

    private companion object {
        val configuration = PaymentSheet.Configuration(
            merchantDisplayName = "Merchant, Inc.",
            customer = PaymentSheet.CustomerConfiguration(
                id = "1",
                ephemeralKeySecret = "ek_123",
            ),
            googlePay = PaymentSheet.GooglePayConfiguration(
                environment = PaymentSheet.GooglePayConfiguration.Environment.Test,
                countryCode = "CA",
                currencyCode = "CAD",
                amount = 5099,
                label = "Merchant, Inc.",
                buttonType = PaymentSheet.GooglePayConfiguration.ButtonType.Checkout,
            ),
            primaryButtonColor = ColorStateList.valueOf(Color.BLUE),
            defaultBillingDetails = PaymentSheet.BillingDetails(
                name = "Jenny Rosen",
            ),
            shippingDetails = AddressDetails(
                name = "Jenny Rosen",
            ),
            primaryButtonLabel = "Buy",
            billingDetailsCollectionConfiguration = PaymentSheet.BillingDetailsCollectionConfiguration(
                name = PaymentSheet.BillingDetailsCollectionConfiguration.CollectionMode.Always,
            ),
        )
    }
}
