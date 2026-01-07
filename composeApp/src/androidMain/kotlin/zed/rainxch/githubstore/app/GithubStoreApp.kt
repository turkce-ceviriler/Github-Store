package zed.rainxch.githubstore.app

import android.app.Application
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import zed.rainxch.githubstore.app.di.initKoin
import zed.rainxch.githubstore.feature.details.domain.use_case.CleanupStaleDownloadsUseCase

class GithubStoreApp : Application() {

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@GithubStoreApp)
        }

        val cleanupUseCase = CleanupStaleDownloadsUseCase(get())

        GlobalScope.launch(Dispatchers.IO) {
            cleanupUseCase()
        }
    }
}