package com.stripe.android.paymentelement.embedded.content

import android.os.Bundle
import com.stripe.android.common.model.asCommonConfiguration
import com.stripe.android.core.injection.ViewModelScope
import com.stripe.android.paymentelement.EmbeddedPaymentElement
import com.stripe.android.paymentelement.EmbeddedPaymentElement.ConfigureResult
import com.stripe.android.paymentelement.embedded.EmbeddedSelectionHolder
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.state.PaymentElementLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import javax.inject.Inject
import javax.inject.Singleton

internal interface EmbeddedConfigurationCoordinator {
    suspend fun configure(
        intentConfiguration: PaymentSheet.IntentConfiguration,
        configuration: EmbeddedPaymentElement.Configuration,
    ): ConfigureResult
}

@Singleton
internal class DefaultEmbeddedConfigurationCoordinator @Inject constructor(
    private val confirmationStateHolder: EmbeddedConfirmationStateHolder,
    private val configurationHandler: EmbeddedConfigurationHandler,
    private val selectionHolder: EmbeddedSelectionHolder,
    private val selectionChooser: EmbeddedSelectionChooser,
    private val stateHelper: EmbeddedStateHelper,
    @ViewModelScope private val viewModelScope: CoroutineScope,
) : EmbeddedConfigurationCoordinator {
    override suspend fun configure(
        intentConfiguration: PaymentSheet.IntentConfiguration,
        configuration: EmbeddedPaymentElement.Configuration
    ): ConfigureResult {
        return viewModelScope.async {
            confirmationStateHolder.state = null
            configurationHandler.configure(
                intentConfiguration = intentConfiguration,
                configuration = configuration,
            ).fold(
                onSuccess = { state ->
                    handleLoadedState(
                        state = state,
                        intentConfiguration = intentConfiguration,
                        configuration = configuration,
                    )
                    ConfigureResult.Succeeded()
                },
                onFailure = { error ->
                    ConfigureResult.Failed(error)
                },
            )
        }.await()
    }

    private fun handleLoadedState(
        state: PaymentElementLoader.State,
        intentConfiguration: PaymentSheet.IntentConfiguration,
        configuration: EmbeddedPaymentElement.Configuration,
    ) {
        val newPaymentSelection = selectionChooser.choose(
            paymentMethodMetadata = state.paymentMethodMetadata,
            paymentMethods = state.customer?.paymentMethods,
            previousSelection = selectionHolder.selection.value,
            newSelection = state.paymentSelection,
            newConfiguration = configuration.asCommonConfiguration(),
            formSheetAction = configuration.formSheetAction,
        )
        stateHelper.state = EmbeddedPaymentElement.State(
            confirmationState = EmbeddedConfirmationStateHolder.State(
                paymentMethodMetadata = state.paymentMethodMetadata,
                selection = newPaymentSelection,
                initializationMode = PaymentElementLoader.InitializationMode.DeferredIntent(
                    intentConfiguration
                ),
                configuration = configuration,
            ),
            customer = state.customer,
            previousNewSelections = Bundle(),
        )
    }
}
