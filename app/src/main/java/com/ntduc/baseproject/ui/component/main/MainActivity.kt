package com.ntduc.baseproject.ui.component.main

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import com.ntduc.baseproject.R
import com.ntduc.baseproject.databinding.ActivityMainBinding
import com.ntduc.baseproject.ui.base.BaseActivity
import com.ntduc.baseproject.utils.setupNavigationWithNavigationBar
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val viewModel: MainViewModel by viewModels()

    override fun initView() {
        super.initView()

        navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = setupNavigationWithNavigationBar(
            navController = navController,
            bottomNavigationView = binding.bnv,
            topLevelDestinationIds = setOf(R.id.homeFragment, R.id.filesFragment, R.id.securityFragment, R.id.cleanFragment)
        )

        navController.addOnDestinationChangedListener { _: NavController?, navDestination: NavDestination, _: Bundle? ->
            when (navDestination.id) {
                R.id.homeFragment, R.id.filesFragment, R.id.securityFragment, R.id.cleanFragment -> binding.bnv.visible()
                else -> binding.bnv.gone()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController)
                || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }
}