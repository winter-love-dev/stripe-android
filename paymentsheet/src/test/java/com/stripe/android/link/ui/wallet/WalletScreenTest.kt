package com.stripe.android.link.ui.wallet

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.stripe.android.DefaultCardBrandFilter
import com.stripe.android.core.strings.resolvableString
import com.stripe.android.link.LinkDismissalCoordinator
import com.stripe.android.link.LinkLaunchMode
import com.stripe.android.link.RealLinkDismissalCoordinator
import com.stripe.android.link.TestFactory
import com.stripe.android.link.account.FakeLinkAccountManager
import com.stripe.android.link.account.LinkAccountManager
import com.stripe.android.link.confirmation.DefaultCompleteLinkFlow
import com.stripe.android.link.confirmation.FakeLinkConfirmationHandler
import com.stripe.android.link.confirmation.LinkConfirmationHandler
import com.stripe.android.link.theme.DefaultLinkTheme
import com.stripe.android.link.ui.BottomSheetContent
import com.stripe.android.link.ui.PrimaryButtonState
import com.stripe.android.link.ui.PrimaryButtonTag
import com.stripe.android.link.utils.TestNavigationManager
import com.stripe.android.model.CardBrand
import com.stripe.android.model.ConsumerPaymentDetails
import com.stripe.android.model.ConsumerPaymentDetailsUpdateParams
import com.stripe.android.model.CvcCheck
import com.stripe.android.payments.financialconnections.FinancialConnectionsAvailability
import com.stripe.android.testing.CoroutineTestRule
import com.stripe.android.testing.FakeLogger
import com.stripe.android.ui.core.elements.CvcController
import com.stripe.android.uicore.elements.DateConfig
import com.stripe.android.uicore.elements.SimpleTextFieldController
import com.stripe.android.uicore.utils.stateFlowOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds
import com.stripe.android.link.confirmation.Result as LinkConfirmationResult

