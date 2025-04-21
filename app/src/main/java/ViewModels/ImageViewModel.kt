package ViewModels
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * ViewModel responsable de la gestion de l'image de profil de l'utilisateur.
 * Il conserve le chemin vers l'image sélectionnée ou capturée.
 */
class ImageViewModel : ViewModel() {

    // -------------------------
    // État du chemin vers l'image sélectionnée
    // -------------------------

    var imagePath by mutableStateOf<String?>(null)

    /**
     * Met à jour le chemin vers l’image de profil.
     *
     * @param path Le chemin absolu vers l'image locale.
     */
    fun updateImagePath(path: String) {
        imagePath = path
    }
} 