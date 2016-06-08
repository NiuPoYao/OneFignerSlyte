package xniuniux.onefignerslyte;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PagerFragmentWeather#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PagerFragmentWeather extends Fragment {

    private static final String ARG_LOCATION = "location";
    private static final String ARG_UNIT = "unit";

    private String mLocation;
    private int mUnit;


    public PagerFragmentWeather() {
        // Required empty public constructor
    }

    public static PagerFragmentWeather newInstance(String location, int unit) {
        PagerFragmentWeather fragment = new PagerFragmentWeather();
        Bundle args = new Bundle();
        args.putString(ARG_LOCATION, location);
        args.putInt(ARG_UNIT, unit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLocation = getArguments().getString(ARG_LOCATION);
            mUnit = getArguments().getInt(ARG_UNIT);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_section, container, false);


        return rootView;
    }




}
