package com.stripe.android.ui.core.elements

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.stripe.android.uicore.elements.CheckboxElementUI
import com.stripe.android.uicore.strings.resolve
import com.stripe.android.uicore.utils.collectAsState

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
const val SET_AS_DEFAULT_PAYMENT_METHOD_TEST_TAG = "SET_AS_DEFAULT_PAYMENT_METHOD_TEST_TAG"

@Composable
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun SetAsDefaultPaymentMethodElementUI(
    enabled: Boolean,
    element: SetAsDefaultPaymentMethodElement,
    modifier: Modifier = Modifier,
) {
    val controller = element.controller
    val checked by controller.setAsDefaultPaymentMethodChecked.collectAsState()
    val label by controller.label.collectAsState()

    val shouldShow by element.shouldShowElementFlow.collectAsState()

    if (shouldShow) {
        CheckboxElementUI(
            automationTestTag = SET_AS_DEFAULT_PAYMENT_METHOD_TEST_TAG,
            isChecked = checked,
            label = label.resolve(),
            isEnabled = enabled,
            modifier = modifier,
            onValueChange = {
                controller.onValueChange(!checked)
            },
        )
    }
}
