package com.example.heliora.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.heliora.models.*
import com.example.heliora.repository.HealthcareRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HealthcareViewModel(
    private val repository: HealthcareRepository = HealthcareRepository()
) : ViewModel() {

    private val _doctors = MutableStateFlow<List<Doctor>>(emptyList())
    val doctors: StateFlow<List<Doctor>> = _doctors.asStateFlow()

    private val _hospitals = MutableStateFlow<List<Hospital>>(emptyList())
    val hospitals: StateFlow<List<Hospital>> = _hospitals.asStateFlow()

    private val _labs = MutableStateFlow<List<Lab>>(emptyList())
    val labs: StateFlow<List<Lab>> = _labs.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    init {
        fetchDoctors()
        fetchHospitals()
        fetchLabs()
    }

    private fun fetchDoctors() {
        repository.getDoctors()
            .onStart { _isLoading.value = true }
            .onEach { 
                _doctors.value = it
                _isLoading.value = false 
            }
            .catch { e -> _error.emit(e.message ?: "Failed to fetch doctors") }
            .launchIn(viewModelScope)
    }

    private fun fetchHospitals() {
        repository.getHospitals()
            .onStart { _isLoading.value = true }
            .onEach { fetchedHospitals ->
                if (fetchedHospitals.isEmpty()) {
                    _hospitals.value = getSampleHospitals()
                } else {
                    _hospitals.value = fetchedHospitals
                }
                _isLoading.value = false 
            }
            .catch { e -> _error.emit(e.message ?: "Failed to fetch hospitals") }
            .launchIn(viewModelScope)
    }

    private fun fetchLabs() {
        // Since repository might not have getLabs yet, we use sample data
        _isLoading.value = true
        _labs.value = getSampleLabs()
        _isLoading.value = false
    }

    private fun getSampleLabs(): List<Lab> {
        return listOf(
            Lab(id = "1", name = "Dr. Lal PathLabs", type = "Diagnostic Center", location = "Sector 18, Noida", distance = "2.5 km", rating = 4.6, reviewsCount = 2500, services = listOf("Blood Test", "Thyroid", "Diabetes")),
            Lab(id = "2", name = "Metropolis Healthcare", type = "Pathology Lab", location = "Vasant Kunj", distance = "3.2 km", rating = 4.7, reviewsCount = 1800, services = listOf("Full Body Checkup", "Biopsy", "COVID-19")),
            Lab(id = "3", name = "SRL Diagnostics", type = "Diagnostic Center", location = "Panchsheel Park", distance = "1.8 km", rating = 4.5, reviewsCount = 3000, services = listOf("MRI", "CT Scan", "Ultrasound")),
            Lab(id = "4", name = "Thyrocare", type = "Reference Lab", location = "Indirapuram", distance = "5.4 km", rating = 4.4, reviewsCount = 4500, services = listOf("Thyroid", "Liver Profile", "Kidney Profile")),
            Lab(id = "5", name = "Apollo Diagnostics", type = "Diagnostic Center", location = "Greater Kailash", distance = "4.0 km", rating = 4.8, reviewsCount = 1200, services = listOf("X-Ray", "ECG", "PFT")),
            Lab(id = "6", name = "Healthians", type = "Home Collection Lab", location = "Gurugram", distance = "6.5 km", rating = 4.3, reviewsCount = 850, isHomeCollectionAvailable = true, services = listOf("All Blood Tests", "Vitamin D")),
            Lab(id = "7", name = "Redcliffe Labs", type = "Digital Lab", location = "Janakpuri", distance = "2.2 km", rating = 4.7, reviewsCount = 1500, services = listOf("Genomics", "Oncology", "Pathology"))
        )
    }

    private fun getSampleHospitals(): List<Hospital> {
        return listOf(
            Hospital(id = "1", name = "City General Hospital", type = "Multi-Specialty", location = "Sector 12, Dwarka", distance = "1.2 km", rating = 4.8, reviewsCount = 1200, departments = listOf("Cardiology", "Neurology", "Pediatrics")),
            Hospital(id = "2", name = "Apollo Health Center", type = "Super-Specialty", location = "Sarita Vihar", distance = "4.5 km", rating = 4.7, reviewsCount = 850, departments = listOf("Oncology", "Orthopedics", "Cardiology")),
            Hospital(id = "3", name = "Max Super Specialty", type = "Multi-Specialty", location = "Saket", distance = "3.8 km", rating = 4.9, reviewsCount = 2100, departments = listOf("Emergency", "Neurology", "Gastroenterology")),
            Hospital(id = "4", name = "Fortis Memorial", type = "General Hospital", location = "Gurugram", distance = "8.2 km", rating = 4.6, reviewsCount = 950, departments = listOf("Urology", "Dermatology", "Pediatrics")),
            Hospital(id = "5", name = "Holy Family Hospital", type = "Charitable Trust", location = "Okhla", distance = "5.1 km", rating = 4.4, reviewsCount = 600, departments = listOf("General Physician", "Maternity", "ENT")),
            Hospital(id = "6", name = "Medanta - The Medicity", type = "Super-Specialty", location = "Gurugram", distance = "10.5 km", rating = 4.8, reviewsCount = 3200, departments = listOf("Heart Institute", "Kidney Transplant", "Cancer Care")),
            Hospital(id = "7", name = "Aakash Healthcare", type = "Multi-Specialty", location = "Dwarka Sector 3", distance = "2.0 km", rating = 4.5, reviewsCount = 450, departments = listOf("Orthopedics", "Ophthalmology", "Dental"))
        )
    }

    fun bookAppointment(appointment: Appointment) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.bookAppointment(appointment)
            if (result.isSuccess) {
                // Success handled in UI
            } else {
                _error.emit(result.exceptionOrNull()?.message ?: "Booking failed")
            }
            _isLoading.value = false
        }
    }

    fun fetchUserAppointments(userId: String) {
        repository.getAppointments(userId)
            .onEach { _appointments.value = it }
            .catch { e -> _error.emit(e.message ?: "Failed to fetch appointments") }
            .launchIn(viewModelScope)
    }
}
