package xniuniux.onefignerslyte;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * {@link AppChooserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class AppChooserFragment extends Fragment {

    private String LOG_TAG = "AppChooserFragment";

    private int mCandidateNum=0;

    private FloatingActionButton mConfirmFab;
    private OnFragmentInteractionListener mListener;

    private Context mContext;
    private PackageManager mPm;
    private List<ResolveInfo> mLaunchableAppsRI;

    public AppChooserFragment() {
        // Required empty public constructor
    }


    public void setLaunchableAppsRI(ArrayList<ResolveInfo> list){
        this.mLaunchableAppsRI = list;
    }

    public void setCandidateNum(int cn){
        this.mCandidateNum = cn;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mPm = context.getPackageManager();
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_app_chooser, container, false);

        GridView appList = (GridView) rootView.findViewById(R.id.app_chooser_grid_list);

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        appList.setAdapter(new appAdapter(mContext, mLaunchableAppsRI));

        mConfirmFab = (FloatingActionButton) rootView.findViewById(R.id.fab_confirm);
        mConfirmFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onFragmentInteraction(LOG_TAG, MainActivity.FRG_ACTION_CONFIRM);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public class appAdapter extends BaseAdapter{
        private Context mContext;
        private List<ResolveInfo> mRIs;

        public appAdapter(Context context, List<ResolveInfo> list){
            mContext = context;
            mRIs = list;
        }

        public int getCount() {
            return mRIs.size();
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
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String tag, int action);
    }

}
