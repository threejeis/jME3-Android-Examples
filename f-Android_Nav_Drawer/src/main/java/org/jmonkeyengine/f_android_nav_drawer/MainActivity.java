package org.jmonkeyengine.f_android_nav_drawer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.jme3.math.ColorRGBA;

import org.jmonkeyengine.f_android_nav_drawer.gamelogic.JmeAndroidInterface;
import org.jmonkeyengine.f_android_nav_drawer.gamelogic.Main;
import org.jmonkeyengine.f_android_nav_drawer.gamelogic.UserSettings;

public class MainActivity extends AppCompatActivity implements JmeAndroidInterface {
    private static String TAG = "MainActivity";
    private static int INITIAL_HIDE_DELAY = 3000;
    private Main jmeApp = null;
    private ColorRGBA currentColor = null;
    private Toolbar toolbar = null;
    private FloatingActionButton fab = null;

    /** List of strings to display as selection choices in the drawer */
    private String[] drawerTitles;
    /** Main layout for the entire screen that contains the main content and the drawer view */
    private DrawerLayout drawerLayout;
    /** List view in the drawer */
    private ListView drawerList;
    /** Main layout view that includes the drawer views */
    private View drawerView;

    /** Constant Request Code to define the settings activity */
    private static int REQUEST_SETTINGS = 1;

    private ActionBarDrawerToggle drawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The "activity_main" layout includes the reference
        // to the fragment that contains the GLSurfaceView
        // that will be used to display the jME content.
        setContentView(R.layout.activity_main);

