package com.shohan.khatago.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shohan.khatago.core.Money
import com.shohan.khatago.core.now
import com.shohan.khatago.core.toStorage
import com.shohan.khatago.data.*
import com.shohan.khatago.data.local.CustomCategoryEntity
import com.shohan.khatago.data.local.Frequency
import com.shohan.khatago.data.local.PersonalDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntrySheet(
    type: AddSheetType,
    categories: List<CustomCategoryEntity>,
    paymentTarget: Pair<String, Long>?,
    onDismiss: () -> Unit,
    onSaveShopCredit: suspend (AddShopCreditInput) -> Unit,
    onSaveLoan: suspend (AddLoanInput) -> Unit,
    onSaveEmi: suspend (AddEmiInput) -> Unit,
    onSavePersonalDebt: suspend (AddPersonalDebtInput) -> Unit,
    onSaveIncome: suspend (AddIncomeInput) -> Unit,
    onSaveExpense: suspend (AddExpenseInput) -> Unit,
    onSavePayment: suspend (AddPaymentInput) -> Unit,
    onMessage: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        when (type) {
            AddSheetType.SHOP_CREDIT -> ShopCreditForm(onDismiss, onSaveShopCredit, onMessage)
            AddSheetType.LOAN -> LoanForm(onDismiss, onSaveLoan, onMessage)
            AddSheetType.EMI -> EmiForm(onDismiss, onSaveEmi, onMessage)
            AddSheetType.PERSONAL_BORROWED -> PersonalDebtForm(PersonalDirection.BORROWED, onDismiss, onSavePersonalDebt, onMessage)
            AddSheetType.PERSONAL_LENT -> PersonalDebtForm(PersonalDirection.LENT, onDismiss, onSavePersonalDebt, onMessage)
            AddSheetType.INCOME -> IncomeForm(categories, onDismiss, onSaveIncome, onMessage)
            AddSheetType.EXPENSE -> ExpenseForm(categories, onDismiss, onSaveExpense, onMessage)
            AddSheetType.PAYMENT -> PaymentForm(paymentTarget, onDismiss, onSavePayment, onMessage)
        }
    }
}

@Composable
private fun FormLayout(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
            Spacer(Modifier.height(8.dp))
        }
    )
}

@Composable
private fun ShopCreditForm(
    onDismiss: () -> Unit,
    onSave: suspend (AddShopCreditInput) -> Unit,
    onMessage: (String) -> Unit
) {
    var shopName by rememberSaveable { mutableStateOf("") }
    var ownerName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var purchaseDate by rememberSaveable { mutableStateOf(now().toStorage().substring(0, 10)) }
    var dueDate by rememberSaveable { mutableStateOf("") }
    var purchaseNotes by rememberSaveable { mutableStateOf("") }
    var items by remember { mutableStateOf(listOf(ItemDraft(), ItemDraft())) }
    val scope = rememberCoroutineScope()
    FormLayout("Add Shop Credit") {
        StandardField(shopName, { shopName = it }, "Shop name")
        StandardField(ownerName, { ownerName = it }, "Owner name")
        StandardField(phone, { phone = it }, "Phone", KeyboardType.Phone)
        StandardField(address, { address = it }, "Address")
        StandardField(purchaseDate, { purchaseDate = it }, "Purchase date (YYYY-MM-DD)")
        StandardField(dueDate, { dueDate = it }, "Due date (optional)")
        items.forEachIndexed { index, item ->
            ElevatedCard {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Item ${index + 1}", style = MaterialTheme.typography.titleMedium)
                    StandardField(item.name, { value -> items = items.update(index) { copy(name = value) } }, "Item name")
                    StandardField(item.quantity, { value -> items = items.update(index) { copy(quantity = value) } }, "Quantity", KeyboardType.Decimal)
                    StandardField(item.unit, { value -> items = items.update(index) { copy(unit = value) } }, "Unit")
                    StandardField(item.unitPrice, { value -> items = items.update(index) { copy(unitPrice = value) } }, "Unit price", KeyboardType.Decimal)
                    Text("Line total: ${Money.format(item.lineTotalMinor())}", style = MaterialTheme.typography.bodyMedium)
                    if (items.size > 1) {
                        TextButton(onClick = { items = items.toMutableList().also { it.removeAt(index) } }) { Text("Remove Item") }
                    }
                }
            }
        }
        OutlinedButton(onClick = { items = items + ItemDraft() }, modifier = Modifier.fillMaxWidth()) { Text("Add Item") }
        StandardField(purchaseNotes, { purchaseNotes = it }, "Purchase notes")
        StandardField(notes, { notes = it }, "Shop notes")
        PrimaryActions(onDismiss = onDismiss) {
            val parsedItems = parseItemDrafts(items)
            if (shopName.isBlank() || parsedItems.isEmpty()) {
                onMessage("Please complete the required fields.")
                return@PrimaryActions
            }
            scope.launchSheet(onMessage, onDismiss, successMessage = "Shop added.") {
                onSave(
                    AddShopCreditInput(
                        shopName = shopName,
                        ownerName = ownerName,
                        phone = phone,
                        address = address,
                        notes = notes,
                        purchaseDate = purchaseDate,
                        dueDate = dueDate.ifBlank { null },
                        items = parsedItems,
                        purchaseNotes = purchaseNotes
                    )
                )
            }
        }
    }
}

