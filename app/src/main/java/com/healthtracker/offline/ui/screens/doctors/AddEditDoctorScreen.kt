package com.healthtracker.offline.ui.screens.doctors

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthtracker.offline.ui.viewmodels.DoctorViewModel
import com.healthtracker.offline.data.entities.Doctor

/**
 * Screen for adding or editing a doctor.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDoctorScreen(
    doctorId: Int?,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DoctorViewModel = hiltViewModel()
) {
    val uiState by viewModel.addEditUiState.collectAsStateWithLifecycle()
    val isEditing = doctorId != null
    
    LaunchedEffect(doctorId) {
        if (doctorId != null) {
            viewModel.loadDoctorForEdit(doctorId)
        } else {
            viewModel.resetForm()
        }
    }
    
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaveSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditing) "Edit Doctor" else "Add Doctor")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveDoctor()
                        },
                        enabled = uiState.isFormValid && !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            DoctorForm(
                formState = uiState.formState,
                onFormChange = viewModel::updateFormState,
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
    
    // Show error if any
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or error dialog
            viewModel.clearError()
        }
    }
}

@Composable
private fun DoctorForm(
    formState: DoctorFormState,
    onFormChange: (DoctorFormState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Name field
        OutlinedTextField(
            value = formState.name,
            onValueChange = { onFormChange(formState.copy(name = it)) },
            label = { Text("Doctor Name *") },
            placeholder = { Text("Enter doctor's full name") },
            isError = formState.nameError != null,
            supportingText = formState.nameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null
                )
            }
        )
        
        // Speciality field
        OutlinedTextField(
            value = formState.speciality,
            onValueChange = { onFormChange(formState.copy(speciality = it)) },
            label = { Text("Speciality *") },
            placeholder = { Text("e.g., Cardiology, General Medicine") },
            isError = formState.specialityError != null,
            supportingText = formState.specialityError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = null
                )
            }
        )
        
        // PMDC Number field
        OutlinedTextField(
            value = formState.pmdcNumber,
            onValueChange = { onFormChange(formState.copy(pmdcNumber = it)) },
            label = { Text("PMDC Number") },
            placeholder = { Text("Pakistan Medical & Dental Council number") },
            isError = formState.pmdcNumberError != null,
            supportingText = formState.pmdcNumberError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null
                )
            }
        )
        
        // Mobile Number field
        OutlinedTextField(
            value = formState.mobileNumber,
            onValueChange = { onFormChange(formState.copy(mobileNumber = it)) },
            label = { Text("Mobile Number") },
            placeholder = { Text("Contact mobile number") },
            isError = formState.mobileNumberError != null,
            supportingText = formState.mobileNumberError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null
                )
            }
        )
        
        // Qualifications section
        QualificationsSection(
            qualifications = formState.qualifications,
            onQualificationsChange = { onFormChange(formState.copy(qualifications = it)) }
        )
        
        // Form validation info
        if (formState.name.isBlank() || formState.speciality.isBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Name and Speciality are required fields",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun QualificationsSection(
    qualifications: List<String>,
    onQualificationsChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var newQualification by remember { mutableStateOf("") }
    
    Column(modifier = modifier) {
        Text(
            text = "Qualifications",
            style = MaterialTheme.typography.titleSmall
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Add new qualification
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newQualification,
                onValueChange = { newQualification = it },
                label = { Text("Add qualification") },
                placeholder = { Text("e.g., MBBS, MD, FCPS") },
                modifier = Modifier.weight(1f),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null
                    )
                }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = {
                    if (newQualification.isNotBlank()) {
                        onQualificationsChange(qualifications + newQualification.trim())
                        newQualification = ""
                    }
                },
                enabled = newQualification.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add qualification"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Display existing qualifications
        qualifications.forEachIndexed { index, qualification ->
            QualificationChip(
                qualification = qualification,
                onRemove = {
                    onQualificationsChange(qualifications.toMutableList().apply {
                        removeAt(index)
                    })
                }
            )
        }
    }
}

@Composable
private fun QualificationChip(
    qualification: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = qualification,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove qualification",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Data class representing the form state for adding/editing a doctor.
 */
data class DoctorFormState(
    val name: String = "",
    val speciality: String = "",
    val pmdcNumber: String = "",
    val mobileNumber: String = "",
    val qualifications: List<String> = emptyList(),
    val nameError: String? = null,
    val specialityError: String? = null,
    val pmdcNumberError: String? = null,
    val mobileNumberError: String? = null
) {
    val isValid: Boolean
        get() = name.isNotBlank() && 
                speciality.isNotBlank() && 
                nameError == null && 
                specialityError == null && 
                pmdcNumberError == null && 
                mobileNumberError == null
}