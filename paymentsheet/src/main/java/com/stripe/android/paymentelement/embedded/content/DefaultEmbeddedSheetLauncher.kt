package com.stripe.android.paymentelement.embedded.content

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.stripe.android.lpmfoundations.paymentmethod.PaymentMethodMetadata
import com.stripe.android.paymentelement.EmbeddedPaymentElement
import com.stripe.android.paymentelement.callbacks.PaymentElementCallbackIdentifier
import com.stripe.android.paymentelement.embedded.EmbeddedResultCallbackHelper
import com.stripe.android.paymentelement.embedded.EmbeddedRowSelectionImmediateActionHandler
import com.stripe.android.paymentelement.embedded.EmbeddedSelectionHolder
import com.stripe.android.paymentelement.embedded.form.FormContract
import com.stripe.android.paymentelement.embedded.form.FormResult
import com.stripe.android.paymentelement.embedded.manage.ManageContract
import com.stripe.android.paymentelement.embedded.manage.ManageResult
import com.stripe.android.payments.core.analytics.ErrorReporter
import com.stripe.android.payments.core.injection.STATUS_BAR_COLOR
import com.stripe.android.paymentsheet.CustomerStateHolder
import com.stripe.android.paymentsheet.model.PaymentSelection
import com.stripe.android.paymentsheet.model.paymentMethodType
import com.stripe.android.paymentsheet.state.CustomerState
import javax.inject.Inject
import javax.inject.Named

internal interface EmbeddedSheetLauncher {
    fun launchForm(
        code: String,
        paymentMethodMetadata: PaymentMethodMetadata,
        hasSavedPaymentMethods: Boolean,
        embeddedConfirmationState: EmbeddedConfirmationStateHolder.State?
    )

    fun launchManage(
        paymentMethodMetadata: PaymentMethodMetadata,
        customerState: CustomerState,
        selection: PaymentSelection?,
    )
}

@EmbeddedPaymentElementScope
internal class DefaultEmbeddedSheetLauncher @Inject constructor(
    activityResultCaller: ActivityResultCaller,
    lifecycleOwner: LifecycleOwner,
    private val selectionHolder: EmbeddedSelectionHolder,
    private val rowSelectionImmediateActionHandler: EmbeddedRowSelectionImmediateActionHandler,
    private val customerStateHolder: CustomerStateHolder,
    private val sheetStateHolder: SheetStateHolder,
    private val errorReporter: ErrorReporter,
    @Named(STATUS_BAR_COLOR) private val statusBarColor: Int?,
    @PaymentElementCallbackIdentifier private val paymentElementCallbackIdentifier: String,
    embeddedResultCallbackHelper: EmbeddedResultCallbackHelper,
) : EmbeddedSheetLauncher {

    init {
        lifecycleOwner.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    formActivityLauncher.unregister()
                    manageActivityLauncher.unregister()
                    super.onDestroy(owner)
                }
            }
        )
    }

    private val formActivityLauncher: ActivityResultLauncher<FormContract.Args> =
        activityResultCaller.registerForActivityResult(FormContract) { result ->
            sheetStateHolder.sheetIsOpen = false
            selectionHolder.setTemporary(null)
            if (result is FormResult.Complete) {
                selectionHolder.set(result.selection)
                if (result.hasBeenConfirmed) {
                    embeddedResultCallbackHelper.setResult(
                        EmbeddedPaymentElement.Result.Completed()
                    )
                } else {
                    result.selection?.let { rowSelectionImmediateActionHandler.invoke() }
                }
            }
        }

    private val manageActivityLauncher: ActivityResultLauncher<ManageContract.Args> =
        activityResultCaller.registerForActivityResult(ManageContract) { result ->
            sheetStateHolder.sheetIsOpen = false
            when (result) {
                is ManageResult.Error -> Unit
                is ManageResult.Complete -> {
                    customerStateHolder.setCustomerState(result.customerState)
                    selectionHolder.set(result.selection)
                    if (result.shouldInvokeSelectionCallback && result.selection is PaymentSelection.Saved) {
                        rowSelectionImmediateActionHandler.invoke()
                    }
                }
            }
        }

    override fun launchForm(
        code: String,
        paymentMethodMetadata: PaymentMethodMetadata,
        hasSavedPaymentMethods: Boolean,
        embeddedConfirmationState: EmbeddedConfirmationStateHolder.State?
    ) {
        if (embeddedConfirmationState == null) {
            errorReporter.report(
                ErrorReporter.UnexpectedErrorEvent.EMBEDDED_SHEET_LAUNCHER_EMBEDDED_STATE_IS_NULL
            )
            return
        }
        if (sheetStateHolder.sheetIsOpen) return
        sheetStateHolder.sheetIsOpen = true
        selectionHolder.setTemporary(code)
        val currentSelection = (selectionHolder.selection.value as? PaymentSelection.New?)
            .takeIf { it?.paymentMethodType == code }
            ?: selectionHolder.getPreviousNewSelection(code)
        val args = FormContract.Args(
            selectedPaymentMethodCode = code,
            paymentMethodMetadata = paymentMethodMetadata,
            hasSavedPaymentMethods = hasSavedPaymentMethods,
            configuration = embeddedConfirmationState.configuration,
            initializationMode = embeddedConfirmationState.initializationMode,
            paymentElementCallbackIdentifier = paymentElementCallbackIdentifier,
            statusBarColor = statusBarColor,
            paymentSelection = currentSelection,
        )
        formActivityLauncher.launch(args)
    }

    override fun launchManage(
        paymentMethodMetadata: PaymentMethodMetadata,
        customerState: CustomerState,
        selection: PaymentSelection?,
    ) {
        if (sheetStateHolder.sheetIsOpen) return
        sheetStateHolder.sheetIsOpen = true
        val args = ManageContract.Args(
            paymentMethodMetadata = paymentMethodMetadata,
            customerState = customerState,
            selection = selection,
            paymentElementCallbackIdentifier = paymentElementCallbackIdentifier,
        )
        manageActivityLauncher.launch(args)
    }
}