@Composable
private fun LoanForm(onDismiss: () -> Unit, onSave: suspend (AddLoanInput) -> Unit, onMessage: (String) -> Unit) {
    SimpleFinanceForm(
        title = "Add Loan",
        baseLabel = "Loan name",
        secondaryLabel = "Institution",
        onDismiss = onDismiss,
        onMessage = onMessage,
        successMessage = "Loan saved."
    ) { name, secondary, totalMinor, installmentMinor, count, firstDueDate, date, notes, frequency ->
        onSave(
            AddLoanInput(
                name = name,
                institution = secondary,
                loanAmountMinor = totalMinor,
                dateTaken = date,
                interestRate = "0",
                processingFeeMinor = 0,
                totalPayableMinor = totalMinor,
                installmentAmountMinor = installmentMinor,
                installmentCount = count,
                frequency = frequency,
                firstDueDate = firstDueDate,
                notes = notes
            )
        )
    }
}

@Composable
private fun EmiForm(onDismiss: () -> Unit, onSave: suspend (AddEmiInput) -> Unit, onMessage: (String) -> Unit) {
    SimpleFinanceForm(
        title = "Add EMI",
        baseLabel = "Product name",
        secondaryLabel = "Seller or provider",
        onDismiss = onDismiss,
        onMessage = onMessage,
        successMessage = "EMI saved."
    ) { name, secondary, totalMinor, installmentMinor, count, firstDueDate, date, notes, frequency ->
        onSave(
            AddEmiInput(
                productName = name,
                provider = secondary,
                purchaseDate = date,
                totalPriceMinor = totalMinor,
                downPaymentMinor = 0,
                financedAmountMinor = totalMinor,
                totalPayableMinor = totalMinor,
                installmentAmountMinor = installmentMinor,
                installmentCount = count,
                frequency = frequency,
                firstDueDate = firstDueDate,
                notes = notes
            )
        )
    }
}

@Composable
private fun SimpleFinanceForm(
    title: String,
    baseLabel: String,
    secondaryLabel: String,
    onDismiss: () -> Unit,
    onMessage: (String) -> Unit,
    successMessage: String,
    onSave: suspend (String, String, Long, Long, Int, String, String, String, Frequency) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var secondary by rememberSaveable { mutableStateOf("") }
    var total by rememberSaveable { mutableStateOf("") }
    var installment by rememberSaveable { mutableStateOf("") }
    var count by rememberSaveable { mutableStateOf("1") }
    var firstDueDate by rememberSaveable { mutableStateOf(now().toStorage().substring(0, 10)) }
    var date by rememberSaveable { mutableStateOf(now().toStorage().substring(0, 10)) }
    var notes by rememberSaveable { mutableStateOf("") }
    var frequency by rememberSaveable { mutableStateOf(Frequency.MONTHLY) }
    val scope = rememberCoroutineScope()
    FormLayout(title) {
        StandardField(name, { name = it }, baseLabel)
        StandardField(secondary, { secondary = it }, secondaryLabel)
        StandardField(total, { total = it }, "Total amount", KeyboardType.Decimal)
        StandardField(installment, { installment = it }, "Installment amount", KeyboardType.Decimal)
        StandardField(count, { count = it }, "Number of installments", KeyboardType.Number)
        StandardField(date, { date = it }, "Start date (YYYY-MM-DD)")
        StandardField(firstDueDate, { firstDueDate = it }, "First due date (YYYY-MM-DD)")
        FrequencySelector(frequency) { frequency = it }
        StandardField(notes, { notes = it }, "Notes")
        PrimaryActions(onDismiss = onDismiss) {
            val totalMinor = Money.parseToMinorUnits(total)
            val installmentMinor = Money.parseToMinorUnits(installment)
            val countInt = count.toIntOrNull()
            if (name.isBlank() || totalMinor == null || installmentMinor == null || countInt == null || countInt <= 0) {
                onMessage("Please complete the required fields.")
                return@PrimaryActions
            }
            scope.launchSheet(onMessage, onDismiss, successMessage) {
                onSave(name, secondary, totalMinor, installmentMinor, countInt, firstDueDate, date, notes, frequency)
            }
        }
    }
}

