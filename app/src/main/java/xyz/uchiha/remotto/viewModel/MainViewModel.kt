package xyz.uchiha.remotto.viewModel

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.uchiha.remotto.background.MessageCodes
import xyz.uchiha.remotto.background.Network
import xyz.uchiha.remotto.background.Utils
import java.net.InetAddress

class MainViewModelFactory(private val clientName: String, private val ip: String, private val mac: String): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = MainViewModel(clientName, ip, mac, Utils.broadcastFromIP(ip)) as T
}

private const val PORT = 10325

class MainViewModel(var clientName: String, var ip: String, var mac: String, var broadcast: String) : ViewModel() {
    private val TAG: String = this.javaClass.simpleName
    private val state: MutableLiveData<Boolean> = MutableLiveData(false) // TODO: implementar um enum com os valores online, offline e desconhecido
    private var network: Network = Network(TAG, ip, PORT)
    private val pkgLoopMap: MutableMap<MessageCodes, Boolean> = mutableMapOf()

    private fun checkState(): Boolean {
        val address: InetAddress = InetAddress.getByName(ip)
        return address.isReachable(1000)
    }

    fun getStateLiveData(): LiveData<Boolean> = state

    fun sendPkg(msg: MessageCodes, data: String = ""){
        Log.d(TAG, "Sending package. Code: ${msg.code}$clientName$data")
        viewModelScope.launch(Dispatchers.IO) { network.sendUDPPacket("${msg.code}$clientName$data") }
    }

    fun sendPkgLoop(msg: MessageCodes){
        Log.d(TAG, "Sending a sequence of packages. Code: " + msg.name)

        pkgLoopMap[msg] = true

        viewModelScope.launch(Dispatchers.IO) {
            while(pkgLoopMap[msg] == true) {
                network.sendUDPPacket(msg.name)
                delay(300L)
            }
        }
    }

    fun cancelPkgLoop(code: MessageCodes): Boolean {
        val oldValue = pkgLoopMap[code]
        pkgLoopMap[code] = false

        return oldValue == true
    }

    fun sendWOLPkg() {
        Log.d(TAG, "Sending WOL package.")
        viewModelScope.launch(Dispatchers.IO) { network.wakeOnLan(mac, broadcast) }
    }

    fun ping(){
        Log.d(TAG, "Sending ping package.")
        viewModelScope.launch(Dispatchers.IO) { state.postValue(checkState()) }
    }

    fun updateData(device: String, ip: String, mac: String){
        this.clientName = device
        this.ip = ip
        this.mac = mac
        this.broadcast = Utils.broadcastFromIP(ip)
        this.network = Network(TAG, ip, PORT)
    }
}