package com.example.testappbt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.navigation.fragment.findNavController
import com.example.bt_def.BluetoothConstants
import com.example.bt_def.bluetooth.BluetoothController
import com.example.testappbt.databinding.FragmentMainBinding
import com.google.android.material.snackbar.Snackbar

class MainFragment : Fragment(), BluetoothController.Listener {

    private lateinit var binding: FragmentMainBinding
    private lateinit var bluetoothController: BluetoothController
    private lateinit var bAdapter: BluetoothAdapter
    private var prefApp: SharedPreferences? = null
    private var calibrationFactor: String? = null


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
                        val editor = prefApp?.edit()
                        editor?.putString(DataConstants.CALIBRATION_FACTOR, message.substring(1))
                    } else {
                        binding.txtViewStatus.text = message
                    }
                }

            }
        }
    }
}