package online.netbee.pos.sample

import android.app.Application
import java.security.Security
import org.bouncycastle.jce.provider.BouncyCastleProvider


class App : Application() {
    override fun onCreate() {
        super.onCreate()

        initSecurity()
    }

    private fun initSecurity() {
        Security.removeProvider("BC")
        Security.addProvider(BouncyCastleProvider())
    }
}