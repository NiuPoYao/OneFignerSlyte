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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    private String LOG_TAG = "Main Activity";

    /** Used for interaction with child fragments*/
    public static final int FRG_ACTION_KILL = -1;
    public static final int FRG_ACTION_CHANGE_SHORTCUT = 1;
    public static final int FRG_ACTION_CONFIRM= 2;
    public static final int FRG_ACTION_LISTENER_UPDATE= 3;

    private FragmentManager fm;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private AppShortcutFragment mAppShortcutFragment = new AppShortcutFragment();
    private AppChooserFragment mAppChooserFragment;
    private PackageManager mPm;
    private ArrayList<ResolveInfo> mLaunchableApps;

    private FloatingActionButton fab;

    private ArrayList<Integer> mVacancies = new ArrayList<>();
    private ArrayList<Integer> mCandidates = new ArrayList<>();
    public static List<AppShortInfo> appList;

    String lastTag = "last";

    //public View.OnClickListener mainFabOnClickListener;
    public class AppShortInfo {
        CharSequence name;
        List<Bitmap> icons;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.main_fab);

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        ViewGroup MainView = (ViewGroup) findViewById(R.id.main_content);
        MainView.setBackground(wallpaperDrawable);

        mViewPager = (ViewPager) findViewById(R.id.container);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        fm = getSupportFragmentManager();
        fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                setFabClickListener();
            }
        });

        //setSupportActionBar(toolbar);

        if (fab != null) {
            fab.setOnClickListener(mainFabOnClickListener);
        }



        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        mPm = getPackageManager();
        mLaunchableApps = (ArrayList<ResolveInfo>) mPm.queryIntentActivities(i, 0);

        mLaunchableApps = sortRIsByLabel(mLaunchableApps, mPm);

        setAppList(null, null);

    }

    public ArrayList<ResolveInfo> sortRIsByLabel(ArrayList<ResolveInfo> RIs, PackageManager pm){
        final PackageManager fPm = pm;
        Collections.sort(RIs, new Comparator<ResolveInfo>(){
            public int compare(ResolveInfo emp1, ResolveInfo emp2) {
                return emp1.loadLabel(fPm).toString().compareToIgnoreCase(emp2.loadLabel(fPm).toString());
            }
        });

        return RIs;
    }

    public void setAppList(int pos, ResolveInfo RI){
        AppShortInfo app = appList.get(pos);
        app.name = RI.activityInfo.packageName;
        new getIconSet().execute(app, RI);
    }

    public void setAppList(ArrayList<Integer> vacanciesList, ArrayList<Integer> candidatesList){

        if (appList == null) {
            appList = new ArrayList<>();
        }

        if (vacanciesList == null && candidatesList == null){
            Log.d(LOG_TAG, "retrieving app data");
            for (ResolveInfo RI : mLaunchableApps){
                if (MainActivity.appList.size()>23){
                    break;
                }
                AppShortInfo app = new AppShortInfo();
                appList.add(app);
                setAppList(MainActivity.appList.size()-1,RI);
            }
        }

        if (candidatesList != null && candidatesList.size()>0){
            ResolveInfo RI;
            for (int i = 0; i < candidatesList.size(); i++){
                RI = mLaunchableApps.get(candidatesList.get(i));
                setAppList(vacanciesList.get(i) , RI);
            }
        }
    }


    @Override
    public void onFragmentInteraction(String tag, int action) {
        FragmentManager fm = getSupportFragmentManager();
        if (action == FRG_ACTION_LISTENER_UPDATE){
            setFabClickListener();
        }
        switch (tag){
            case "AppShortcutFragment":
                if (action == FRG_ACTION_KILL){
                    fm.popBackStack();
                    Log.d(LOG_TAG, fm.getBackStackEntryCount() + "");
                    break;
                }
                if (action == FRG_ACTION_CHANGE_SHORTCUT){
                    mAppChooserFragment = new AppChooserFragment();
                    mAppChooserFragment.setLaunchableAppsRI(mLaunchableApps);
                    mAppChooserFragment.setVacancies(mAppShortcutFragment.getSelectedApp());
                    fm.beginTransaction().replace(R.id.main_content, mAppChooserFragment,"AppChooser")
                            .addToBackStack("AppChooser").commit();
                    break;
                }
            case "AppChooserFragment":
                if (action == FRG_ACTION_CONFIRM){
                    this.mVacancies = mAppChooserFragment.getVacancies();
                    this.mCandidates = mAppChooserFragment.getCandidates();
                    setAppList(mVacancies, mCandidates);
                    fm.beginTransaction()
                            .replace(R.id.main_content, mAppShortcutFragment, "appShortcut")
                            .addToBackStack("appShortcut")
                            .commit();
                    mVacancies = null;
                    mCandidates = null;
                    break;
                }
        }

        Log.d(LOG_TAG, "fragment send:" + tag);


    }

    @Override
    public void onPause(){
        super.onPause();
    }

    private View.OnClickListener mainFabOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            FragmentManager fm = getSupportFragmentManager();
            if (!mAppShortcutFragment.isAdded()){
                fm.beginTransaction()
                        .add(R.id.main_content, mAppShortcutFragment, "appShortcut")
                        .addToBackStack("appShortcut")
                        .commit();
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


    public class getIconSet extends AsyncTask<Object, Void, Void> {

        private AppShortInfo appInfo;
        private ResolveInfo ri;

        @Override
        protected Void doInBackground(final Object... param){
            appInfo = (AppShortInfo) param[0];
            ri = (ResolveInfo) param[1];
            Drawable icon = ri.loadIcon(mPm);
            appInfo.icons = highlightImage(((BitmapDrawable) icon).getBitmap());
            return null;
        }


    }

    public List<Bitmap> highlightImage(Bitmap icon) {

        List<Bitmap> icons = new ArrayList<>();
        Paint ptBlur = new Paint();

        ptBlur.setMaskFilter(new BlurMaskFilter(25, BlurMaskFilter.Blur.OUTER));
        int[] offsetXY = new int[2];
        Bitmap backLight = icon.extractAlpha(ptBlur, offsetXY);

        Paint ptAlphaColor = new Paint();

        Bitmap iconOut = Bitmap.createBitmap(backLight.getWidth() , backLight.getHeight() , Bitmap.Config.ARGB_8888);
        Bitmap highlightWhite = Bitmap.createBitmap(backLight.getWidth() , backLight.getHeight() ,   Bitmap.Config.ARGB_8888);
        Bitmap highlightYellow = Bitmap.createBitmap(backLight.getWidth() , backLight.getHeight() ,   Bitmap.Config.ARGB_8888);

        ptAlphaColor.setColor(ContextCompat.getColor(this, R.color.long_pressed_highlight));
        Canvas canvas = new Canvas(highlightYellow);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(backLight, 0, 0, ptAlphaColor);

        ptAlphaColor.setColor(ContextCompat.getColor(this, R.color.pressed_highlight));
        canvas = new Canvas(highlightWhite);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(backLight, 0, 0, ptAlphaColor);

        canvas = new Canvas(iconOut);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(icon, -offsetXY[0], -offsetXY[1], null);

        backLight.recycle();

        icons.add(icon);
        icons.add(iconOut);
        icons.add(highlightWhite);
        icons.add(highlightYellow);

        return icons;
    }

    public void setFabClickListener(){
        Fragment frg = getCurrentFragment();
        if (frg == null){
            Log.d(LOG_TAG,"frg is null");
            fab.setOnClickListener(this.mainFabOnClickListener);
            if ( !fab.isShown() ){fab.show();}
            return;
        }
        if (frg instanceof ListenerHolder){
           View.OnClickListener listener = ((ListenerHolder) frg).getClickListener();
            if (listener != null){
                fab.setOnClickListener(listener);
                if ( !fab.isShown() ){fab.show();}
                return;
            }
        }
        fab.hide();
    }

    public Fragment getCurrentFragment(){

        if (fm.getBackStackEntryCount() == 0){ return null;}
        //Log.d(LOG_TAG,fm.getBackStackEntryAt(fm.getBackStackEntryCount()-1).getName());
        return fm.findFragmentByTag(fm.getBackStackEntryAt(fm.getBackStackEntryCount()-1).getName());

    }


    public interface ListenerHolder{
        public View.OnClickListener getClickListener();
    }
}


