package com.stripe.android.lpm

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.stripe.android.BasePlaygroundTest
import com.stripe.android.model.PaymentMethod
import com.stripe.android.paymentsheet.example.playground.settings.CheckoutMode
import com.stripe.android.paymentsheet.example.playground.settings.CheckoutModeSettingsDefinition
import com.stripe.android.paymentsheet.example.playground.settings.Country
import com.stripe.android.paymentsheet.example.playground.settings.CountrySettingsDefinition
import com.stripe.android.paymentsheet.example.playground.settings.Currency
import com.stripe.android.paymentsheet.example.playground.settings.CurrencySettingsDefinition
import com.stripe.android.paymentsheet.example.playground.settings.DelayedPaymentMethodsSettingsDefinition
import com.stripe.android.paymentsheet.example.playground.settings.SupportedPaymentMethodsSettingsDefinition
import com.stripe.android.test.core.AuthorizeAction
import com.stripe.android.test.core.TestParameters
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class TestBacs : BasePlaygroundTest() {
    @Test
    fun testBacsWhenConfirmed() {
        testDriver.confirmNewOrGuestComplete(
            testParameters = createTestParameters(AuthorizeAction.Bacs.Confirm),
            populateCustomLpmFields = {
                populateBacs()
            }
        )
    }

    @Test
    fun testBacsWhenConfirmedWithSfu() {
        testDriver.confirmNewOrGuestComplete(
            testParameters = createTestParameters(AuthorizeAction.Bacs.Confirm).copyPlaygroundSettings { settings ->
                settings[CheckoutModeSettingsDefinition] = CheckoutMode.PAYMENT_WITH_SETUP
            },
            populateCustomLpmFields = {
                populateBacs()
            }
        )
    }

    @Test
    fun testBacsWhenCancelled() {
        testDriver.confirmNewOrGuestComplete(
            testParameters = createTestParameters(AuthorizeAction.Bacs.ModifyDetails),
            populateCustomLpmFields = {
                populateBacs()
            }
        )
    }

    @Test
    fun testBacsWhenConfirmedInCustomFlow() {
        testDriver.confirmCustomAndBuy(
            testParameters = createTestParameters(AuthorizeAction.Bacs.Confirm),
            populateCustomLpmFields = {
                populateBacs()
            },
        )
    }

    @Test
    fun testBacsWhenCancelledInCustomFlow() {
        testDriver.confirmCustomAndBuy(
            testParameters = createTestParameters(AuthorizeAction.Bacs.ModifyDetails),
            populateCustomLpmFields = {
                populateBacs()
            },
        )
    }

    private fun createTestParameters(
        bacsAuthAction: AuthorizeAction.Bacs
    ): TestParameters {
        return TestParameters.create(
            paymentMethodCode = "bacs_debit",
            authorizationAction = bacsAuthAction,
            executeInNightlyRun = true,
        ) { settings ->
            settings[SupportedPaymentMethodsSettingsDefinition] = listOf(
                PaymentMethod.Type.Card,
                PaymentMethod.Type.BacsDebit
            ).joinToString(",")
            settings[DelayedPaymentMethodsSettingsDefinition] = true
            settings[CountrySettingsDefinition] = Country.GB
            settings[CurrencySettingsDefinition] = Currency.GBP
        }
    }
}
