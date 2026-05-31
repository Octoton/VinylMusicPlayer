package com.poupa.vinylmusicplayer.upnp;


import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.upnp.controller.RegistryListener;
import com.poupa.vinylmusicplayer.upnp.controller.RegistryManager;
import com.poupa.vinylmusicplayer.upnp.controller.UpnpDevice;
import com.poupa.vinylmusicplayer.upnp.localserver.MediaServer;
import com.poupa.vinylmusicplayer.upnp.remoterenderer.RendererCommand;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.upnp.remoterenderer.RendererState;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.model.Res;


public class UpnpManager {
   private static final String TAG = "TOTO_Manager";

   public static final String PREFERENCE_KEY = "upnp_manager_preference";
   public static final String UPNP_MODE = "upnp_mode";

   private final SharedPreferences mPreferences;
   private boolean upnpMode;

   private ArrayList<RegistryListener> waitingListener = new ArrayList<>();
   private AndroidUpnpService upnpService;
   private MediaServer mediaServer;
   private RendererCommand rendererCommand;

   private Activity activity;

   private static UpnpManager ourInstance;
   public static UpnpManager getInstance() {
      if (ourInstance == null) {
         ourInstance = new UpnpManager();
      }
      return ourInstance;
   }

   private UpnpManager() {
      waitingListener = new ArrayList<>();
      mPreferences = App.getStaticContext().getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
      upnpMode = mPreferences.getBoolean(UPNP_MODE, false);
   }

   public RendererCommand getRendererCommand() {
      return rendererCommand;
   }

   public void setup(Activity activity) {
      if (rendererCommand == null)
         rendererCommand = new RendererCommand();

      // rendererCommand.resume();

      this.activity = activity;

      // This will start the UPnP service if it wasn't already started
      Log.d(TAG, "Start upnp service");
      activity.bindService(new Intent(activity, UpnpService.class), serviceConnection,
              Context.BIND_AUTO_CREATE);

      addListener(rendererCommand.getRegistryListener());
   }

   public void setup_upnp_connection(UpnpDevice upnpDevice) {
      Log.d("TOTO_setup", "Begin device connection");
      rendererCommand.setSelectedRenderer(upnpDevice, true);

      rendererCommand.setup(upnpService.getControlPoint(), new RendererState());
      rendererCommand.resume();

      rendererCommand.updateFull();

      updateIsUpnpMode(true);
   }

   public boolean isUpnpMode() { return upnpMode; }

   public void updateIsUpnpMode(boolean isUpnpMode) {
      mPreferences.edit().putBoolean(UPNP_MODE, isUpnpMode).apply();

      this.upnpMode = isUpnpMode;
   }

   public void setOnCompletion(MediaPlayer.OnCompletionListener callback, String currentURI) {
      rendererCommand.setOnCompletion(callback, currentURI);
   }

   public void stop() {
      if (rendererCommand == null)
         return;

      rendererCommand.commandStop();
      rendererCommand = null;
   }

   public void resume(boolean force) { //Activity activity) {
      if (force)
         setup(activity);

      rendererCommand.resume();
   }

   public void pause() {
      if (rendererCommand == null)
         return;

      rendererCommand.pause();

      //activity.unbindService(serviceConnection); //=> FIX: if unbind, then not possible to resume with connection to renderer
      //activity = null;

      removeListener(rendererCommand.getRegistryListener());
   }

   public String getAddress() {
      return mediaServer.getAddress();
   }

   public void sendSong(Song song)
   {
      if (rendererCommand == null)
         return;
      String uri = "http://"+mediaServer.getAddress()+"/"+ MediaServer.AUDIO_PREFIX + song.id;

      rendererCommand.launchItem(song, uri);
   }

