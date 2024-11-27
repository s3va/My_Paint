package one.two.three.mypaint

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow

private const val TAG = "MainViewModel"
class MainViewModel(val appli: Application): AndroidViewModel(appli) {
    val savePic = MutableStateFlow(false)
    fun savePic() {
        savePic.value=true
    }
    fun picSaved() {
        savePic.value=false
    }
}