package com.stripe.android.ui.core.elements

import androidx.annotation.RestrictTo
import com.stripe.android.core.strings.resolvableString
import com.stripe.android.uicore.elements.DropdownFieldController
import com.stripe.android.uicore.elements.IdentifierSpec
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@Serializable
@Parcelize
data class DropdownSpec(
    @SerialName("api_path")
    override val apiPath: IdentifierSpec,

    @SerialName("translation_id")
    val labelTranslationId: TranslationId,

    @SerialName("items")
    val items: List<DropdownItemSpec>
) : FormItemSpec() {
    fun transform(
        initialValues: Map<IdentifierSpec, String?> = mapOf()
    ) = createSectionElement(
        SimpleDropdownElement(
            this.apiPath,
            DropdownFieldController(
                SimpleDropdownConfig(
                    resolvableString(labelTranslationId.resourceId),
                    items
                ),
                initialValue = initialValues[apiPath]
            )
        )
    )
}
