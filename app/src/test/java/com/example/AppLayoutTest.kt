package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.PantryLinkRepository
import com.example.ui.PantryLinkAppScreen
import com.example.ui.PantryLinkViewModel
import com.example.ui.theme.MyApplicationTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppLayoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var db: AppDatabase
    private lateinit var repository: PantryLinkRepository
    private lateinit var viewModel: PantryLinkViewModel

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PantryLinkRepository(db.dao())
        viewModel = PantryLinkViewModel(repository)
    }

    @After
    fun closeDb() {
        if (::db.isInitialized) {
            db.close()
        }
    }

    @Test
    fun testAppRendersAndTabsSelectable() {
        composeTestRule.setContent {
            MyApplicationTheme {
                PantryLinkAppScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
    }
}
