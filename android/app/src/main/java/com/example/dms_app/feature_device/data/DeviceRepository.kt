package  com.example.dms_app.feature_device.data
import com.example.dms_app.core.network.ApiClient
import com.example.dms_app.core.network.PairResponse
import com.example.dms_app.core.network.PairRequest

class DeviceRepository {

    suspend fun pairDevice(request: PairRequest): PairResponse {

        return ApiClient.apiService.pairDevice(request)
    }
}
