package com.poupa.vinylmusicplayer.upnp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.upnp.controller.UpnpDevice;


public class UpnpBottomSheetDialogFragment extends BottomSheetDialogFragment {
   public static UpnpBottomSheetDialogFragment newInstance() { return new UpnpBottomSheetDialogFragment(); }

   private ListView listView;
   private ArrayAdapter<UpnpDevice> adapter;

   // for testing
   public UpnpManager upnpManager;
   private boolean setup = false;

   private Handler handler;
   private Runnable runnable;

   @NonNull
   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState) {

      BottomSheetDialog dialog = new BottomSheetDialog(getActivity());

      upnpManager = new UpnpManager(getActivity());
      upnpManager.setup(getActivity());

      dialog.setOnShowListener(new DialogInterface.OnShowListener() {
         @Override
         public void onShow(DialogInterface dialog) {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            BottomSheetBehavior behaviour = BottomSheetBehavior.from(bottomSheet);
            behaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);

            listView = (ListView) d.findViewById(R.id.list_view);
            adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
            listView.setAdapter(adapter);

            //adapter.add("Pixel 4A (local)");

            handler = new Handler();

            runnable = new Runnable() {
               public void run() {
                  handler.post(new Runnable() {
                     @Override
                     public void run () {
                        // make operation on the UI
                        Collection<UpnpDevice> returnedList = upnpManager.getFilteredDeviceList(); //getUpnpDevices();

                        for (UpnpDevice el : returnedList) {
                           int position = adapter.getPosition(el);

                           if (position < 0)
                              adapter.add(el);
                        }
                     }
                  });

                  handler.postDelayed(this, 1000);
               }
            };

            handler.postDelayed(runnable, 1000);

            ((TextView) d.findViewById(R.id.local)).setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                  dismiss();
               }
            });
         }
      });

      return dialog;
   }

   @Override
   public void dismiss() {
      if (!setup) {
         if (upnpManager.getRendererCommand() != null)
            upnpManager.getRendererCommand().pause();
         upnpManager.stop();
      }

      handler.removeCallbacks(runnable);
      super.dismiss();
   }

   @Nullable
   @Override
   public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.upnp_search_bottom_sheet_dialog, container, false);

      int accentColor = ThemeStore.accentColor(getContext());

      TextView title = view.findViewById(R.id.setting_title);
      title.setTextColor(accentColor);

      return view;
   }

}
