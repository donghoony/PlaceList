package org.konkuk.placelist

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.konkuk.placelist.databinding.ActivityMainBinding

@RequiresApi(Build.VERSION_CODES.Q)

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getPermissions()
    }

    private fun getPermissions() {
        lateinit var dialog: AlertDialog
        lateinit var backgroundDialog: AlertDialog

        val permissions = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
        )

        fun checkPermission(permission: String) =
            ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

        fun openPermissionSettings() = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.fromParts("package", packageName, null)
        }.run(::startActivity)

        val multipleLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()){
                map->
                if (map.all{permission -> permission.value}){
                    if (!checkPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                        backgroundDialog.show()
                }
            }

        // 추후에 Dialog 커스텀 필요할 수 있음 -> Fragment로 변경 등
        dialog = AlertDialog.Builder(this)
            .setTitle("권한 요청")
            .setMessage("어플리케이션을 사용하기 위해서는 권한이 필요합니다.\n권한 요청을 승인해 주세요.")
            .setPositiveButton("확인"){
                _, _ ->
                multipleLauncher.launch(permissions)
            }
            .setNegativeButton("종료"){ _, _ ->
                finish()
            }
            .create()

        backgroundDialog = AlertDialog.Builder(this)
            .setTitle("백그라운드 권한 요청")
            .setMessage("권한 - 위치 탭에서 '항상 허용'에 체크해주세요")
            .setPositiveButton("확인"){ _, _ ->
                openPermissionSettings()
            }
            .setNegativeButton("종료"){ _, _ ->
                finish()
            }
            .create()

        if (!permissions.all{permission -> checkPermission(permission)}) dialog.show()
        else if (!checkPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)) backgroundDialog.show()
    }
}
