package com.stripe.android.ui.core.elements

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.stripe.android.core.strings.ResolvableString
import com.stripe.android.uicore.elements.IdentifierSpec
import com.stripe.android.uicore.elements.SectionElement
import com.stripe.android.uicore.elements.SectionFieldElement
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * This is used to define each section in the visual form layout specification
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Serializable(with = FormItemSpecSerializer::class)
sealed class FormItemSpec : Parcelable {
    @SerialName("api_path")
    abstract val apiPath: IdentifierSpec

    internal fun createSectionElement(
        sectionFieldElement: SectionFieldElement,
        label: ResolvableString? = null
    ): SectionElement = SectionElement.wrap(sectionFieldElement, label)

    internal fun createSectionElement(
        sectionFieldElements: List<SectionFieldElement>,
        label: ResolvableString? = null
    ): SectionElement = SectionElement.wrap(sectionFieldElements, label)
}

object FormItemSpecSerializer :
    JsonContentPolymorphicSerializer<FormItemSpec>(FormItemSpec::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<FormItemSpec> {
        return when (element.jsonObject["type"]?.jsonPrimitive?.content) {
            "billing_address" -> AddressSpec.serializer()
            "affirm_header" -> AffirmTextSpec.serializer()
            "afterpay_header" -> AfterpayClearpayTextSpec.serializer()
            "au_becs_bsb_number" -> BsbSpec.serializer()
            "au_becs_account_number" -> AuBankAccountNumberSpec.serializer()
            "au_becs_mandate" -> AuBecsDebitMandateTextSpec.serializer()
            "country" -> CountrySpec.serializer()
            "selector" -> DropdownSpec.serializer()
            "email" -> EmailSpec.serializer()
            "iban" -> IbanSpec.serializer()
            "klarna_country" -> CountrySpec.serializer()
            "klarna_header" -> KlarnaHeaderStaticTextSpec.serializer()
            "static_text" -> StaticTextSpec.serializer()
            "name" -> NameSpec.serializer()
            "mandate" -> MandateTextSpec.serializer()
            "sepa_mandate" -> SepaMandateTextSpec.serializer()
            "text" -> SimpleTextSpec.serializer()
            "placeholder" -> PlaceholderSpec.serializer()
            else -> EmptyFormSpec.serializer()
        }
    }
}
