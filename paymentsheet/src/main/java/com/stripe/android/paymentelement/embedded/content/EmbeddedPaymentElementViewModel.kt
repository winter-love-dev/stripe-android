package com.stripe.android.paymentelement.embedded.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.stripe.android.core.injection.ViewModelScope
import com.stripe.android.core.utils.requireApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
internal class EmbeddedPaymentElementViewModel @Inject constructor(
    val embeddedPaymentElementSubcomponentFactory: EmbeddedPaymentElementSubcomponent.Factory,
    @ViewModelScope private val customViewModelScope: CoroutineScope,
) : ViewModel() {
    override fun onCleared() {
        customViewModelScope.cancel()
    }

    class Factory(
        private val paymentElementCallbackIdentifier: String,
        private val statusBarColor: Int?,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            val component = DaggerEmbeddedPaymentElementViewModelComponent.factory().build(
                savedStateHandle = extras.createSavedStateHandle(),
                application = extras.requireApplication(),
                paymentElementCallbackIdentifier = paymentElementCallbackIdentifier,
                statusBarColor = statusBarColor,
            )
            @Suppress("UNCHECKED_CAST")
            return component.viewModel as T
        }
    }
}
