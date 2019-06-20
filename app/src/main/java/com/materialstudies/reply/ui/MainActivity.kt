package com.materialstudies.reply.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.materialstudies.reply.R
import com.materialstudies.reply.databinding.ActivityMainBinding
import com.materialstudies.reply.ui.nav.AlphaSlideAction
import com.materialstudies.reply.ui.nav.ChangeSettingsMenuStateAction
import com.materialstudies.reply.ui.nav.QuarterRotateSlideAction
import com.materialstudies.reply.ui.nav.ShowHideFabStateAction
import com.materialstudies.reply.util.contentView

class MainActivity : AppCompatActivity(),
    Toolbar.OnMenuItemClickListener,
    NavController.OnDestinationChangedListener {

    private val binding: ActivityMainBinding by contentView(R.layout.activity_main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpBottomNavigationAndFab()
    }

    private fun setUpBottomNavigationAndFab() {
        // Wrap binding.run to ensure ContentViewBindingDelegate is calling this Activity's
        // setContentView before accessing views
        binding.run {
            findNavController(R.id.nav_host_fragment).addOnDestinationChangedListener(
                this@MainActivity
            )
        }

        // Set a custom animation for showing and hiding the FAB
        binding.fab.apply {
            setShowMotionSpecResource(R.animator.fab_show)
            setHideMotionSpecResource(R.animator.fab_hide)
        }

        binding.bottomNavigationDrawer.apply {
            addOnSlideAction(QuarterRotateSlideAction(binding.bottomAppBarChevron))
            addOnSlideAction(AlphaSlideAction(binding.bottomAppBarTitle, true))
            addOnStateChangedAction(ShowHideFabStateAction(binding.fab))
            addOnStateChangedAction(ChangeSettingsMenuStateAction { showSettings ->
                // Toggle between the current destination's BAB menu and the menu which should
                // be displayed when the BottomNavigationDrawer is open.
                binding.bottomAppBar.replaceMenu(if (showSettings) {
                    R.menu.bottom_app_bar_settings_menu
                } else {
                    getBottomAppBarMenuForDestination()
                })
            })
        }

        // Set up the BottomAppBar menu
        binding.bottomAppBar.apply {
            overflowIcon = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.ic_more_vert_on_branded
            )
            setNavigationOnClickListener {
                binding.bottomNavigationDrawer.toggle()
            }
            setOnMenuItemClickListener(this@MainActivity)
        }

        // Set up the BottomNavigationDrawer's open/close affordance
        binding.bottomAppBarContentContainer.setOnClickListener {
            binding.bottomNavigationDrawer.toggle()
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        // Set the configuration of the BottomAppBar and FAB based on the current destination.
        when (destination.id) {
            R.id.homeFragment ->
                setBottomAppBarForHome(getBottomAppBarMenuForDestination(destination))
            R.id.emailFragment ->
                setBottomAppBarForEmail(getBottomAppBarMenuForDestination(destination))
        }
    }

    /**
     * Helper function which returns the menu which should be displayed for the current
     * destination.
     *
     * Used both when the destination has changed, centralizing destination-to-menu mapping, as
     * well as switching between the alternate menu used when the BottomNavigationDrawer is
     * open and closed.
     */
    @MenuRes
    private fun getBottomAppBarMenuForDestination(destination: NavDestination? = null): Int {
        val dest = destination ?: findNavController(R.id.nav_host_fragment).currentDestination
        return when (dest?.id) {
            R.id.homeFragment -> R.menu.bottom_app_bar_home_menu
            R.id.emailFragment -> R.menu.bottom_app_bar_email_menu
            else -> R.menu.bottom_app_bar_home_menu
        }
    }

    private fun setBottomAppBarForHome(@MenuRes menuRes: Int) {
        binding.run {
            fab.setImageResource(R.drawable.ic_edit_on_secondary)
            bottomAppBar.replaceMenu(menuRes)
            bottomAppBarTitle.visibility = View.VISIBLE
        }
    }

    private fun setBottomAppBarForEmail(@MenuRes menuRes: Int) {
        binding.run {
            fab.setImageResource(R.drawable.ic_reply_all_on_secondary)
            bottomAppBar.replaceMenu(menuRes)
            bottomAppBarTitle.visibility = View.INVISIBLE
        }
    }


    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_theme -> {
                binding.bottomNavigationDrawer.close()
                showDarkThemeMenu()
            }
        }
        return true
    }

    private fun showDarkThemeMenu() {
        MenuBottomSheetDialogFragment(R.menu.dark_theme_bottom_sheet_menu) {
            onDarkThemeMenuItemSelected(it.itemId)
        }.show(supportFragmentManager, null)
    }

    /**
     * Set this Activity's night mode based on a user's in-app selection.
     */
    private fun onDarkThemeMenuItemSelected(itemId: Int): Boolean {
        val nightMode = when (itemId) {
            R.id.menu_light -> AppCompatDelegate.MODE_NIGHT_NO
            R.id.menu_dark -> AppCompatDelegate.MODE_NIGHT_YES
            R.id.menu_battery_saver -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            R.id.menu_system_default -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> return false
        }

        delegate.localNightMode = nightMode
        return true
    }
}