@RunWith(AndroidJUnit4::class)
internal class WalletScreenTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule(dispatcher)

    @Test
    fun `wallet list is collapsed on start`() = runTest(dispatcher) {
        val linkAccountManager = FakeLinkAccountManager()
        linkAccountManager.listPaymentDetailsResult = Result.success(
            ConsumerPaymentDetails(
                paymentDetails = listOf(
                    TestFactory.CONSUMER_PAYMENT_DETAILS_CARD,
                    TestFactory.CONSUMER_PAYMENT_DETAILS_BANK_ACCOUNT,
                )
            )
        )
        val viewModel = createViewModel(linkAccountManager)
        composeTestRule.setContent {
            DefaultLinkTheme {
                WalletScreen(
                    viewModel = viewModel,
                    showBottomSheetContent = {},
                    hideBottomSheetContent = {},
                    onLogoutClicked = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        onWalletCollapsedHeader().assertIsDisplayed()
        onWalletCollapsedChevron().assertIsDisplayed()
        onWalletCollapsedPaymentDetails().assertIsDisplayed()
        onCollapsedWalletRow().assertIsDisplayed().assertHasClickAction()
        onWalletPayButton().assertIsDisplayed().assertIsEnabled().assertHasClickAction()
        onWalletPayAnotherWayButton().assertIsDisplayed().assertIsEnabled().assertHasClickAction()
    }

    @Test
    fun `wallet list is collapsed and pay button is disabled for expired card`() = runTest(dispatcher) {
        val linkAccountManager = FakeLinkAccountManager()
        linkAccountManager.listPaymentDetailsResult = Result.success(
            ConsumerPaymentDetails(
                paymentDetails = listOf(
                    TestFactory.CONSUMER_PAYMENT_DETAILS_CARD.copy(
                        expiryYear = 1999
                    ),
                    TestFactory.CONSUMER_PAYMENT_DETAILS_BANK_ACCOUNT,
                )
            )
        )
        val viewModel = createViewModel(linkAccountManager)
        composeTestRule.setContent {
            DefaultLinkTheme {
                WalletScreen(
                    viewModel = viewModel,
                    showBottomSheetContent = {},
                    hideBottomSheetContent = {},
                    onLogoutClicked = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        onWalletCollapsedHeader().assertIsDisplayed()
        onWalletCollapsedChevron().assertIsDisplayed()
        onWalletCollapsedPaymentDetails().assertIsDisplayed()
        onCollapsedWalletRow().assertIsDisplayed().assertHasClickAction()

        onWalletPayButton()
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsNotEnabled()
            .assertHasClickAction()

        onWalletPayAnotherWayButton()
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
    }

    @Test
    fun `wallet list is expanded on expand clicked`() = runTest(dispatcher) {
        val linkAccountManager = FakeLinkAccountManager()
        linkAccountManager.listPaymentDetailsResult = Result.success(
            ConsumerPaymentDetails(
                paymentDetails = listOf(
                    TestFactory.CONSUMER_PAYMENT_DETAILS_CARD,
                    TestFactory.CONSUMER_PAYMENT_DETAILS_BANK_ACCOUNT
                )
            )
        )
        val viewModel = createViewModel(linkAccountManager)
        composeTestRule.setContent {
            DefaultLinkTheme {
                WalletScreen(
                    viewModel = viewModel,
                    showBottomSheetContent = {},
                    hideBottomSheetContent = {},
                    onLogoutClicked = {},
                )
            }
        }

        composeTestRule.waitForIdle()

        onCollapsedWalletRow().performClick()

        composeTestRule.waitForIdle()

        onWalletAddPaymentMethodRow().assertIsDisplayed().assertHasClickAction()
        onExpandedWalletHeader().assertIsDisplayed()
        onPaymentMethodList().assertCountEquals(2)

        onWalletPayButton()
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()

        onWalletPayAnotherWayButton()
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
    }

    @Test
    fun `add payment method menu is shown when multiple options are available`() = runTest(dispatcher) {
        composeTestRule.setContent {
            DefaultLinkTheme {
                var bottomSheetContent: BottomSheetContent? by remember { mutableStateOf(null) }
                Box(modifier = Modifier.fillMaxSize()) {
                    if (bottomSheetContent == null) {
                        TestWalletBody(
                            addPaymentMethodOptions = listOf(
                                AddPaymentMethodOption.Card,
                                AddPaymentMethodOption.Bank(FinancialConnectionsAvailability.Full),
                            ),
                            showBottomSheetContent = { bottomSheetContent = it },
                            hideBottomSheetContent = { bottomSheetContent = null },
                        )
                    } else {
                        Column {
                            bottomSheetContent?.invoke(this)
                        }
                    }
                }
            }
        }
        onWalletAddPaymentMethodRow().performClick()
        composeTestRule.waitForIdle()
        onWalletAddPaymentMethodMenu().assertIsDisplayed()
    }

    @Test
    fun `single payment method option is selected when only one available`() = runTest(dispatcher) {
        var selectedAddPaymentMethodOptionClicked: AddPaymentMethodOption? = null
        composeTestRule.setContent {
            DefaultLinkTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    TestWalletBody(
                        addPaymentMethodOptions = listOf(AddPaymentMethodOption.Card),
                        onAddPaymentMethodOptionClicked = { selectedAddPaymentMethodOptionClicked = it },
                        showBottomSheetContent = {},
                        hideBottomSheetContent = {},
                    )
                }
            }
        }
        onWalletAddPaymentMethodRow().performClick()
        composeTestRule.waitForIdle()
        assertThat(selectedAddPaymentMethodOptionClicked).isEqualTo(AddPaymentMethodOption.Card)
    }

    @Test
    fun `wallet list is expanded and pay button should be disabled for expired card`() = runTest(dispatcher) {
        val linkAccountManager = FakeLinkAccountManager()
        linkAccountManager.listPaymentDetailsResult = Result.success(
            ConsumerPaymentDetails(
                paymentDetails = listOf(
                    TestFactory.CONSUMER_PAYMENT_DETAILS_CARD.copy(
                        expiryYear = 1999
                    ),
                    TestFactory.CONSUMER_PAYMENT_DETAILS_BANK_ACCOUNT
                )
            )
        )
        val viewModel = createViewModel(linkAccountManager)
        composeTestRule.setContent {
            DefaultLinkTheme {
                WalletScreen(
                    viewModel = viewModel,
                    showBottomSheetContent = {},
                    hideBottomSheetContent = {},
                    onLogoutClicked = {},
                )
            }
        }

        composeTestRule.waitForIdle()

        onWalletPayButton()
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsNotEnabled()
            .assertHasClickAction()

        onWalletPayAnotherWayButton()
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
    }

    @Test
    fun `wallet loader should be displayed when no payment method is available`() = runTest(dispatcher) {
        val linkAccountManager = FakeLinkAccountManager()
        linkAccountManager.listPaymentDetailsResult = Result.success(ConsumerPaymentDetails(emptyList()))

        val viewModel = createViewModel(linkAccountManager)
        composeTestRule.setContent {
            DefaultLinkTheme {
                WalletScreen(
                    viewModel = viewModel,
                    showBottomSheetContent = {},
                    hideBottomSheetContent = {},
                    onLogoutClicked = {},
                )
            }
        }

        composeTestRule.waitForIdle()

        onLoader().assertIsDisplayed()
        onPaymentMethodList().assertCountEquals(0)
    }

    @Test
    fun `recollection form is displayed for expired card`() = runTest(dispatcher) {
        val expiredCard = TestFactory.CONSUMER_PAYMENT_DETAILS_CARD.copy(expiryYear = 1999)
        val linkAccountManager = FakeLinkAccountManager()
        linkAccountManager.listPaymentDetailsResult = Result.success(
            ConsumerPaymentDetails(paymentDetails = listOf(expiredCard))
        )
        val viewModel = createViewModel(linkAccountManager)
        composeTestRule.setContent {
            DefaultLinkTheme {
                WalletScreen(
                    viewModel = viewModel,
                    showBottomSheetContent = {},
                    hideBottomSheetContent = {},
                    onLogoutClicked = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        onWalletPayButton().assertIsNotEnabled()
        onWalletFormError().assertIsDisplayed()
        onWalletFormFields().assertIsDisplayed()
        onWalletPayButton().assertIsNotEnabled()
    }

    @Test
    fun `recollection form is displayed for card requiring CVC`() = runTest(dispatcher) {
        val cardRequiringCvc = TestFactory.CONSUMER_PAYMENT_DETAILS_CARD.copy(
            cvcCheck = CvcCheck.Unchecked
        )
        val linkAccountManager = FakeLinkAccountManager()
        linkAccountManager.listPaymentDetailsResult = Result.success(
            ConsumerPaymentDetails(paymentDetails = listOf(cardRequiringCvc))
        )
        val viewModel = createViewModel(linkAccountManager)
        composeTestRule.setContent {
            DefaultLinkTheme {
                WalletScreen(
                    viewModel = viewModel,
                    showBottomSheetContent = {},
                    hideBottomSheetContent = {},
                    onLogoutClicked = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        onWalletPayButton().assertIsNotEnabled()
        onWalletFormError().assertIsDisplayed()
        onWalletFormFields().assertIsDisplayed()
        onWalletPayButton().assertIsNotEnabled()
    }

    @Test
    fun `pay button is enabled after filling recollection form for expired card`() = runTest(dispatcher) {
        val expiredCard = TestFactory.CONSUMER_PAYMENT_DETAILS_CARD.copy(expiryYear = 1999)
        val linkAccountManager = FakeLinkAccountManager()
        linkAccountManager.listPaymentDetailsResult = Result.success(
            ConsumerPaymentDetails(paymentDetails = listOf(expiredCard))
        )
        val viewModel = createViewModel(linkAccountManager)
        composeTestRule.setContent {
            DefaultLinkTheme {
                WalletScreen(
                    viewModel = viewModel,
                    showBottomSheetContent = {},
                    hideBottomSheetContent = {},
                    onLogoutClicked = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        onWalletPayButton().assertIsNotEnabled()

        viewModel.expiryDateController.onRawValueChange("1225")
        viewModel.cvcController.onRawValueChange("123")

        composeTestRule.waitForIdle()

        onWalletPayButton().assertIsEnabled()
        onWalletDialogTag().assertDoesNotExist()
    }

    @Test
    fun `alert is displayed after expiry update failure`() = runTest(dispatcher) {
        val error = Throwable("oops")
        val expiredCard = TestFactory.CONSUMER_PAYMENT_DETAILS_CARD.copy(expiryYear = 1999)
        val linkAccountManager = FakeLinkAccountManager()
        linkAccountManager.listPaymentDetailsResult = Result.success(
            ConsumerPaymentDetails(paymentDetails = listOf(expiredCard))
        )
        linkAccountManager.updatePaymentDetailsResult = Result.failure(error)
        val viewModel = createViewModel(linkAccountManager)
        composeTestRule.setContent {
            DefaultLinkTheme {
                WalletScreen(
                    viewModel = viewModel,
                    showBottomSheetContent = {},
                    hideBottomSheetContent = {},
                    onLogoutClicked = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        onWalletPayButton().assertIsNotEnabled()

        viewModel.expiryDateController.onRawValueChange("1225")
        viewModel.cvcController.onRawValueChange("123")

        composeTestRule.waitForIdle()

        onWalletPayButton()
            .performScrollTo()
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitForIdle()

        onWalletDialogTag()
            .assertIsDisplayed()

        onWalletDialogButtonTag()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        onWalletDialogTag().assertDoesNotExist()
    }

    @Test
    fun `pay button is enabled after filling CVC for card requiring CVC`() = runTest(dispatcher) {
        val cardRequiringCvc = TestFactory.CONSUMER_PAYMENT_DETAILS_CARD.copy(
            cvcCheck = CvcCheck.Unchecked
        )
        val linkAccountManager = FakeLinkAccountManager()
        linkAccountManager.listPaymentDetailsResult = Result.success(
            ConsumerPaymentDetails(paymentDetails = listOf(cardRequiringCvc))
        )
        val viewModel = createViewModel(linkAccountManager)
        composeTestRule.setContent {
            DefaultLinkTheme {
                WalletScreen(
                    viewModel = viewModel,
                    showBottomSheetContent = {},
                    hideBottomSheetContent = {},
                    onLogoutClicked = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        onWalletPayButton().assertIsNotEnabled()

        viewModel.cvcController.onRawValueChange("123")

        composeTestRule.waitForIdle()

        onWalletPayButton().assertIsEnabled()
    }

    @Test
    fun `pay button state switches to processing state during payment`() = runTest(dispatcher) {
        val cardRequiringCvc = TestFactory.CONSUMER_PAYMENT_DETAILS_CARD.copy(
            expiryYear = 2999,
            cvcCheck = CvcCheck.Pass
        )
        val linkAccountManager = FakeLinkAccountManager()
        linkAccountManager.listPaymentDetailsResult = Result.success(
            ConsumerPaymentDetails(paymentDetails = listOf(cardRequiringCvc))
        )
        val linkConfirmationHandler = FakeLinkConfirmationHandler()
        linkConfirmationHandler.confirmResult = com.stripe.android.link.confirmation.Result.Succeeded

        val viewModel = createViewModel(
            linkAccountManager = linkAccountManager,
            linkConfirmationHandler = linkConfirmationHandler,
            navigationManager = TestNavigationManager()
        )
        composeTestRule.setContent {
            DefaultLinkTheme {
                WalletScreen(
                    viewModel = viewModel,
                    showBottomSheetContent = {},
                    hideBottomSheetContent = {},
                    onLogoutClicked = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        onWalletPayButton().assertIsEnabled()

        onWalletPayButton().performClick()

        composeTestRule.waitForIdle()

        onWalletErrorTag().assertDoesNotExist()

        assertThat(viewModel.uiState.value.primaryButtonState).isEqualTo(PrimaryButtonState.Processing)
    }

    @Test
    fun `error message is displayed when payment confirmation fails`() = runTest(dispatcher) {
        val cardRequiringCvc = TestFactory.CONSUMER_PAYMENT_DETAILS_CARD.copy(
            expiryYear = 2999,
            cvcCheck = CvcCheck.Pass
        )
        val linkAccountManager = FakeLinkAccountManager()
        linkAccountManager.listPaymentDetailsResult = Result.success(
            ConsumerPaymentDetails(paymentDetails = listOf(cardRequiringCvc))
        )
        val linkConfirmationHandler = FakeLinkConfirmationHandler()
        linkConfirmationHandler.confirmResult = LinkConfirmationResult.Failed("oops".resolvableString)

        val viewModel = createViewModel(
            linkAccountManager = linkAccountManager,
            linkConfirmationHandler = linkConfirmationHandler,
            navigationManager = TestNavigationManager()
        )
        composeTestRule.setContent {
            DefaultLinkTheme {
                WalletScreen(
                    viewModel = viewModel,
                    showBottomSheetContent = {},
                    hideBottomSheetContent = {},
                    onLogoutClicked = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        onWalletPayButton().assertIsEnabled()

        onWalletPayButton().performClick()

        composeTestRule.waitForIdle()

        onWalletErrorTag().assertIsDisplayed()

        assertThat(viewModel.uiState.value.primaryButtonState).isEqualTo(PrimaryButtonState.Enabled)
    }

    @Test
    fun `recollection form is not displayed for valid card`() = runTest(dispatcher) {
        val validCard = TestFactory.CONSUMER_PAYMENT_DETAILS_CARD.copy(
            expiryYear = 2099,
            cvcCheck = CvcCheck.Pass
        )
        val linkAccountManager = FakeLinkAccountManager()
        linkAccountManager.listPaymentDetailsResult = Result.success(
            ConsumerPaymentDetails(paymentDetails = listOf(validCard))
        )
        val viewModel = createViewModel(linkAccountManager)
        composeTestRule.setContent {
            DefaultLinkTheme {
                WalletScreen(
                    viewModel = viewModel,
                    showBottomSheetContent = {},
                    hideBottomSheetContent = {},
                    onLogoutClicked = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        onWalletFormError().assertDoesNotExist()
        onWalletFormFields().assertDoesNotExist()
        onWalletPayButton().assertIsEnabled()
    }

    @Test
    fun `wallet menu is displayed on payment method menu clicked`() = runTest(dispatcher) {
        val linkAccountManager = FakeLinkAccountManager()
        linkAccountManager.listPaymentDetailsResult = Result.success(TestFactory.CONSUMER_PAYMENT_DETAILS)
        val viewModel = createViewModel(
            linkAccountManager = linkAccountManager,
            navigationManager = TestNavigationManager()
        )

        composeTestRule.setContent {
            DefaultLinkTheme {
                var sheetContent by remember { mutableStateOf<BottomSheetContent?>(null) }
                Box {
                    WalletScreen(
                        viewModel = viewModel,
                        showBottomSheetContent = {
                            sheetContent = it
                        },
                        hideBottomSheetContent = {
                            sheetContent = null
                        },
                        onLogoutClicked = {},
                    )

                    sheetContent?.let {
                        Column { it() }
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        onCollapsedWalletRow().performClick()

        composeTestRule.waitForIdle()

        onWalletPaymentMethodMenu().assertDoesNotExist()
        onWalletPaymentMethodRowMenuButton().onFirst().performClick()

        composeTestRule.waitForIdle()
        onWalletPaymentMethodMenu().assertIsDisplayed()
    }

    @Test
    fun `pay method row is loading when card is being updated`() = runTest(dispatcher) {
        val linkAccountManager = object : FakeLinkAccountManager() {
            override suspend fun updatePaymentDetails(
                updateParams: ConsumerPaymentDetailsUpdateParams
            ): Result<ConsumerPaymentDetails> {
                delay(1.seconds)
                return super.updatePaymentDetails(updateParams)
            }
        }
        val card1 = TestFactory.CONSUMER_PAYMENT_DETAILS_CARD.copy(id = "card1", isDefault = false)
        val card2 = TestFactory.CONSUMER_PAYMENT_DETAILS_CARD.copy(id = "card2", isDefault = true)
        linkAccountManager.listPaymentDetailsResult = Result.success(
            ConsumerPaymentDetails(paymentDetails = listOf(card1, card2))
        )

        val viewModel = createViewModel(linkAccountManager)
        composeTestRule.setContent {
            DefaultLinkTheme {
                WalletScreen(
                    viewModel = viewModel,
                    showBottomSheetContent = {},
                    hideBottomSheetContent = {},
                    onLogoutClicked = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        onCollapsedWalletRow()
            .performClick()
        composeTestRule.waitForIdle()

        viewModel.onSetDefaultClicked(card1)

        composeTestRule.waitForIdle()

        onWalletPaymentMethodRowLoadingIndicator().assertIsDisplayed()
        onWalletPayButton().assertIsNotEnabled()

        dispatcher.scheduler.advanceTimeBy(1.1.seconds)

        onWalletPaymentMethodRowLoadingIndicator().assertDoesNotExist()
        onWalletPayButton()
            .assertExists()
            .assertIsEnabled()
    }

    @Test
    fun `wallet menu is dismissed on remove clicked`() = runTest(dispatcher) {
        testMenu(
            nodeTag = onWalletPaymentMethodMenuRemoveTag(),
            expectedRemovedCounter = 1
        )
    }

    @Test
    fun `wallet menu is dismissed on setAsDefault clicked`() = runTest(dispatcher) {
        testMenu(
            nodeTag = onWalletPaymentMethodMenuSetAsDefaultTag(),
            expectedSetAsDefaultCounter = 1
        )
    }

    private fun testMenu(
        nodeTag: SemanticsNodeInteraction,
        expectedRemovedCounter: Int = 0,
        expectedSetAsDefaultCounter: Int = 0,
    ) {
        var onSetDefaultCounter = 0
        var onRemoveClickedCounter = 0
        composeTestRule.setContent {
            DefaultLinkTheme {
                var sheetContent by remember { mutableStateOf<BottomSheetContent?>(null) }
                Box {
                    TestWalletBody(
                        onSetDefaultClicked = {
                            onSetDefaultCounter += 1
                        },
                        onRemoveClicked = {
                            onRemoveClickedCounter += 1
                        },
                        showBottomSheetContent = {
                            sheetContent = it
                        },
                        hideBottomSheetContent = {
                            sheetContent = null
                        }
                    )

                    sheetContent?.let {
                        Column { it() }
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        onWalletPaymentMethodRowMenuButton().onLast().performClick()

        composeTestRule.waitForIdle()

        onWalletPaymentMethodMenu().assertIsDisplayed()

        nodeTag.performClick()

        composeTestRule.waitForIdle()

        onWalletPaymentMethodMenu().assertDoesNotExist()
        assertThat(onSetDefaultCounter).isEqualTo(expectedSetAsDefaultCounter)
        assertThat(onRemoveClickedCounter).isEqualTo(expectedRemovedCounter)
    }

    @Composable
    private fun TestWalletBody(
        addPaymentMethodOptions: List<AddPaymentMethodOption> = listOf(AddPaymentMethodOption.Card),
        onRemoveClicked: (ConsumerPaymentDetails.PaymentDetails) -> Unit = {},
        onSetDefaultClicked: (ConsumerPaymentDetails.PaymentDetails) -> Unit = {},
        onAddPaymentMethodOptionClicked: (AddPaymentMethodOption) -> Unit = {},
        showBottomSheetContent: (BottomSheetContent?) -> Unit,
        hideBottomSheetContent: () -> Unit,
    ) {
        val paymentDetails = TestFactory.CONSUMER_PAYMENT_DETAILS.paymentDetails
            .filterIsInstance<ConsumerPaymentDetails.Card>()
            .map { it.copy(isDefault = false) }
        WalletBody(
            state = WalletUiState(
                paymentDetailsList = paymentDetails,
                email = "email@email.com",
                selectedItemId = paymentDetails.firstOrNull()?.id,
                cardBrandFilter = DefaultCardBrandFilter,
                isProcessing = false,
                hasCompleted = false,
                primaryButtonLabel = "Buy".resolvableString,
                secondaryButtonLabel = "Pay another way".resolvableString,
                addPaymentMethodOptions = addPaymentMethodOptions,
                userSetIsExpanded = true,
                isSettingUp = false,
                merchantName = "Example Inc.",
            ),
            onItemSelected = {},
            onExpandedChanged = {},
            onPrimaryButtonClick = {},
            onPayAnotherWayClicked = {},
            onRemoveClicked = onRemoveClicked,
            onSetDefaultClicked = onSetDefaultClicked,
            showBottomSheetContent = showBottomSheetContent,
            hideBottomSheetContent = hideBottomSheetContent,
            onAddPaymentMethodOptionClicked = onAddPaymentMethodOptionClicked,
            onDismissAlert = {},
            onUpdateClicked = {},
            onLogoutClicked = {},
            expiryDateController = SimpleTextFieldController(DateConfig()),
            cvcController = CvcController(cardBrandFlow = stateFlowOf(CardBrand.Visa))
        )
    }

    private fun createViewModel(
        linkAccountManager: LinkAccountManager = FakeLinkAccountManager(),
        linkConfirmationHandler: LinkConfirmationHandler = FakeLinkConfirmationHandler(),
        navigationManager: TestNavigationManager = TestNavigationManager(),
        dismissalCoordinator: LinkDismissalCoordinator = RealLinkDismissalCoordinator(),
        linkLaunchMode: LinkLaunchMode = LinkLaunchMode.Full
    ): WalletViewModel {
        return WalletViewModel(
            configuration = TestFactory.LINK_CONFIGURATION,
            linkAccount = TestFactory.LINK_ACCOUNT,
            linkAccountManager = linkAccountManager,
            logger = FakeLogger(),
            navigateAndClearStack = {},
            dismissWithResult = {},
            navigationManager = navigationManager,
            linkLaunchMode = linkLaunchMode,
            dismissalCoordinator = dismissalCoordinator,
            completeLinkFlow = DefaultCompleteLinkFlow(
                linkConfirmationHandler = linkConfirmationHandler,
                linkAccountManager = linkAccountManager,
                dismissalCoordinator = dismissalCoordinator,
                linkLaunchMode = linkLaunchMode
            )
        )
    }

    private fun onWalletCollapsedHeader() =
        composeTestRule.onNodeWithTag(COLLAPSED_WALLET_HEADER_TAG, useUnmergedTree = true)

    private fun onWalletCollapsedChevron() =
        composeTestRule.onNodeWithTag(COLLAPSED_WALLET_CHEVRON_ICON_TAG, useUnmergedTree = true)

    private fun onWalletCollapsedPaymentDetails() =
        composeTestRule.onNodeWithTag(COLLAPSED_WALLET_PAYMENT_DETAILS_TAG, useUnmergedTree = true)

    private fun onCollapsedWalletRow() = composeTestRule.onNodeWithTag(COLLAPSED_WALLET_ROW, useUnmergedTree = true)

    private fun onWalletAddPaymentMethodRow() =
        composeTestRule.onNodeWithTag(WALLET_ADD_PAYMENT_METHOD_ROW, useUnmergedTree = true)

    private fun onWalletAddPaymentMethodMenu() =
        composeTestRule.onNodeWithTag(WALLET_SCREEN_ADD_PAYMENT_METHOD_MENU, useUnmergedTree = true)

    private fun onPaymentMethodList() = composeTestRule.onAllNodes(hasTestTag(WALLET_SCREEN_PAYMENT_METHODS_LIST))

    private fun onExpandedWalletHeader() =
        composeTestRule.onNodeWithTag(WALLET_SCREEN_EXPANDED_ROW_HEADER, useUnmergedTree = true)

    private fun onWalletPayButton() =
        composeTestRule.onNodeWithTag(PrimaryButtonTag, useUnmergedTree = true)

    private fun onWalletPayAnotherWayButton() =
        composeTestRule.onNodeWithTag(WALLET_SCREEN_PAY_ANOTHER_WAY_BUTTON, useUnmergedTree = true)

    private fun onWalletFormError() =
        composeTestRule.onNodeWithTag(WALLET_SCREEN_RECOLLECTION_FORM_ERROR, useUnmergedTree = true)

    private fun onWalletFormFields() =
        composeTestRule.onNodeWithTag(WALLET_SCREEN_RECOLLECTION_FORM_FIELDS, useUnmergedTree = true)

    private fun onLoader() = composeTestRule.onNodeWithTag(WALLET_LOADER_TAG)

    private fun onWalletPaymentMethodRowMenuButton() =
        composeTestRule.onAllNodes(hasTestTag(WALLET_PAYMENT_DETAIL_ITEM_MENU_BUTTON), useUnmergedTree = true)

    private fun onWalletPaymentMethodRowLoadingIndicator() =
        composeTestRule.onNodeWithTag(WALLET_PAYMENT_DETAIL_ITEM_LOADING_INDICATOR, useUnmergedTree = true)

    private fun onWalletPaymentMethodMenu() =
        composeTestRule.onNodeWithTag(WALLET_SCREEN_MENU_SHEET_TAG, useUnmergedTree = true)

    private fun onWalletPaymentMethodMenuRemoveTag() =
        composeTestRule.onNodeWithTag(WALLET_MENU_REMOVE_ITEM_TAG, useUnmergedTree = true)

    private fun onWalletPaymentMethodMenuSetAsDefaultTag() =
        composeTestRule.onNodeWithTag(WALLET_MENU_SET_AS_DEFAULT_TAG, useUnmergedTree = true)

    private fun onWalletDialogTag() = composeTestRule.onNodeWithTag(WALLET_SCREEN_DIALOG_TAG, useUnmergedTree = true)

    private fun onWalletDialogButtonTag() = composeTestRule
        .onNodeWithTag(WALLET_SCREEN_DIALOG_BUTTON_TAG, useUnmergedTree = true)

    private fun onWalletErrorTag() = composeTestRule.onNodeWithTag(WALLET_SCREEN_ERROR_TAG, useUnmergedTree = true)
}
