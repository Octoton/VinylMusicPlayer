package com.poupa.vinylmusicplayer.upnp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.dialogs.BottomSheetDialog.BottomSheetDialog;
import com.poupa.vinylmusicplayer.upnp.controller.UpnpDevice;


public class UpnpBottomSheetDialogFragment extends BottomSheetDialog {
   public static UpnpBottomSheetDialogFragment newInstance() { return new UpnpBottomSheetDialogFragment(); }

   private CheckedTextView localDevice;
   private ListView listView;
   private ArrayAdapter<UpnpDevice> adapter;

   // for testing
   public UpnpManager upnpManager;
   private boolean setup = false;

   private Handler handler;
   private Runnable runnable;


   @Override
   protected void onShowInternal(DialogInterface dialog) {
      com.google.android.material.bottomsheet.BottomSheetDialog d = (com.google.android.material.bottomsheet.BottomSheetDialog) dialog;

      BottomSheetBehavior behaviour = BottomSheetBehavior.from(bottomSheet);
      behaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);

      localDevice = (CheckedTextView) d.findViewById(R.id.local);
      localDevice.setChecked(true);

      listView = (ListView) d.findViewById(R.id.list_view);
      listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
      adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice); //simple_list_item_1);
      listView.setAdapter(adapter);

      handler = new Handler();

      if (upnpManager.getRendererCommand() != null &&
              upnpManager.getRendererCommand().getRegistryListener() != null &&
              upnpManager.getRendererCommand().getRegistryListener().getSelectedRenderer() != null) {

         adapter.add(upnpManager.getRendererCommand().getRegistryListener().getSelectedRenderer());
         listView.setItemChecked(0, true);
         localDevice.setChecked(false);

      }

      runnable = new Runnable() {
         public void run() {
            handler.post(new Runnable() {
               @Override
               public void run () {
                  // make operation on the UI
                  Log.d("TOTO_setup", "handler bootomsheet");
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

      localDevice.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            //dismiss();
            setup = false;
            //upnpManager.stop(); ??? or just stop upnp connection

            localDevice.setChecked(true);
            for (int i = 0; i < listView.getCount(); i++)
               listView.setItemChecked(i, false);
         }
      });
      listView.setOnItemClickListener(new OnItemClickListener(){
         @Override
         public void onItemClick(AdapterView<?> adapter, View v, int position, long id){
            UpnpDevice item = (UpnpDevice)adapter.getItemAtPosition(position);

            listView.setItemChecked(position, true);
            localDevice.setChecked(false);

            setup = true;
            upnpManager.setup_upnp_connection(item);
         }
      });
   }

   @NonNull
   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState) {
      upnpManager = UpnpManager.getInstance();
      upnpManager.setup(getActivity());

      return super.onCreateDialog(savedInstanceState);
   }

   @Override
   public void onCancel(DialogInterface dialog)
   {
      if (!setup) {
         if (upnpManager.getRendererCommand() != null)
            upnpManager.getRendererCommand().pause();
         upnpManager.stop();
      }

      handler.removeCallbacks(runnable);
      super.onCancel(dialog);
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
