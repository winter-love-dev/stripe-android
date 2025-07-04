package com.stripe.android.ui.core.elements

import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.google.common.truth.Truth
import com.stripe.android.core.R
import com.stripe.android.core.strings.resolvableString
import com.stripe.android.uicore.elements.PostalCodeConfig
import com.stripe.android.uicore.elements.TextFieldState
import org.junit.Test

class PostalCodeConfigTest {
    @Test
    fun `verify US config uses proper keyboard capitalization & keyboard type`() {
        with(createConfigForCountry("US")) {
            Truth.assertThat(capitalization).isEqualTo(KeyboardCapitalization.None)
            Truth.assertThat(keyboard).isEqualTo(KeyboardType.NumberPassword)
        }
    }

    @Test
    fun `verify CA config uses proper keyboard capitalization & keyboard type`() {
        with(createConfigForCountry("CA")) {
            Truth.assertThat(capitalization).isEqualTo(KeyboardCapitalization.Characters)
            Truth.assertThat(keyboard).isEqualTo(KeyboardType.Text)
        }
    }

    @Test
    fun `verify GB config uses proper keyboard capitalization & keyboard type`() {
        with(createConfigForCountry("GB")) {
            Truth.assertThat(capitalization).isEqualTo(KeyboardCapitalization.Characters)
            Truth.assertThat(keyboard).isEqualTo(KeyboardType.Text)
        }
    }

    @Test
    fun `verify Other config uses proper keyboard capitalization & keyboard type`() {
        with(createConfigForCountry("IN")) {
            Truth.assertThat(capitalization).isEqualTo(KeyboardCapitalization.Characters)
            Truth.assertThat(keyboard).isEqualTo(KeyboardType.Text)
        }
    }

    @Test
    fun `verify US postal codes`() {
        with(createConfigForCountry("US")) {
            Truth.assertThat(determineStateForInput("").isValid()).isFalse()
            Truth.assertThat(determineStateForInput("").isFull()).isFalse()
            Truth.assertThat(determineStateForInput("12345").isValid()).isTrue()
            Truth.assertThat(determineStateForInput("12345").isFull()).isTrue()
            Truth.assertThat(determineStateForInput("abcde").isValid()).isFalse()
            Truth.assertThat(determineStateForInput("abcde").isFull()).isFalse()
        }
    }

    @Test
    fun `verify CA postal codes`() {
        with(createConfigForCountry("CA")) {
            Truth.assertThat(determineStateForInput("").isValid()).isFalse()
            Truth.assertThat(determineStateForInput("").isFull()).isFalse()
            Truth.assertThat(determineStateForInput("AAA AAA").isValid()).isFalse()
            Truth.assertThat(determineStateForInput("AAAAAA").isValid()).isFalse()
            Truth.assertThat(determineStateForInput("A0A 0A0").isValid()).isTrue()
            Truth.assertThat(determineStateForInput("A0A 0A0").isFull()).isTrue()
            Truth.assertThat(determineStateForInput("A0A0A0").isValid()).isTrue()
            Truth.assertThat(determineStateForInput("A0A0A0").isFull()).isTrue()
        }
    }

    @Test
    fun `verify GB postal codes`() {
        with(createConfigForCountry("GB")) {
            Truth.assertThat(determineStateForInput("").isValid()).isFalse()
            Truth.assertThat(determineStateForInput("").isFull()).isFalse()
            Truth.assertThat(determineStateForInput("1M1AA").isValid()).isFalse()
            Truth.assertThat(determineStateForInput("1M 1AA").isValid()).isFalse()
            Truth.assertThat(determineStateForInput("M11AA").isValid()).isTrue()
            Truth.assertThat(determineStateForInput("B2 3DF").isValid()).isTrue()
            Truth.assertThat(determineStateForInput("CR26XH").isValid()).isTrue()
            Truth.assertThat(determineStateForInput("M60 1NW").isValid()).isTrue()
            Truth.assertThat(determineStateForInput("DN551PT").isValid()).isTrue()
            Truth.assertThat(determineStateForInput("EC1A 1BB").isValid()).isTrue()
            Truth.assertThat(determineStateForInput("EC1A 1BB").isFull()).isTrue()
        }
    }

    @Test
    fun `verify other postal codes`() {
        with(createConfigForCountry("IN")) {
            Truth.assertThat(determineStateForInput("").isValid()).isFalse()
            Truth.assertThat(determineStateForInput("").isFull()).isFalse()
            Truth.assertThat(determineStateForInput(" ").isValid()).isFalse()
            Truth.assertThat(determineStateForInput(" ").isFull()).isFalse()
            Truth.assertThat(determineStateForInput("a").isValid()).isTrue()
            Truth.assertThat(determineStateForInput("a").isFull()).isFalse()
            Truth.assertThat(determineStateForInput("1").isValid()).isTrue()
            Truth.assertThat(determineStateForInput("1").isFull()).isFalse()
            Truth.assertThat(determineStateForInput("aaaaaa").isValid()).isTrue()
            Truth.assertThat(determineStateForInput("111111").isValid()).isTrue()
        }
    }

    @Test
    fun `invalid US postal codes emit error`() {
        with(createConfigForCountry("US")) {
            Truth.assertThat(determineStateForInput("").getError()).isNull()
            Truth.assertThat(determineStateForInput("1234").getError()).isNotNull()
            Truth.assertThat(determineStateForInput("12345").getError()).isNull()
        }
    }

    @Test
    fun `invalid CA postal codes emit error`() {
        with(createConfigForCountry("CA")) {
            Truth.assertThat(determineStateForInput("").getError()).isNull()
            Truth.assertThat(determineStateForInput("1N8E8R").getError()).isNotNull()
            Truth.assertThat(determineStateForInput("141124").getError()).isNotNull()
        }
    }

    @Test
    fun `invalid GB postal codes emit error`() {
        with(createConfigForCountry("GB")) {
            Truth.assertThat(determineStateForInput("").getError()).isNull()
            Truth.assertThat(determineStateForInput("N18E").getError()).isNotNull()
            Truth.assertThat(determineStateForInput("4C1A 1BB").getError()).isNotNull()
            Truth.assertThat(determineStateForInput("12345").getError()).isNotNull()
            Truth.assertThat(determineStateForInput("141124").getError()).isNotNull()
        }
    }

    private fun createConfigForCountry(country: String): PostalCodeConfig {
        return PostalCodeConfig(
            label = resolvableString(R.string.stripe_address_label_postal_code),
            country = country
        )
    }

    private fun PostalCodeConfig.determineStateForInput(input: String): TextFieldState {
        return determineState(filter(input))
    }
}
