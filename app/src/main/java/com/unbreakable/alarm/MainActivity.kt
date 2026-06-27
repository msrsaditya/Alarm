package com.unbreakable.alarm
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.unbreakable.alarm.data.AlarmRepository
import com.unbreakable.alarm.data.AppDatabase
import com.unbreakable.alarm.ui.AddEditAlarmScreen
import com.unbreakable.alarm.ui.AlarmListScreen
import com.unbreakable.alarm.ui.AlarmViewModel
import com.unbreakable.alarm.ui.AlarmViewModelFactory
import com.unbreakable.alarm.ui.theme.MyApplicationTheme
class MainActivity : ComponentActivity() {
    private val repository by lazy { (applicationContext as AlarmApplication).repository }
    private val scheduler by lazy { AlarmScheduler(applicationContext) }
    private val viewModel: AlarmViewModel by viewModels {
        AlarmViewModelFactory(repository, scheduler)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                var showSplash by remember { mutableStateOf(true) }
                if (showSplash) {
                    com.unbreakable.alarm.ui.AppSplashScreen {
                        showSplash = false
                    }
                } else {
                    PermissionsCheckScreen {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            val navController = rememberNavController()
                            NavHost(
                                navController = navController,
                                startDestination = "alarms",
                                enterTransition = {
                                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500)) + fadeIn(animationSpec = tween(500))
                                },
                                exitTransition = {
                                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500)) + fadeOut(animationSpec = tween(500))
                                },
                                popEnterTransition = {
                                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500)) + fadeIn(animationSpec = tween(500))
                                },
                                popExitTransition = {
                                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500)) + fadeOut(animationSpec = tween(500))
                                }
                            ) {
                                composable("alarms") {
                                    AlarmListScreen(
                                        viewModel = viewModel,
                                        onNavigateToAdd = { navController.navigate("add_edit_alarm/-1") },
                                        onNavigateToEdit = { alarmId -> navController.navigate("add_edit_alarm/$alarmId") }
                                    )
                                }
                                composable("add_edit_alarm/{id}") { backStackEntry ->
                                    val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: -1
                                    val alarm = viewModel.alarms.value.find { it.id == id }
                                    AddEditAlarmScreen(
                                        viewModel = viewModel,
                                        alarm = alarm,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}