@Composable
private fun PersonalDebtForm(
    direction: PersonalDirection,
    onDismiss: () -> Unit,
    onSave: suspend (AddPersonalDebtInput) -> Unit,
    onMessage: (String) -> Unit
) {
    var personName by rememberSaveable { mutableStateOf("") }
    var relationship by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var startedOn by rememberSaveable { mutableStateOf(now().toStorage().substring(0, 10)) }
    var expectedDate by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    FormLayout(if (direction == PersonalDirection.BORROWED) "Add Personal Borrowed Money" else "Add Personal Lent Money") {
        StandardField(personName, { personName = it }, "Person name")
        StandardField(relationship, { relationship = it }, "Relationship")
        StandardField(phone, { phone = it }, "Phone", KeyboardType.Phone)
        StandardField(amount, { amount = it }, "Amount", KeyboardType.Decimal)
        StandardField(startedOn, { startedOn = it }, if (direction == PersonalDirection.BORROWED) "Borrowed date (YYYY-MM-DD)" else "Lent date (YYYY-MM-DD)")
        StandardField(expectedDate, { expectedDate = it }, "Expected return date (optional)")
        StandardField(notes, { notes = it }, "Notes")
        PrimaryActions(onDismiss = onDismiss) {
            val amountMinor = Money.parseToMinorUnits(amount)
            if (personName.isBlank() || amountMinor == null) {
                onMessage("Please complete the required fields.")
                return@PrimaryActions
            }
            scope.launchSheet(onMessage, onDismiss, if (direction == PersonalDirection.BORROWED) "Personal debt saved." else "Personal debt saved.") {
                onSave(
                    AddPersonalDebtInput(
                        personName = personName,
                        relationship = relationship,
                        phone = phone,
                        amountMinor = amountMinor,
                        startedOn = startedOn,
                        expectedDate = expectedDate.ifBlank { null },
                        direction = direction,
                        notes = notes
                    )
                )
            }
        }
    }
}

@Composable
private fun IncomeForm(
    categories: List<CustomCategoryEntity>,
    onDismiss: () -> Unit,
    onSave: suspend (AddIncomeInput) -> Unit,
    onMessage: (String) -> Unit
) {
    CashEntryForm(
        title = "Add Income",
        labelOne = "Source",
        defaultCategories = KhataGoRepository.defaultIncomeCategories + categories.filter { it.kind == "INCOME" }.map { it.name },
        onDismiss = onDismiss,
        onMessage = onMessage,
        successMessage = "Income saved."
    ) { occurredAt, amountMinor, label, category, notes ->
        onSave(AddIncomeInput(occurredAt = occurredAt, amountMinor = amountMinor, source = label, category = category, notes = notes))
    }
}

@Composable
private fun ExpenseForm(
    categories: List<CustomCategoryEntity>,
    onDismiss: () -> Unit,
    onSave: suspend (AddExpenseInput) -> Unit,
    onMessage: (String) -> Unit
) {
    CashEntryForm(
        title = "Add Expense",
        labelOne = "Place",
        defaultCategories = KhataGoRepository.defaultExpenseCategories + categories.filter { it.kind == "EXPENSE" }.map { it.name },
        onDismiss = onDismiss,
        onMessage = onMessage,
        successMessage = "Expense saved."
    ) { occurredAt, amountMinor, label, category, notes ->
        onSave(AddExpenseInput(occurredAt = occurredAt, amountMinor = amountMinor, category = category, place = label, notes = notes))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CashEntryForm(
    title: String,
    labelOne: String,
    defaultCategories: List<String>,
    onDismiss: () -> Unit,
    onMessage: (String) -> Unit,
    successMessage: String,
    onSave: suspend (String, Long, String, String, String) -> Unit
) {
    var occurredAt by rememberSaveable { mutableStateOf(now().toStorage()) }
    var amount by rememberSaveable { mutableStateOf("") }
    var label by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(defaultCategories.first()) }
    var notes by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    FormLayout(title) {
        StandardField(label, { label = it }, labelOne)
        StandardField(amount, { amount = it }, "Amount", KeyboardType.Decimal)
        StandardField(occurredAt, { occurredAt = it }, "Date and time (YYYY-MM-DDTHH:MM:SS)")
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = MaterialTheme.shapes.large
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                defaultCategories.distinct().forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = { category = it; expanded = false })
                }
            }
        }
        StandardField(notes, { notes = it }, "Notes")
        PrimaryActions(onDismiss = onDismiss) {
            val amountMinor = Money.parseToMinorUnits(amount)
            if (amountMinor == null || label.isBlank()) {
                onMessage("Please complete the required fields.")
                return@PrimaryActions
            }
            scope.launchSheet(onMessage, onDismiss, successMessage) {
                onSave(occurredAt, amountMinor, label, category, notes)
            }
        }
    }
}

