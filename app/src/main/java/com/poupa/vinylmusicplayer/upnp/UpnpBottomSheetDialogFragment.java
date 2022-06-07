package com.poupa.vinylmusicplayer.upnp;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.poupa.vinylmusicplayer.R;


public class UpnpBottomSheetDialogFragment extends BottomSheetDialogFragment {
   public static UpnpBottomSheetDialogFragment newInstance() { return new UpnpBottomSheetDialogFragment(); }

   //private ListFragment listFragment;
   private ListView listView;
   private ArrayList<String> listItems=new ArrayList<String>();
   private ArrayAdapter<String> adapter;

   private Handler handler;

   @NonNull
   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState) {

      BottomSheetDialog dialog = new BottomSheetDialog(getActivity());

      dialog.setOnShowListener(new DialogInterface.OnShowListener() {
         @Override
         public void onShow(DialogInterface dialog) {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            BottomSheetBehavior behaviour = BottomSheetBehavior.from(bottomSheet);
            behaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            /*behaviour.setDraggable(false);
            behaviour.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
               @Override public void onStateChanged(@NonNull View bottomSheet, int newState) {
                  if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                     behaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                  }
               }

               @Override
               public void onSlide(@NonNull View bottomSheet, float slideOffset) {

               }
            });*/

            /*listFragment = new ListFragment();

            getChildFragmentManager().beginTransaction()
                    .add(R.id.testFragment, listFragment)
                    .commit();

            listFragment.*/
            listView = (ListView) d.findViewById(R.id.list_view);
            adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, listItems);
            listView.setAdapter(adapter);

            adapter.add("Pixel 4A (local)");

            handler = new Handler();

            final Runnable r = new Runnable() {
               public void run() {
                  handler.post(new Runnable() {
                     @Override
                     public void run () {
                        // make operation on the UI
                        String test = "toto";
                        int position = adapter.getPosition(test);

                        if (position < 0)
                           adapter.add(test);
                     }
                  });

                  handler.postDelayed(this, 1000);
               }
            };

            handler.postDelayed(r, 1000);
         }
      });

      return dialog;
   }


   @Nullable
   @Override
   public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.upnp_search_bottom_sheet_dialog, container, false);

      int accentColor = ThemeStore.accentColor(getContext());

      TextView title = view.findViewById(R.id.setting_title);
      title.setTextColor(accentColor);

      // dismiss();

      return view;
   }

}
