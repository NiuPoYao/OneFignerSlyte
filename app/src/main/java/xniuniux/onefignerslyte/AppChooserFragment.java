package xniuniux.onefignerslyte;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class AppChooserFragment extends Fragment implements MainActivity.ListenerHolder{

    private String LOG_TAG = "AppChooserFragment";

    private ArrayList<Integer> mCandidates = new ArrayList<>();
    private ArrayList<Integer> mVacancies = new ArrayList<>();
    private OnFragmentInteractionListener mInteractionListener;
    private View.OnClickListener chooserFabOnClickListener;

    private Context mContext;
    private GridLayout mCandidatesLayout;
    private PackageManager mPm;
    private List<ResolveInfo> mLaunchableAppsRI;

    public AppChooserFragment() {
        // Required empty public constructor
    }


    public void setLaunchableAppsRI(ArrayList<ResolveInfo> list){
        this.mLaunchableAppsRI = list;
    }

    public void setVacancies(ArrayList<Integer> list){
        this.mVacancies = list;
    }

    public ArrayList<Integer> getVacancies(){
        return this.mVacancies;
    }

    public ArrayList<Integer> getCandidates(){
        return this.mCandidates;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mPm = context.getPackageManager();
        if (context instanceof OnFragmentInteractionListener) {
            mInteractionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_app_chooser, container, false);

        mCandidatesLayout = (GridLayout) rootView.findViewById(R.id.app_chooser_candidate);

        for (int i = 0; i < mVacancies.size(); i++){
            int pos = mVacancies.get(i);
            ImageView superfluous =(ImageView) inflater.inflate(R.layout.element_app_shortcut, null);
            MainActivity.AppShortInfo app = MainActivity.appList.get(pos);
            superfluous.setTag(pos);
            superfluous.setImageBitmap(app.icons.get(0));
            superfluous.setVisibility(View.VISIBLE);
            addToCandidates(superfluous,0,i);

        }

        chooserFabOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInteractionListener.onFragmentInteraction(LOG_TAG, MainActivity.FRG_ACTION_CONFIRM);
            }
        };

        GridView appList = (GridView) rootView.findViewById(R.id.app_chooser_grid_list);

        appList.setAdapter(new appAdapter(mContext, mLaunchableAppsRI));
        appList.setOnItemClickListener(mAppClickListener);

        return rootView;
    }

    private AdapterView.OnItemClickListener mAppClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
            GridView gv = (GridView) adapterView;
            if ( mCandidates.size() < mVacancies.size()){
                mCandidates.add(pos);
                ImageView iv = (ImageView) LayoutInflater.from(mContext).inflate(R.layout.element_app_shortcut,null);
                iv.setImageDrawable(((ImageView) view.findViewById(R.id.app_list_image)) .getDrawable());
                addToCandidates(iv,1,mCandidates.size()-1);
            } else {
                int col = mCandidates.indexOf(pos);
                removeCandidates(1,col);
            }
        }
    };

    public void addToCandidates(ImageView view, int row, int col){
        mCandidatesLayout.addView(view);
        GridLayout.LayoutParams params = (GridLayout.LayoutParams) view.getLayoutParams();
        Log.d(LOG_TAG,"add to candidate: " + params.toString());
        params.setGravity(Gravity.FILL);
        params.rowSpec = GridLayout.spec(row);
        params.columnSpec = GridLayout.spec(col);
        view.setVisibility(View.VISIBLE);
    }

    public void removeCandidates(int row, int col){
        //mCandidatesLayout.re
        Log.d(LOG_TAG,"remove candidate: " + row + ", " + col);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mInteractionListener = null;
    }


    public class appAdapter extends BaseAdapter{
        private Context mContext;
        private List<ResolveInfo> mRIs;

        public appAdapter(Context context, List<ResolveInfo> list){
            mContext = context;
            mRIs = list;
        }

        public int getCount() {
            if (mRIs != null) {
                return mRIs.size();
            }
            return 0;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }


        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {

            ImageView button;
            TextView textView;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.element_app_list, null);

            }

            button = (ImageView) convertView.findViewById(R.id.app_list_image);
            textView = (TextView) convertView.findViewById(R.id.app_list_text);

            new getItem().execute(button, textView, mRIs.get(position));

            convertView.setTag(position);

            return convertView;
        }


    }

    public class getItem extends AsyncTask<Object, Void, Drawable>{

        private ImageView button;
        private TextView textView;
        private ResolveInfo ri;

        @Override
        protected Drawable doInBackground(final Object... param){
            button  = (ImageView) param[0];
            textView = (TextView) param[1];
            ri = (ResolveInfo) param[2];
            return ri.loadIcon(mPm);

        }

        @Override
        protected void onPostExecute(Drawable icon){
            super.onPostExecute(icon);
            button.setImageDrawable(icon);
            textView.setText(ri.loadLabel(mPm));
        }
    }

    @Override
    public View.OnClickListener getClickListener() {
        return chooserFabOnClickListener;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     **/
}