   /*public void sendPath(String path) {
      Log.d(TAG, "Send temporary path: "+path);
      if (rendererCommand == null)
         return;

      // testing
      String mimeType = "audio/flac";
      Res test = new Res(mimeType, 2222222l, "00:03:35", 8192l, null);

      Log.d(TAG, "RES: "+test.getProtocolInfo().toString());

      String res = "size=\"22310000\" duration=\"0:03:35.000\" bitrate=\"860000\" resolution=\"640x360\" protocolInfo=\"http-get:*:audio/flac:*";

      rendererCommand.launchItem("todo", "todo", "", "", "", res, path);
   }*/

   public Collection<UpnpDevice> getFilteredDeviceList()
   {
      ArrayList<UpnpDevice> deviceList = new ArrayList<UpnpDevice>();
      try
      {
         if(upnpService != null && upnpService.getRegistry() != null) {
            Log.d("TOTO_setup", "Number: "+upnpService.getRegistry().getDevices().size());
            for (Device device : upnpService.getRegistry().getDevices()) {
               UpnpDevice upnpDevice = new UpnpDevice(device);
               if (rendererCommand.filter(upnpDevice)) {
                  //Log.d("TOTO_setup", "Udn: "+upnpDevice.getDevice().getIdentity().getUdn());
                  Log.d("TOTO_setup", "Name: "+upnpDevice.getFriendlyName());
                  deviceList.add(upnpDevice);
               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return deviceList;
   }

   protected ServiceConnection serviceConnection = new ServiceConnection() {

      @Override
      public void onServiceConnected(ComponentName className, IBinder service)
      {
         Log.d(TAG, "Service connexion");
         upnpService = (AndroidUpnpService) service;

         try
         {
            // Local content directory: not working yet
            if(mediaServer == null)
            {
               mediaServer = new MediaServer(activity, upnpService.getControlPoint());
               mediaServer.start();
            }
            else
            {
               mediaServer.restart();
            }
            upnpService.getRegistry().addDevice(mediaServer.getDevice());

            // test set of directory
            rendererCommand.setSelectedContentDirectory(new UpnpDevice(mediaServer.getDevice()), true);
         }
         catch (UnknownHostException e1)
         {
            Log.e(TAG, "Creating demo device failed");
            Log.e(TAG, "exception", e1);
         }
         catch (ValidationException e2)
         {
            Log.e(TAG, "Creating demo device failed");
            Log.e(TAG, "exception", e2);
         }
         catch (IOException e3)
         {
            Log.e(TAG, "Starting http server failed");
            Log.e(TAG, "exception", e3);
         }

         for (RegistryListener registryListener : waitingListener)
         {
            addListenerSafe(registryListener);
         }

         // Search asynchronously for all devices, they will respond soon
         upnpService.getControlPoint().search();
      }

      @Override
      public void onServiceDisconnected(ComponentName className)
      {
         Log.d(TAG, "Service disconnected");
         upnpService = null;
      }
   };

   public void addListener(RegistryListener registryListener)
   {
      Log.d(TAG, "Add Listener !");
      if (upnpService != null)
         addListenerSafe(registryListener);
      else
         waitingListener.add(registryListener);
   }

   private void addListenerSafe(RegistryListener registryListener)
   {
      assert upnpService != null;
      Log.d(TAG, "Add Listener Safe !");

      // Get ready for future device advertisements
      upnpService.getRegistry().addListener(new RegistryManager(registryListener));

      // Now add all devices to the list we already know about
      for (Device device : upnpService.getRegistry().getDevices())
      {
         registryListener.deviceAdded(new UpnpDevice(device));
      }
   }

   public void removeListener(RegistryListener registryListener)
   {
      Log.d(TAG, "remove listener");
      if (upnpService != null)
         removeListenerSafe(registryListener);
      else
         waitingListener.remove(registryListener);
   }

   private void removeListenerSafe(RegistryListener registryListener)
   {
      assert upnpService != null;
      Log.d(TAG, "remove listener Safe");
      upnpService.getRegistry().removeListener(new RegistryManager(registryListener));
   }

   public void clearListener()
   {
      waitingListener.clear();
   }
}