@Composable
private fun PaymentForm(
    paymentTarget: Pair<String, Long>?,
    onDismiss: () -> Unit,
    onSave: suspend (AddPaymentInput) -> Unit,
    onMessage: (String) -> Unit
) {
    var amount by rememberSaveable { mutableStateOf("") }
    var paidAt by rememberSaveable { mutableStateOf(now().toStorage()) }
    var method by rememberSaveable { mutableStateOf("Cash") }
    var notes by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    FormLayout("Add Payment") {
        Text("Open a shop, loan, EMI or personal debt first, then add your payment here.", style = MaterialTheme.typography.bodyMedium)
        StandardField(amount, { amount = it }, "Amount", KeyboardType.Decimal)
        StandardField(paidAt, { paidAt = it }, "Date and time (YYYY-MM-DDTHH:MM:SS)")
        StandardField(method, { method = it }, "Payment method")
        StandardField(notes, { notes = it }, "Notes")
        PrimaryActions(onDismiss = onDismiss) {
            val target = paymentTarget
            val amountMinor = Money.parseToMinorUnits(amount)
            if (target == null || amountMinor == null) {
                onMessage("Open an account first, then add your payment.")
                return@PrimaryActions
            }
            scope.launchSheet(onMessage, onDismiss, successMessage = "Payment added.") {
                onSave(AddPaymentInput(target.first, target.second, amountMinor, paidAt, method, notes))
            }
        }
    }
}

@Composable
private fun FrequencySelector(selected: Frequency, onSelected: (Frequency) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FilterChip(selected = selected == Frequency.WEEKLY, onClick = { onSelected(Frequency.WEEKLY) }, label = { Text("Weekly") })
        FilterChip(selected = selected == Frequency.MONTHLY, onClick = { onSelected(Frequency.MONTHLY) }, label = { Text("Monthly") })
    }
}

@Composable
private fun StandardField(value: String, onValueChange: (String) -> Unit, label: String, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun PrimaryActions(onDismiss: () -> Unit, onSave: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
        Button(onClick = onSave, modifier = Modifier.weight(1f)) { Text("Save") }
    }
}

private data class ItemDraft(
    val name: String = "",
    val quantity: String = "1",
    val unit: String = "",
    val unitPrice: String = ""
) {
    fun lineTotalMinor(): Long {
        val unitPriceMinor = Money.parseToMinorUnits(unitPrice) ?: return 0
        val quantityDecimal = quantity.toBigDecimalOrNull() ?: return 0
        return BigDecimal.valueOf(unitPriceMinor, 2)
            .multiply(quantityDecimal)
            .setScale(2, RoundingMode.HALF_UP)
            .movePointRight(2)
            .longValueExact()
    }
}

private fun parseItemDrafts(items: List<ItemDraft>): List<ShopCreditItemInput> = items.mapNotNull { item ->
    val unitPriceMinor = Money.parseToMinorUnits(item.unitPrice)
    val quantityDecimal = item.quantity.toBigDecimalOrNull()
    if (item.name.isBlank() || unitPriceMinor == null || quantityDecimal == null) return@mapNotNull null
    ShopCreditItemInput(
        itemName = item.name,
        quantityText = item.quantity,
        unit = item.unit,
        unitPriceMinor = unitPriceMinor,
        lineTotalMinor = item.lineTotalMinor()
    )
}

private fun List<ItemDraft>.update(index: Int, transform: ItemDraft.() -> ItemDraft): List<ItemDraft> =
    toMutableList().also { current -> current[index] = current[index].transform() }

private fun CoroutineScope.launchSheet(
    onMessage: (String) -> Unit,
    onDismiss: () -> Unit,
    successMessage: String,
    block: suspend () -> Unit
) {
    launch {
        runCatching { block() }
            .onSuccess {
                onMessage(successMessage)
                onDismiss()
            }
            .onFailure { onMessage(it.message ?: "Something went wrong.") }
    }
}
