package com.fplay.audioapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.fplay.audioapp.activities.OptionsPage;

public class BaseActivity extends AppCompatActivity implements FragmentDoesDonts.OnFragmentInteractionListener{

    private DrawerLayout mDrawerLayout;
    private   NavigationView  navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);

        mDrawerLayout = findViewById(R.id.drawer_layout);

      navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        Drawable drawable = menuItem.getIcon();
                        if (drawable != null) {
                            drawable.mutate();
                            drawable.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                        }
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                       displaySelectedScreen(menuItem.getItemId());

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });

        //displaySelectedScreen(R.id.menu_home);
        Fragment fragment = new OptionsPage();

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

    }

    private void displaySelectedScreen(int itemId) {

        //creating fragment object
        Fragment fragment = null;
        Bundle bundl = null;
        //initializing the fragment object which is selected
        switch (itemId) {

            case R.id.menu_how_to_reach:

                String latitude = "26.8453217";
                String latitude_cur = "26.8453217";
                String longitude = "80.9493923";
                String longitude_cur = "80.9493923";


                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr="+latitude_cur+","+longitude_cur+"&daddr="+latitude+","+longitude));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_LAUNCHER );
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(intent);
                break;

            case R.id.menu_attractions:
                startActivity(new Intent(getApplicationContext(), Attractions.class));
                break;

            case R.id.menu_dos_donts:
                bundl = new Bundle();
                bundl.putString("url", "http://swan.lucknowzooweb.c66.me/does_and_donts");
                fragment = new FragmentDoesDonts();
                fragment.setArguments(bundl);
                break;
            case R.id.menu_adopt:
                bundl = new Bundle();
                bundl.putString("url", "http://swan.lucknowzooweb.c66.me/adopt");
                fragment = new WebViewFragment();
                fragment.setArguments(bundl);
                break;

            case R.id.menu_donate:
                bundl = new Bundle();
                bundl.putString("url", "http://swan.lucknowzooweb.c66.me/donate");
                fragment = new WebViewFragment();
                fragment.setArguments(bundl);
                break;

//            case R.id.menu_butterflypark:
//                bundl = new Bundle();
//                bundl.putString("url", "https://www.youtube.com/watch?v=1iaJkzbQJbE");
//                fragment = new WebViewFragment();
//                fragment.setArguments(bundl);
//                break;

            case R.id.menu_book_your_ticket:
                bundl = new Bundle();
                bundl.putString("url", "http://swan.lucknowzooweb.c66.me/ticketing");
                fragment = new WebViewFragment();
                fragment.setArguments(bundl);
                break;

        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }


        mDrawerLayout.closeDrawer(GravityCompat.START);
    }


    public void openDrawer(){
        mDrawerLayout.openDrawer(navigationView);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}