package com.tmark.client.ui.screens.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmark.client.data.CartManager
import com.tmark.client.data.api.ApiResult
import com.tmark.client.data.local.TokenStore
import com.tmark.client.data.model.*
import com.tmark.client.data.repository.RequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectContact(
    val phone: String = "",
    val name: String = "",
    val designationId: String = "",
    val designationName: String = "",
    val email: String = "",
    val lookedUp: Boolean = false,
    val lookingUp: Boolean = false,
    val lookupError: String? = null
)

data class ProductionHouseForm(
    val searchQuery: String = "",
    val selectedId: String? = null,
    val selectedName: String = "",
    // new production house
    val newName: String = "",
    val newContactPerson: String = "",
    val newPhone: String = "",
    val newAddress: String = "",
    val isCreatingNew: Boolean = false
)

data class NewRequestUiState(
    val step: Int = 1,
    // Step 1 — your details
    val clientName: String = "",
    val clientPhone: String = "",
    val projectName: String = "",
    val projectType: String = "",
    val projectLocation: String = "",
    val notes: String = "",
    // Step 2 — equipment
    val packages: List<BootstrapPackage> = emptyList(),
    val items: List<BootstrapItem> = emptyList(),
    val selected: Map<String, com.tmark.client.data.model.SelectedEquipment> = emptyMap(),
    // Step 3 — dates
    val selectedDates: List<String> = emptyList(),
    // Step 4 — contacts
    val contacts: List<ProjectContact> = listOf(ProjectContact()),
    val designations: List<BootstrapDesignation> = emptyList(),
    // Step 5 — production house
    val productionHouses: List<BootstrapProductionHouse> = emptyList(),
    val productionHouseForm: ProductionHouseForm = ProductionHouseForm(),
    // meta
    val loadingCatalog: Boolean = false,
    val submitting: Boolean = false,
    val submitted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NewRequestViewModel @Inject constructor(
    private val repo: RequestRepository,
    private val tokenStore: TokenStore,
    private val cartManager: CartManager
) : ViewModel() {

    private val _ui = MutableStateFlow(NewRequestUiState())
    val ui: StateFlow<NewRequestUiState> = _ui

    init { loadAll() }

    private fun loadAll() {
        viewModelScope.launch {
            val name  = tokenStore.clientName.firstOrNull() ?: ""
            val phone = tokenStore.clientPhone.firstOrNull() ?: ""
            // Pre-populate equipment and dates from cart if available
            val cartSnapshot = cartManager.snapshot()
            val cartDates = cartManager.selectedDates.value
            _ui.value = _ui.value.copy(
                clientName = name, clientPhone = phone,
                loadingCatalog = true,
                selected = if (cartSnapshot.isNotEmpty()) cartSnapshot else _ui.value.selected,
                selectedDates = if (cartDates.isNotEmpty()) cartDates else _ui.value.selectedDates
            )
            when (val r = repo.getBootstrap()) {
                is ApiResult.Success -> _ui.value = _ui.value.copy(
                    loadingCatalog = false,
                    packages = r.data.packages,
                    items = r.data.items,
                    designations = r.data.designations,
                    productionHouses = r.data.productionHouses
                )
                else -> _ui.value = _ui.value.copy(loadingCatalog = false)
            }
        }
    }

    // Step 1
    fun onProjectName(v: String)     { _ui.value = _ui.value.copy(projectName = v, error = null) }
    fun onProjectType(v: String)     { _ui.value = _ui.value.copy(projectType = v) }
    fun onProjectLocation(v: String) { _ui.value = _ui.value.copy(projectLocation = v) }
    fun onNotes(v: String)           { _ui.value = _ui.value.copy(notes = v) }

    // Step 2 — equipment
    fun incrementItem(entityId: String, type: String, name: String, price: Double, maxQty: Int = Int.MAX_VALUE) {
        val updated = _ui.value.selected.toMutableMap()
        val cur = updated[entityId]
        val newQty = ((cur?.quantity ?: 0) + 1).coerceAtMost(maxQty)
        updated[entityId] = com.tmark.client.data.model.SelectedEquipment(entityId, type, name, price, newQty, maxQty)
        _ui.value = _ui.value.copy(selected = updated)
    }

    fun decrementItem(entityId: String) {
        val updated = _ui.value.selected.toMutableMap()
        val cur = updated[entityId] ?: return
        if (cur.quantity <= 1) updated.remove(entityId) else updated[entityId] = cur.copy(quantity = cur.quantity - 1)
        _ui.value = _ui.value.copy(selected = updated)
    }

    // Step 3 — dates
    fun toggleDate(date: String) {
        val dates = _ui.value.selectedDates.toMutableList()
        if (dates.contains(date)) dates.remove(date) else dates.add(date)
        _ui.value = _ui.value.copy(selectedDates = dates.sorted())
    }

    // Step 4 — contacts
    fun addContact() {
        _ui.value = _ui.value.copy(contacts = _ui.value.contacts + ProjectContact())
    }

    fun removeContact(index: Int) {
        val updated = _ui.value.contacts.toMutableList()
        if (updated.size > 1) updated.removeAt(index)
        _ui.value = _ui.value.copy(contacts = updated)
    }

    fun updateContactPhone(index: Int, phone: String) {
        val updated = updateContact(index) { it.copy(phone = phone, lookedUp = false, lookupError = null, name = if (it.lookedUp) "" else it.name) }
        _ui.value = _ui.value.copy(contacts = updated)
    }

    fun updateContactName(index: Int, name: String) {
        _ui.value = _ui.value.copy(contacts = updateContact(index) { it.copy(name = name) })
    }

    fun updateContactDesignation(index: Int, id: String, name: String) {
        _ui.value = _ui.value.copy(contacts = updateContact(index) { it.copy(designationId = id, designationName = name) })
    }

    fun updateContactEmail(index: Int, email: String) {
        _ui.value = _ui.value.copy(contacts = updateContact(index) { it.copy(email = email) })
    }

    fun lookupContactPhone(index: Int) {
        val contact = _ui.value.contacts.getOrNull(index) ?: return
        if (contact.phone.length < 7) return
        _ui.value = _ui.value.copy(contacts = updateContact(index) { it.copy(lookingUp = true, lookupError = null) })
        viewModelScope.launch {
            when (val r = repo.lookupPhone(contact.phone)) {
                is ApiResult.Success -> {
                    val data = r.data
                    _ui.value = _ui.value.copy(contacts = updateContact(index) { c ->
                        when {
                            !data.found -> c.copy(lookingUp = false, lookupError = "Not found in system")
                            data.blocked == true -> c.copy(lookingUp = false, lookupError = "Profile private")
                            else -> {
                                val client = data.client!!
                                val desigId = client.designationId ?: c.designationId
                                val desigName = client.designationName ?: c.designationName
                                c.copy(
                                    lookingUp = false, lookedUp = true, lookupError = null,
                                    name = client.name,
                                    email = client.email ?: c.email,
                                    designationId = desigId,
                                    designationName = desigName
                                )
                            }
                        }
                    })
                }
                else -> _ui.value = _ui.value.copy(contacts = updateContact(index) { it.copy(lookingUp = false, lookupError = "Lookup failed") })
            }
        }
    }

    private fun updateContact(index: Int, transform: (ProjectContact) -> ProjectContact): List<ProjectContact> {
        return _ui.value.contacts.toMutableList().also { list ->
            if (index in list.indices) list[index] = transform(list[index])
        }
    }

    // Step 5 — production house
    fun updatePHSearch(query: String) {
        _ui.value = _ui.value.copy(productionHouseForm = _ui.value.productionHouseForm.copy(searchQuery = query, selectedId = null, selectedName = ""))
    }

    fun selectProductionHouse(id: String, name: String) {
        _ui.value = _ui.value.copy(productionHouseForm = _ui.value.productionHouseForm.copy(
            selectedId = id, selectedName = name, searchQuery = name, isCreatingNew = false
        ))
    }

    fun clearProductionHouse() {
        _ui.value = _ui.value.copy(productionHouseForm = ProductionHouseForm())
    }

    fun toggleCreatingNewPH(creating: Boolean) {
        _ui.value = _ui.value.copy(productionHouseForm = _ui.value.productionHouseForm.copy(
            isCreatingNew = creating, selectedId = null, selectedName = "", searchQuery = ""
        ))
    }

    fun updateNewPHName(v: String)          { _ui.value = _ui.value.copy(productionHouseForm = _ui.value.productionHouseForm.copy(newName = v)) }
    fun updateNewPHContactPerson(v: String) { _ui.value = _ui.value.copy(productionHouseForm = _ui.value.productionHouseForm.copy(newContactPerson = v)) }
    fun updateNewPHPhone(v: String)         { _ui.value = _ui.value.copy(productionHouseForm = _ui.value.productionHouseForm.copy(newPhone = v)) }
    fun updateNewPHAddress(v: String)       { _ui.value = _ui.value.copy(productionHouseForm = _ui.value.productionHouseForm.copy(newAddress = v)) }

    // Pre-select from catalog
    fun preSelect(entityId: String, entityType: String) {
        val s = _ui.value
        if (s.selected.containsKey(entityId)) return
        val pkg  = s.packages.firstOrNull { it.id == entityId }
        val item = s.items.firstOrNull { it.id == entityId }
        val eq = when {
            pkg != null  -> com.tmark.client.data.model.SelectedEquipment(pkg.id, "package", pkg.name, pkg.pricePerDay, 1)
            item != null -> com.tmark.client.data.model.SelectedEquipment(item.id, "item", item.name, item.pricePerDay, 1)
            else -> return
        }
        val updated = s.selected.toMutableMap()
        updated[entityId] = eq
        _ui.value = s.copy(selected = updated, step = 2)
    }

    // True when the user pre-filled both equipment and dates via the cart+calendar flow
    val fromCartFlow: Boolean
        get() = cartManager.snapshot().isNotEmpty() && cartManager.selectedDates.value.isNotEmpty()

    // Navigation
    fun nextStep() {
        val s = _ui.value
        when (s.step) {
            1 -> {
                if (s.projectName.isBlank()) { _ui.value = s.copy(error = "Project name is required"); return }
                // Skip equipment + dates steps when they are pre-filled from the basket/calendar flow
                if (s.selected.isNotEmpty() && s.selectedDates.isNotEmpty()) {
                    _ui.value = s.copy(step = 4, error = null)
                    return
                }
            }
            2 -> if (s.selected.isEmpty()) { _ui.value = s.copy(error = "Select at least one item"); return }
            3 -> if (s.selectedDates.isEmpty()) { _ui.value = s.copy(error = "Select at least one date"); return }
        }
        if (s.step < 6) _ui.value = s.copy(step = s.step + 1, error = null)
    }

    fun prevStep() {
        val s = _ui.value
        // When in step 4 from the cart flow, go back to step 1 (not 3)
        if (s.step == 4 && s.selected.isNotEmpty() && s.selectedDates.isNotEmpty() && fromCartFlow) {
            _ui.value = s.copy(step = 1, error = null)
            return
        }
        if (s.step > 1) _ui.value = s.copy(step = s.step - 1, error = null)
    }

    val estimatedTotal: Double
        get() = _ui.value.let { s ->
            s.selected.values.sumOf { it.pricePerDay * it.quantity } * s.selectedDates.size
        }

    fun submit() {
        val s = _ui.value
        val phForm = s.productionHouseForm
        viewModelScope.launch {
            _ui.value = s.copy(submitting = true, error = null)

            val validContacts = s.contacts.filter { it.name.isNotBlank() && it.designationId.isNotBlank() }

            val body = CreateRequestBody(
                projectName = s.projectName.trim(),
                requestedDates = s.selectedDates,
                selectedItems = s.selected.values.map { com.tmark.client.data.model.SelectedItem(it.type, it.entityId, it.quantity) },
                notes = s.notes.trim().ifBlank { null },
                projectType = s.projectType.trim().ifBlank { null },
                projectLocation = s.projectLocation.trim().ifBlank { null },
                productionHouseId = phForm.selectedId,
                productionHouse = if (!phForm.isCreatingNew || phForm.newName.isBlank()) null else NewProductionHouseBody(
                    name = phForm.newName.trim(),
                    phone = phForm.newPhone.trim().ifBlank { null },
                    address = phForm.newAddress.trim().ifBlank { null },
                    contactPerson = phForm.newContactPerson.trim().ifBlank { null }
                ),
                contacts = validContacts.map { c ->
                    ContactBody(
                        designationId = c.designationId,
                        contactName = c.name.trim(),
                        phone = c.phone.trim().ifBlank { null },
                        email = c.email.trim().ifBlank { null }
                    )
                }.ifEmpty { null }
            )

            when (val r = repo.createRequest(body)) {
                is ApiResult.Success   -> { cartManager.clear(); _ui.value = _ui.value.copy(submitting = false, submitted = true) }
                is ApiResult.Error     -> _ui.value = _ui.value.copy(submitting = false, error = r.message)
                is ApiResult.Exception -> _ui.value = _ui.value.copy(submitting = false, error = "Network error: ${r.throwable.message}")
            }
        }
    }
}
