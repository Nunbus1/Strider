package ViewModels
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.strider.R

class ImageViewModel : ViewModel() {
    var imagePath by mutableStateOf<String?>(null)

    fun updateImagePath(path: String) {
        imagePath = path
        // Perform any other necessary logic here, e.g., update LiveData, etc.
    }
}