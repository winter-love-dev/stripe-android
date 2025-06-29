package com.stripe.android.ui.core.elements

import androidx.compose.ui.text.input.KeyboardType
import com.google.common.truth.Truth.assertThat
import com.stripe.android.core.strings.resolvableString
import com.stripe.android.uicore.elements.SimpleTextFieldConfig
import org.junit.Test

class SimpleTextFieldConfigTest {
    @Test
    fun `test number keyboards only accept numbers`() {
        val textConfig = SimpleTextFieldConfig(
            label = resolvableString("Phone"),
            keyboard = KeyboardType.Number
        )

        assertThat(textConfig.filter("abc123")).isEqualTo("123")
    }

    @Test
    fun `test number password keyboards only accept numbers`() {
        val textConfig = SimpleTextFieldConfig(
            label = resolvableString("Password"),
            keyboard = KeyboardType.NumberPassword
        )

        assertThat(textConfig.filter("abc123")).isEqualTo("123")
    }
}
