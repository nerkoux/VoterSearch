package com.keofi.poonamashishmehta_votergen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.keofi.poonamashishmehta_votergen.ui.screen.HomeScreen
import com.keofi.poonamashishmehta_votergen.ui.screen.ImportScreen
import com.keofi.poonamashishmehta_votergen.ui.screen.ListDetailScreen
import com.keofi.poonamashishmehta_votergen.ui.screen.ListsScreen
import com.keofi.poonamashishmehta_votergen.ui.screen.OcrReviewScreen
import com.keofi.poonamashishmehta_votergen.ui.screen.PrinterScreen
import com.keofi.poonamashishmehta_votergen.ui.screen.SearchScreen
import com.keofi.poonamashishmehta_votergen.ui.screen.SettingsScreen
import com.keofi.poonamashishmehta_votergen.ui.screen.SlipPreviewScreen
import com.keofi.poonamashishmehta_votergen.ui.screen.VoterDetailScreen
import com.keofi.poonamashishmehta_votergen.ui.screen.WelcomeScreen
import com.keofi.poonamashishmehta_votergen.ui.theme.DarkNavy
import com.keofi.poonamashishmehta_votergen.ui.theme.DividerGray
import com.keofi.poonamashishmehta_votergen.ui.theme.LightSurface
import com.keofi.poonamashishmehta_votergen.ui.theme.SaffronOrange
import com.keofi.poonamashishmehta_votergen.ui.theme.SecondaryText

sealed interface TopLevelRoute {
    val icon: ImageVector
    val label: String
}

data object WelcomeRoute
data object HomeRoute : TopLevelRoute {
    override val icon = Icons.Default.Home
    override val label = "Home"
}
data object SearchRoute : TopLevelRoute {
    override val icon = Icons.Default.Search
    override val label = "Search"
}
data object ListsRoute : TopLevelRoute {
    override val icon = Icons.AutoMirrored.Filled.List
    override val label = "Lists"
}
data object PrinterRoute : TopLevelRoute {
    override val icon = Icons.Default.Print
    override val label = "Printer"
}
data object SettingsRoute : TopLevelRoute {
    override val icon = Icons.Default.Settings
    override val label = "Settings"
}

data class VoterDetailRoute(val voterId: Long)
data class SlipPreviewRoute(val voterId: Long)
data object ImportRoute
data class ListDetailRoute(val listId: Long)
data class OcrReviewRoute(val voterId: Long)

private val TOP_LEVEL_ROUTES: List<TopLevelRoute> = listOf(
    HomeRoute, SearchRoute, ListsRoute, PrinterRoute, SettingsRoute
)

