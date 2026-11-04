import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.net.InetAddress
import java.nio.ByteOrder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF8B5CF6),
                    background = Color(0xFF0F172A),
                    surface = Color(0xFF1E293B),
                    onBackground = Color(0xFFF8FAFC),
                    onSurface = Color(0xFFF8FAFC)
                )
            ) {
                NetProScreen(this)
            }
        }
    }
}

@Composable
fun NetProScreen(context: Context) {
    var signal by remember { mutableStateOf(-100) }
    var network by remember { mutableStateOf("غير متصل") }
    var linkSpeed by remember { mutableStateOf("--") }
    var ipAddress by remember { mutableStateOf("--") }
    var hasSignal by remember { mutableStateOf(false) }

    val updateSignal = {
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifi.connectionInfo
        if (info != null && info.ssid != null && info.ssid != "<unknown ssid>") {
            signal = info.rssi
            network = info.ssid.replace("\"", "")
            linkSpeed = "${info.linkSpeed} Mbps"
            
            // تحويل عنوان الـ IP من رقم صحيح إلى نصي
            val ipInt = info.ipAddress
            ipAddress = if (ipInt != 0) {
                InetAddress.getByAddress(
                    ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()
                ).hostAddress ?: "غير معروف"
            } else {
                "غير معروف"
            }
            hasSignal = true
        } else {
            signal = 0
            network = "غير متصل"
            linkSpeed = "--"
            ipAddress = "--"
            hasSignal = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            updateSignal()
        }
    }

    // الألوان الذكية للإشارة
    val signalColor = when {
        !hasSignal -> Color.Gray
        signal >= -65 -> Color(0xFF10B981) // أخضر (ممتاز)
        signal >= -80 -> Color(0xFFF59E0B) // أصفر (متوسط)
        else -> Color(0xFFEF4444)           // أحمر (ضعيف)
    }

    val signalStatusText = when {
        !hasSignal -> "غير متصل"
        signal >= -65 -> "إشارة ممتازة"
        signal >= -80 -> "إشارة متوسطة"
        else -> "إشارة ضعيفة"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 10.dp)) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(signalColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = signalColor,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "NetPro",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )
                Text(
                    text = "NETWORK ANALYZER",
                    style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray, letterSpacing = 2.sp)
                )
            }

            // Main Signal Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SignalWifi4Bar, contentDescription = null, tint = signalColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(signalStatusText, style = MaterialTheme.typography.bodyMedium, color = signalColor, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                        Text(
                            text = if (hasSignal) "$signal" else "--",
                            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold, fontSize = 56.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (hasSignal) {
                            Text(
                                text = " dBm",
                                style = MaterialTheme.typography.titleMedium,
                                color = signalColor,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("اسم الشبكة المتصلة", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = network,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Advanced Network Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Link Speed
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("سرعة الربط", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(linkSpeed, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }

                    Divider(
                        modifier = Modifier
                            .height(35.dp)
                            .width(1.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    )

                    // IP Address
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("عنوان IP", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(ipAddress, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Button
            Button(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        updateSignal()
                    } else {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("تحديث بيانات الشبكة", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            // Footer
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 5.dp)) {
                Text("Developed & Designed by Alhadi Rex", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(2.dp))
                Text("All Rights Reserved 2026 ©", style = MaterialTheme.typography.labelSmall, color = Color.Gray.copy(alpha = 0.7f))
            }
        }
    }
}