        // set the toolbar that has the menu items
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // find the floating action button and define a Snackbar to be displayed when touched
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // set a listener on the Red UI Button to tell the jME code to change the color of the box
        Button redButton = (Button) findViewById(R.id.redButton);
        redButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jmeApp.setColor(ColorRGBA.Red);
            }
        });

        // set a listener on the Green UI Button to tell the jME code to change the color of the box
        Button greenButton = (Button) findViewById(R.id.greenButton);
        greenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jmeApp.setColor(ColorRGBA.Green);
            }
        });

        // set a listener on the Blue UI Button to tell the jME code to change the color of the box
        Button blueButton = (Button) findViewById(R.id.blueButton);
        blueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jmeApp.setColor(ColorRGBA.Blue);
            }
        });


        // Save a reference to the jME application for later use when a menu item is selected.
        JmeFragment jmeFragment = (JmeFragment) getFragmentManager()
                .findFragmentById(R.id.jMEFragment);
        jmeApp = (Main)jmeFragment.getJmeApplication();

        // Set the androidInterface object in the game logic to this class.
        // This enables the game logic to callback to this class to modify the
        // Android based menu items.
        jmeApp.setAndroidInterface(this);

        // create a listener for the system ui visibility changes so that the toolbar and
        // floating action button can also be hidden / shown to match the system ui.
        View mDecorView = getWindow().getDecorView();
        // listener to toggle user controls visibility
        mDecorView.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener(){
                    @Override
                    public void onSystemUiVisibilityChange(int flags) {
                        boolean visible =
                                (flags & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;

                        toolbar.setVisibility(visible ? View.VISIBLE: View.GONE);
                        fab.setVisibility(visible ? View.VISIBLE: View.GONE);

                    }
                });

        // Create a gesture detector that will be used to re-hide the system ui when touched
        // Avoid simple onClick listeners because they are triggered by the swipe gesture
        // that shows the system UI.
        final GestureDetector clickDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        boolean visible = (getWindow().getDecorView().getSystemUiVisibility()
                                & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;
                        if (visible) {
                            hideSystemUI();
                        } else {
                            showSystemUI();
                        }
                        return true;
                    }
                });
        toolbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return clickDetector.onTouchEvent(motionEvent);
            }
        });


        drawerTitles = getResources().getStringArray(R.array.drawer_titles_array);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerView = (View) findViewById(R.id.main_drawer_view);

        // Set the adapter for the list view
        drawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, drawerTitles));
        Log.d(TAG, "Drawer has " + drawerList.getAdapter().getCount() + " items.");
        // Set the list's click listener
        drawerList.setOnItemClickListener(new DrawerItemClickListener());


        final CharSequence mTitle;
        final CharSequence mDrawerTitle;
        mTitle = mDrawerTitle = getTitle();

        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                android.R.drawable.ic_menu_zoom,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(drawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    // method to hide the system ui
    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
        );
    }

    // method to show the system ui
    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }


    // Trigger the initial hide() shortly after the activity has been
    // created to briefly hint to the user that UI controls are available.
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(INITIAL_HIDE_DELAY);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    // Define handler that hides the system ui when a message is received.
    Handler mHideSystemUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            hideSystemUI();
        }
    };

    // remove previously posted messages and send new message to hide the system UI
    // after a defined delay amount of time
    private void delayedHide(int delayMillis) {
        mHideSystemUiHandler.removeMessages(0);
        mHideSystemUiHandler.sendEmptyMessageDelayed(0, delayMillis);
    }

    // When the window loses focus (ie, the action overflow is shown)
    // cancel any pending hide actions.  When the window gains focus,
    // hide the system UI.
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            delayedHide(INITIAL_HIDE_DELAY);
        } else {
            mHideSystemUiHandler.removeMessages(0);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /*
    Inflate the xml file that defines the menu items
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items, menu);
        return true;
    }

    // set the enable/disable state of the menu item based on the current color
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean matchesCurrentColor;
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerView);
        if (drawerOpen) {
            Log.d(TAG, "Nav Drawer is open, disabling all menu items");
            menu.findItem(R.id.action_red).setEnabled(false);
            menu.findItem(R.id.action_green).setEnabled(false);
            menu.findItem(R.id.action_blue).setEnabled(false);
        } else {
            if (currentColor != null) {
                matchesCurrentColor = currentColor.equals(ColorRGBA.Red);
                menu.findItem(R.id.action_red).setEnabled(!matchesCurrentColor);
                Log.d(TAG, "Red Enabled: " + !matchesCurrentColor);

                matchesCurrentColor = currentColor.equals(ColorRGBA.Green);
                menu.findItem(R.id.action_green).setEnabled(!matchesCurrentColor);
                Log.d(TAG, "Green Enabled: " + !matchesCurrentColor);

                matchesCurrentColor = currentColor.equals(ColorRGBA.Blue);
                menu.findItem(R.id.action_blue).setEnabled(!matchesCurrentColor);
                Log.d(TAG, "Blue Enabled: " + !matchesCurrentColor);
            } else {
                Log.d(TAG, "currentColor is null, enabling all menu items");
                menu.findItem(R.id.action_red).setEnabled(true);
                menu.findItem(R.id.action_green).setEnabled(true);
                menu.findItem(R.id.action_blue).setEnabled(true);
            }
        }


        return true;
    }

    /*
    When a menu item is selected, call a method in the game logic to perform
    an action based which menu item is selected.
    This uses the reference to the game logic instance obtained in onCreate
    from the jME fragment.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (jmeApp == null) {
            Toast.makeText(this, "No jME Application Found!!!", Toast.LENGTH_LONG)
                    .show();
        } else {
            switch (item.getItemId()) {
                // action with ID action_red was selected
                case R.id.action_red:
                    jmeApp.setColor(ColorRGBA.Red);
                    break;
                // action with ID action_green was selected
                case R.id.action_green:
                    jmeApp.setColor(ColorRGBA.Green);
                    break;
                // action with ID action_blue was selected
                case R.id.action_blue:
                    jmeApp.setColor(ColorRGBA.Blue);
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    // called from the game logic to disable the menu selection for the current color
    @Override
    public void disableColorOption(ColorRGBA color) {
        currentColor = color;
        invalidateOptionsMenu();
    }

    // called from the game logic to toggle showing the System Bar and Toolbar / Menus from
    // within the jME context
    @Override
    public void toggleMenus() {
        // is the system ui already visible?
        boolean visible = (getWindow().getDecorView().getSystemUiVisibility()
                & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;

        // need to run on the android ui thread.  jME runs on the GLSurfaceView thread and only
        // the ui thread can modify the android views.
        if (visible) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideSystemUI();
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showSystemUI();
                }
            });
        }

    }

    /**
     * Click Listener to respond to users selecting an item in the navigation drawer.
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Displays the activity related to the item selected in the drawer */
    private void selectItem(int position) {
//        // Create a new fragment and specify the planet to show based on position
//        Fragment fragment = new PlanetFragment();
//        Bundle args = new Bundle();
//        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
//        fragment.setArguments(args);
//
//        // Insert the fragment by replacing any existing fragment
//        FragmentManager fragmentManager = getFragmentManager();
//        fragmentManager.beginTransaction()
//                .replace(R.id.content_frame, fragment)
//                .commit();

        Intent intent=new Intent(MainActivity.this, SettingsActivity.class);
        startActivityForResult(intent, REQUEST_SETTINGS);// Activity is started with requestCode 2

        // Highlight the selected item, update the title, and close the drawer
        drawerList.setItemChecked(position, true);
        setTitle(drawerTitles[position]);
        drawerLayout.closeDrawer(drawerView);
    }

    @Override
    public void setTitle(CharSequence title) {
        Log.d(TAG, "Setting Actionbar Title: " + title);
        getSupportActionBar().setTitle(title);
    }

    // Call Back method when the other activity is closed
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult requestCode: " + requestCode + ", resultCode: " + resultCode);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==REQUEST_SETTINGS) {
            switch (resultCode) {
                case RESULT_CANCELED:
                    Log.e(TAG, "UserSettings activity closed with RESULT_CANCELED and returned to MainActivity");
                    break;
                case RESULT_FIRST_USER:
                    Log.e(TAG, "UserSettings activity closed with RESULT_FIRST_USER and returned to MainActivity");
                    break;
                case RESULT_OK:
                    Log.e(TAG, "UserSettings activity closed with RESULT_OK and returned to MainActivity");
                    break;
                default:
                    break;
            }

            updatePreferences();

        }
    }

    /**
     * Method to send the Android Shared Preferences to jME game logic to update game settings.
     * Android Shared Preferences are used by the UserSettings screen as a way to store preferences.
     * When the user returns from the settings screen, we sent the values to jME.
     */
    private void updatePreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        UserSettings userSettings = jmeApp.getUserSettings();

        // get the preference value
        String viewFPSKey = getString(R.string.pref_display_fps_key);
        boolean viewFPS = sharedPref.getBoolean(viewFPSKey, true);
        Log.d(TAG, "Preference " + viewFPSKey + ": " + viewFPS);
        // update the jME game logic
        boolean successFPS = userSettings.setShowFPS(viewFPS);
        Log.d(TAG, "successFPS: " + successFPS);
        // If the set was unsuccessful, change the preference to the last value;
        if (!successFPS) { editor.putBoolean(viewFPSKey, userSettings.getShowFPS()); }

        // get the preference value
        String viewStatsKey = getString(R.string.pref_display_stats_key);
        boolean viewStats = sharedPref.getBoolean(viewStatsKey, true);
        Log.d(TAG, "Preference " + viewStatsKey + ": " + viewStats);
        // update the jME game logic
        boolean successStats = userSettings.setShowStats(viewStats);
        Log.d(TAG, "successStats: " + successStats);
        // If the set was unsuccessful, change the preference to the last value;
        if (!successStats) { editor.putBoolean(viewStatsKey, userSettings.getShowStats()); }
    }

}