@Composable
fun MainScreen() {
    val topLevelBackStack = remember { TopLevelBackStack<Any>(WelcomeRoute) }

    Scaffold(
        bottomBar = {
            val currentKey = topLevelBackStack.topLevelKey
            val isRootScreen = topLevelBackStack.isCurrentAtRoot()
            val isWelcome = currentKey is WelcomeRoute

            if (isRootScreen && !isWelcome) {
                Column {
                    HorizontalDivider(thickness = 0.8.dp, color = DividerGray)
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 0.dp
                    ) {
                        TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
                            val isSelected = topLevelRoute == currentKey
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    topLevelBackStack.addTopLevel(topLevelRoute)
                                },
                                icon = {
                                    Icon(
                                        imageVector = topLevelRoute.icon,
                                        contentDescription = topLevelRoute.label
                                    )
                                },
                                label = { Text(topLevelRoute.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SaffronOrange,
                                    selectedTextColor = SaffronOrange,
                                    indicatorColor = LightSurface,
                                    unselectedIconColor = SecondaryText,
                                    unselectedTextColor = SecondaryText
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = topLevelBackStack.backStack,
            onBack = { topLevelBackStack.pop() },
            modifier = Modifier
                .padding(innerPadding)
                .background(Color.White),
            entryProvider = entryProvider {
                entry<WelcomeRoute> {
                    WelcomeScreen(
                        onNext = {
                            topLevelBackStack.replaceStartupWith(HomeRoute)
                        }
                    )
                }
                entry<HomeRoute> {
                    HomeScreen(
                        onNavigateToSearch = { topLevelBackStack.addTopLevel(SearchRoute) },
                        onNavigateToImport = { topLevelBackStack.push(ImportRoute) },
                        onNavigateToPrinter = { topLevelBackStack.addTopLevel(PrinterRoute) },
                        onNavigateToLists = { topLevelBackStack.addTopLevel(ListsRoute) }
                    )
                }
                entry<SearchRoute> {
                    SearchScreen(
                        onVoterClick = { voterId -> topLevelBackStack.push(VoterDetailRoute(voterId)) }
                    )
                }
                entry<ListsRoute> {
                    ListsScreen(
                        onSelectList = { listId -> topLevelBackStack.push(ListDetailRoute(listId)) }
                    )
                }
                entry<PrinterRoute> {
                    PrinterScreen()
                }
                entry<SettingsRoute> {
                    SettingsScreen(
                        onNavigateToPrinter = { topLevelBackStack.addTopLevel(PrinterRoute) }
                    )
                }
                entry<VoterDetailRoute> { route ->
                    VoterDetailScreen(
                        voterId = route.voterId,
                        onBack = { topLevelBackStack.pop() },
                        onGenerateSlip = { voterId -> topLevelBackStack.push(SlipPreviewRoute(voterId)) }
                    )
                }
                entry<SlipPreviewRoute> { route ->
                    SlipPreviewScreen(
                        voterId = route.voterId,
                        onBack = { topLevelBackStack.pop() }
                    )
                }
                entry<ImportRoute> {
                    ImportScreen(
                        onBack = { topLevelBackStack.pop() },
                        onViewList = { listId -> topLevelBackStack.push(ListDetailRoute(listId)) }
                    )
                }
                entry<ListDetailRoute> { route ->
                    ListDetailScreen(
                        listId = route.listId,
                        onBack = { topLevelBackStack.pop() },
                        onVoterClick = { voterId -> topLevelBackStack.push(VoterDetailRoute(voterId)) },
                        onStartReview = { voterId -> topLevelBackStack.push(OcrReviewRoute(voterId)) }
                    )
                }
                entry<OcrReviewRoute> { route ->
                    OcrReviewScreen(
                        voterId = route.voterId,
                        onBack = { topLevelBackStack.pop() },
                        onAccepted = { topLevelBackStack.pop() }
                    )
                }
            }
        )
    }
}

class TopLevelBackStack<T : Any>(startKey: T) {
    private var topLevelStacks: LinkedHashMap<T, SnapshotStateList<T>> = linkedMapOf(
        startKey to mutableStateListOf(startKey)
    )

    var topLevelKey by mutableStateOf(startKey)
        private set

    val backStack = mutableStateListOf(startKey)

    private fun updateBackStack() {
        backStack.clear()
        backStack.addAll(topLevelStacks.flatMap { it.value })
    }

    fun isCurrentAtRoot(): Boolean {
        val currentStack = topLevelStacks[topLevelKey]
        return currentStack != null && currentStack.size <= 1
    }

    fun addTopLevel(key: T) {
        if (topLevelStacks[key] == null) {
            topLevelStacks[key] = mutableStateListOf(key)
        } else {
            topLevelStacks.apply {
                remove(key)?.let {
                    put(key, it)
                }
            }
        }
        topLevelKey = key
        updateBackStack()
    }

    fun replaceStartupWith(key: T) {
        topLevelStacks.clear()
        topLevelStacks[key] = mutableStateListOf(key)
        topLevelKey = key
        updateBackStack()
    }

    fun push(route: T) {
        topLevelStacks[topLevelKey]?.add(route)
        updateBackStack()
    }

    fun pop() {
        val currentStack = topLevelStacks[topLevelKey]
        if (currentStack != null && currentStack.size > 1) {
            currentStack.removeLast()
            updateBackStack()
        } else {
            removeLast()
        }
    }

    fun removeLast() {
        val removedKey = topLevelStacks[topLevelKey]?.removeLastOrNull()
        topLevelStacks.remove(removedKey)
        if (topLevelStacks.isNotEmpty()) {
            topLevelKey = topLevelStacks.keys.last()
        }
        updateBackStack()
    }
}
