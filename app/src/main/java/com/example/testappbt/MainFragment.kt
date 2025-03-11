package com.example.testappbt


import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.navigation.fragment.findNavController
import com.example.bt_def.BluetoothConstants
import com.example.bt_def.bluetooth.BluetoothController
import com.example.testappbt.databinding.FragmentMainBinding
import com.google.android.material.snackbar.Snackbar
import android.renderscript.ScriptGroup.Binding
import android.util.Log
import com.example.testappbt.fbot.ApiService
import com.example.testappbt.fbot.StringRequest
import com.example.testappbt.fbot.StringResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainFragment : Fragment(), BluetoothController.Listener {

    private lateinit var binding: FragmentMainBinding
    private lateinit var bluetoothController: BluetoothController
    private lateinit var bAdapter: BluetoothAdapter
    private var prefApp: SharedPreferences? = null
    private var calibrationFactor: String? = null

    private var callories = 0f
    private var belki = 0f
    private var jiri = 0f
    private var uglevodi = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBTAdapter()
        val pref = activity?.getSharedPreferences(
            BluetoothConstants.PREFERENCES, Context.MODE_PRIVATE)
        val mac = pref?.getString(BluetoothConstants.MAC, "")
        bluetoothController = BluetoothController(bAdapter)
        //calibrationFactor
        prefApp = activity?.getSharedPreferences(
            DataConstants.PREFERENCES, Context.MODE_PRIVATE)
        calibrationFactor = prefApp?.getString(DataConstants.CALIBRATION_FACTOR, "358")

        binding.btList.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_deviceListFragment)
        }

        binding.btConnect.setOnClickListener{
            bluetoothController.connect(mac ?: "", this)
        }

        binding.btTare.setOnClickListener {
            bluetoothController.sendMessage("t")
        }
        binding.btColibrate.setOnClickListener {
            bluetoothController.sendMessage("c")
            var weight = binding.etColibrate.text.toString()
            bluetoothController.sendMessage(weight)
        }
        val spinner: Spinner = binding.spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.spinner_items,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                when (selectedItem){
                    resources.getStringArray(R.array.spinner_items)[0] ->
                        setKBJU(89f,1.1f, 0.3f, 22.8f) //banan
                    resources.getStringArray(R.array.spinner_items)[1] ->
                        setKBJU(52f,0.3f, 0.2f, 14f) //jabloko
                    resources.getStringArray(R.array.spinner_items)[2] ->
                        setKBJU(47f,0.9f, 0.2f, 11.8f) //orange
                }
                //Snackbar.make(binding.root, "Вы выбрали: $selectedItem", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://api.deepseek.com")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        val apiService = retrofit.create(ApiService::class.java)
//
//// Выполните запрос к API
//
//        val inputString = "какой сегодня день?"
//        val request = StringRequest(inputString)
//
//        apiService.getPrediction(request).enqueue(object : Callback<StringResponse> {
//            override fun onResponse(call: Call<StringResponse>, response: Response<StringResponse>) {
//                if (response.isSuccessful) {
//                    val stringResponse = response.body()
//                    stringResponse?.let {
//                        Log.d("Response", it.response)
//                    }
//                } else {
//                    Log.e("Response", "Error: ${response.errorBody()?.string()}")
//                }
//            }
//
//            override fun onFailure(call: Call<StringResponse>, t: Throwable) {
//                // Обработка ошибки
//                t.printStackTrace()
//            }
//        })

    }

    private fun setKBJU(c :Float, b:Float, j: Float, u:Float){
        callories = c
        belki = b
        jiri = j
        uglevodi = u
    }

    private fun raschet(value: Float, ves:Float): Float{
        val result = ves * value / 100
        return (Math.round(result * 10) / 10.0).toFloat()
    }

    private fun viewKBJU(mes: String){
        val varMes = mes.substring(0, mes.length-2)
        val ves = varMes.toFloat()
        //Snackbar.make(binding.root, "$ves gramm", Toast.LENGTH_SHORT).show()
        binding.txtJiri.text = "F\n ${raschet(jiri, ves)}"
        binding.txtBelki.text = "P\n ${raschet(belki, ves)}"
        binding.txtUglevodi.text = "CH\n ${raschet(uglevodi, ves)}"
        binding.txtCallories.text = "C\n ${raschet(callories, ves)}"
    }

    private fun initBTAdapter(){
        val bManager = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bAdapter = bManager.adapter
    }

    override fun onReceive(message: String) {
        activity?.runOnUiThread{
            when(message){
                BluetoothController.BLUETOOTH_CONNECTED -> {
                    binding.btConnect.backgroundTintList = AppCompatResources
                        .getColorStateList(requireContext(), R.color.red)

                    binding.btConnect.text = "Disconnect"
                    Snackbar.make(binding.root, "Connected", Snackbar.LENGTH_SHORT).show()
                    bluetoothController.sendMessage("k$calibrationFactor")
                }
                BluetoothController.BLUETOOTH_NO_CONNECTED -> {
                    binding.btConnect.backgroundTintList = AppCompatResources
                        .getColorStateList(requireContext(), R.color.green)
                    binding.btConnect.text = "Connect"
                    Snackbar.make(binding.root, "Disconnected", Snackbar.LENGTH_SHORT).show()
                }
                BluetoothController.MESSAGE_EMPTY -> {

                }


                else -> {
                    if (message.startsWith("k"))
                    {
                        binding.etColibrate.setText("")
                        val editor = prefApp?.edit()
                        editor?.putString(DataConstants.CALIBRATION_FACTOR, message.substring(1))
                    } else {
                        binding.txtViewStatus.text = message
                        viewKBJU(message)
                    }
                }

            }
        }
    }
}