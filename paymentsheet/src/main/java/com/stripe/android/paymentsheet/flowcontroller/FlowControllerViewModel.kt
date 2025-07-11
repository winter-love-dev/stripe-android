package com.stripe.android.paymentsheet.flowcontroller

import android.app.Application
import androidx.annotation.ColorInt
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.stripe.android.analytics.SessionSavedStateHandler
import com.stripe.android.core.utils.requireApplication
import com.stripe.android.paymentsheet.model.PaymentSelection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class FlowControllerViewModel(
    application: Application,
    val handle: SavedStateHandle,
    paymentElementCallbackIdentifier: String,
    @ColorInt statusBarColor: Int?,
) : AndroidViewModel(application) {

    val flowControllerStateComponent: FlowControllerStateComponent =
        DaggerFlowControllerStateComponent
            .builder()
            .application(application)
            .statusBarColor(statusBarColor)
            .paymentElementCallbackIdentifier(paymentElementCallbackIdentifier)
            .flowControllerViewModel(this)
            .build()

    var walletButtonsRendered: Boolean = false

    @Volatile
    var paymentSelection: PaymentSelection? = null

    // Used to determine if we need to reload the flow controller configuration.
    var previousConfigureRequest: FlowControllerConfigurationHandler.ConfigureRequest?
        get() = _configureRequest.value
        set(value) {
            _configureRequest.value = value
        }

    var state: DefaultFlowController.State?
        get() = handle[STATE_KEY]
        set(value) {
            handle[STATE_KEY] = value
        }

    fun updateState(block: (DefaultFlowController.State?) -> DefaultFlowController.State?) {
        handle[STATE_KEY] = block(state)
    }

    private val _configureRequest = MutableStateFlow<FlowControllerConfigurationHandler.ConfigureRequest?>(null)
    val configureRequest: StateFlow<FlowControllerConfigurationHandler.ConfigureRequest?> =
        _configureRequest.asStateFlow()

    val stateFlow = handle.getStateFlow<DefaultFlowController.State?>(STATE_KEY, null)
    private val restartSession = SessionSavedStateHandler.attachTo(this, handle)

    init {
        viewModelScope.launch {
            stateFlow.collectLatest { state ->
                flowControllerStateComponent.linkHandler.setupLink(
                    state = state?.paymentSheetState?.paymentMethodMetadata?.linkState
                )
            }
        }
    }

    fun resetSession() {
        restartSession()
    }

    class Factory(
        @ColorInt private val statusBarColor: Int?,
        private val paymentElementCallbackIdentifier: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return FlowControllerViewModel(
                application = extras.requireApplication(),
                handle = extras.createSavedStateHandle(),
                paymentElementCallbackIdentifier = paymentElementCallbackIdentifier,
                statusBarColor = statusBarColor,
            ) as T
        }
    }

    private companion object {
        private const val STATE_KEY = "state"
    }
}
