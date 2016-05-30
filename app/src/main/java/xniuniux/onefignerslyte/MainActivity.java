package xniuniux.onefignerslyte;

import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AppShortcutFragment.OnFragmentInteractionListener,
        AppChooserFragment.OnFragmentInteractionListener {

    private String LOG_TAG = "Main Activity";

    /** Used for interaction with child fragments*/
    public static final int FRG_ACTION_KILL = -1;
    public static final int FRG_ACTION_LAUNCH_APPCHOOSER = 1;
    public static final int FRG_ACTION_CONFIRM= 2;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private AppShortcutFragment mAppShortcutFragment = new AppShortcutFragment();
    private AppChooserFragment mAppChooserFragment;
    public boolean isAppListShow = false;
    public PackageManager packageManager;

    public static ArrayList<AppShortInfo> appArrayList;

    public class AppShortInfo {
        CharSequence name;
        ArrayList<Bitmap> icons;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab_rb);
        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab_lb);

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        ViewGroup MainView = (ViewGroup) findViewById(R.id.main_content);
        MainView.setBackground(wallpaperDrawable);

        mViewPager = (ViewPager) findViewById(R.id.container);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        setSupportActionBar(toolbar);

        if (fab1 != null) {
            fab1.setOnClickListener(fab_1_OnclickListener);
        }

        if (fab2 != null) {
            fab2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int current = mViewPager.getCurrentItem();
                    Snackbar.make(view, "Now is on page " + current, Snackbar.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), "Now is on page " + current,
                            Toast.LENGTH_SHORT).show();
                    /*Intent i = new Intent();
                    i.setClass(MainActivity.this, AllAppListActivity.class);
                    startActivity(i);*/
                }
            });
        }

        //TODO: do this in background? AsyncTask?

        if (appArrayList==null || appArrayList.size()<24){
            Log.d(LOG_TAG,"retrieving app data");
            Intent i = new Intent(Intent.ACTION_MAIN, null);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            packageManager = getPackageManager();
            List<ResolveInfo> launchableApps = packageManager.queryIntentActivities(i, 0);
            appArrayList = new ArrayList<>();
            for (ResolveInfo RI : launchableApps ){
                AppShortInfo app = new AppShortInfo();
                app.name = RI.activityInfo.packageName;
                app.icons = highlightImage(((BitmapDrawable) RI.activityInfo.loadIcon(packageManager)).getBitmap());
                appArrayList.add(app);
            }
        }
        mAppShortcutFragment.setAppList(appArrayList);
    }



    @Override
    public void onFragmentInteraction(String tag, int action) {
        FragmentManager fm = getSupportFragmentManager();
        switch (tag){
            case "AppShortcutFragment":
                if (action == FRG_ACTION_KILL){
                    fm.beginTransaction().remove(mAppShortcutFragment).commit();
                    break;
                }
                if (action == FRG_ACTION_LAUNCH_APPCHOOSER){
                    mAppChooserFragment = new AppChooserFragment();
                    fm.beginTransaction().replace(R.id.main_content, mAppChooserFragment).addToBackStack(null).commit();
                    break;
                }
            case "AppChooserFragment":
                if (action == FRG_ACTION_CONFIRM){
                    fm.popBackStack();
                    break;
                }
        }

        Log.d(LOG_TAG, "fragment send:" + tag);


    }

    @Override
    public void onPause(){
        if (isAppListShow){
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().remove(mAppShortcutFragment).commit();
            isAppListShow = false;
        }
        super.onPause();
    }

    private View.OnClickListener fab_1_OnclickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            FragmentManager fm = getSupportFragmentManager();
            if (!isAppListShow) {
                if (!mAppShortcutFragment.isAdded()){
                    fm.beginTransaction()
                            .add(R.id.main_content, mAppShortcutFragment)
                            .commit();
                    isAppListShow = true;
                    return;
                }
            } else {
                mAppShortcutFragment.hideList();
                isAppListShow = false;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public final ArrayList<Fragment> fragments = new ArrayList<>();
        public int pageNum = 5;
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object){
            if (position < getCount()) {
                FragmentManager m = ((Fragment) object).getFragmentManager();
                FragmentTransaction t = m.beginTransaction();
                t.remove((Fragment) object);
                t.commit();
            }
            this.notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a SectionFragment (defined as a static inner class below).
            return SectionFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return pageNum;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 2";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }

    public ArrayList<Bitmap> highlightImage(Bitmap icon) {

        Log.d(LOG_TAG, "Draw:" + icon.getWidth() + " " + icon.getHeight());

        ArrayList<Bitmap> icons = new ArrayList<>();
        Paint ptBlur = new Paint();

        ptBlur.setMaskFilter(new BlurMaskFilter(25, BlurMaskFilter.Blur.OUTER));
        int[] offsetXY = new int[2];
        Bitmap backLight = icon.extractAlpha(ptBlur, offsetXY);

        ptBlur.setMaskFilter(new BlurMaskFilter(20, BlurMaskFilter.Blur.OUTER));
        int[] offsetXYSmall = new int[2];
        Bitmap backLightSmall = icon.extractAlpha(ptBlur, offsetXYSmall);

        Paint ptAlphaColor = new Paint();

        Bitmap iconOut = Bitmap.createBitmap(backLight.getWidth() , backLight.getHeight() , Bitmap.Config.ARGB_8888);
        Bitmap highlighter = Bitmap.createBitmap(backLight.getWidth() , backLight.getHeight() ,   Bitmap.Config.ARGB_8888);
        Bitmap highlighterSmall = Bitmap.createBitmap(backLight.getWidth() , backLight.getHeight() ,   Bitmap.Config.ARGB_8888);


        ptAlphaColor.setColor(ContextCompat.getColor(this, R.color.long_pressed_highlight));
        Canvas canvas = new Canvas(highlighter);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(backLight, 0, 0, ptAlphaColor);


        ptAlphaColor.setColor(ContextCompat.getColor(this, R.color.pressed_highlight));
        canvas = new Canvas(highlighterSmall);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(backLightSmall,
                -offsetXY[0] + offsetXYSmall[0],
                -offsetXY[1] + offsetXYSmall[1], ptAlphaColor);

        canvas = new Canvas(iconOut);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(icon, -offsetXY[0], -offsetXY[1], null);

        backLight.recycle();

        icons.add(iconOut);
        icons.add(highlighterSmall);
        icons.add(highlighter);

        return icons;
    }

    public void changeAppShortcutList(){

    }


}


