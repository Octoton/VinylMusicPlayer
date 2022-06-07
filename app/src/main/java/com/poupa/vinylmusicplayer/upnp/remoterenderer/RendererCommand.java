package com.poupa.vinylmusicplayer.upnp.remoterenderer;


import android.util.Log;

import com.poupa.vinylmusicplayer.upnp.controller.RegistryListener;
import com.poupa.vinylmusicplayer.upnp.controller.UpnpDevice;
import com.poupa.vinylmusicplayer.upnp.localserver.MediaServer;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.avtransport.callback.Pause;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume;
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume;


@SuppressWarnings("rawtypes")
public class RendererCommand implements Runnable {

   private static final String TAG = "TOTO_RendererCommand";

   private RegistryListener registryListener;
   private RendererState rendererState;
   private ControlPoint controlPoint;

   public Thread thread;
   boolean pause;

   public RendererCommand()
   {
      registryListener = new RegistryListener();

      pause = true;
   }

   @Override
   public void finalize()
   {
      this.pause();
   }

   public void pause()
   {
      Log.v(TAG, "Interrupt");
      pause = true;
      if (thread != null)
         thread.interrupt();
   }

   public void setup(ControlPoint controlPoint, RendererState rendererState) {
      Log.v(TAG, "Resume");

      this.rendererState = rendererState;
      this.controlPoint = controlPoint;
   }

   public void resume() {
      if (thread == null)
         thread = new Thread(this);

      pause = false;
      if (!thread.isAlive())
         thread.start();
      else
         thread.interrupt();
   }

   public boolean filter(UpnpDevice device) {
      return registryListener.filter(device);
   }

   public void setSelectedRenderer(UpnpDevice renderer, boolean force) {
      registryListener.setSelectedRenderer(renderer, force);
   }

   public void setSelectedContentDirectory(UpnpDevice contentDirectory, boolean force) {
      registryListener.setSelectedContentDirectory(contentDirectory, force);
   }

   public RegistryListener getRegistryListener() {
      return registryListener;
   }

   private Service getRenderingControlService()
   {
      if (registryListener.getSelectedRenderer() == null)
         return null;

      return ((UpnpDevice) registryListener.getSelectedRenderer()).getDevice().findService(
              new UDAServiceType("RenderingControl"));
   }

   private Service getAVTransportService()
   {
      if (registryListener.getSelectedRenderer() == null)
         return null;

      return ((UpnpDevice) registryListener.getSelectedRenderer()).getDevice().findService(
              new UDAServiceType("AVTransport"));
   }

   public void commandStop()
   {
      if (getAVTransportService() == null)
         return;

      controlPoint.execute(new Stop(getAVTransportService()) {
         @Override
         public void success(ActionInvocation invocation)
         {
            Log.v(TAG, "Success stopping ! ");
            // TODO update player state
         }

         @Override
         public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
         {
            Log.w(TAG, "Fail to stop ! " + arg2);
         }
      });
   }

   public void commandPlay()
   {
      if (getAVTransportService() == null)
         return;

      controlPoint.execute(new Play(getAVTransportService()) {
         @Override
         public void success(ActionInvocation invocation)
         {
            Log.v(TAG, "Success playing ! ");
            // TODO update player state
         }

         @Override
         public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
         {
            Log.w(TAG, "Fail to play ! " + arg2);
         }
      });
   }

   public void commandPause()
   {
      if (getAVTransportService() == null)
         return;

      controlPoint.execute(new Pause(getAVTransportService()) {
         @Override
         public void success(ActionInvocation invocation)
         {
            Log.v(TAG, "Success pausing ! ");
            // TODO update player state
         }

         @Override
         public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
         {
            Log.w(TAG, "Fail to pause ! " + arg2);
         }
      });
   }

   public void setVolume(final int volume)
   {
      if (getRenderingControlService() == null)
         return;

      controlPoint.execute(new SetVolume(getRenderingControlService(), volume) {
         @Override
         public void success(ActionInvocation invocation)
         {
            super.success(invocation);
            Log.v(TAG, "Success to set volume");
            rendererState.setVolume(volume);
         }

         @Override
         public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
         {
            Log.w(TAG, "Fail to set volume ! " + arg2);
         }
      });
   }

   public void updateVolume()
   {
      if (getRenderingControlService() == null)
         return;

      controlPoint.execute(new GetVolume(getRenderingControlService()) {
         @Override
         public void received(ActionInvocation arg0, int arg1)
         {
            Log.d(TAG, "Receive volume ! " + arg1);
            rendererState.setVolume(arg1);
         }

         @Override
         public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
         {
            Log.w(TAG, "Fail to get volume ! " + arg2);
         }
      });
   }

   public int getVolume()
   {
      return rendererState.getVolume();
   }

   private void setURI(String uri, TrackMetadata trackMetadata)
   {
      Log.i(TAG, "Set uri to " + uri);

      controlPoint.execute(new SetAVTransportURI(getAVTransportService(), uri, trackMetadata.getXML()) {

         @Override
         public void success(ActionInvocation invocation)
         {
            super.success(invocation);
            Log.i(TAG, "URI successfully set !");
            commandPlay();
         }

         @Override
         public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
         {
            Log.w(TAG, "Fail to set URI ! " + arg2);
         }
      });
   }

   // launchItem(upnpItem.id, upnpItem.title, upnpItem.artist, "", "", upnpItem.getFirstResource().getValue(), item.getURI());
   public void launchItem(String id, String title, String artist, String genre, String artURI, String res, String uri)
   {
      if (getAVTransportService() == null)
         return;

      // TODO genre && artURI
      final TrackMetadata trackMetadata = new TrackMetadata(id, title,
              artist, genre, artURI, res,
              "object.item." + "audioItem");

      Log.i(TAG, "TrackMetadata : "+trackMetadata.toString());

      // Stop playback before setting URI
      controlPoint.execute(new Stop(getAVTransportService()) {
         @Override
         public void success(ActionInvocation invocation)
         {
            Log.v(TAG, "Success stopping ! ");
            callback();
         }

         @Override
         public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
         {
            Log.w(TAG, "Fail to stop ! " + arg2);
            callback();
         }

         public void callback()
         {
            setURI(uri, trackMetadata);
         }
      });

   }

   @Override
   public void run()
   {
      while (true) {
         try {
            int count = 0;
            while (true) {
               if (!pause) {
                  if (registryListener != null && registryListener.getSelectedRenderer() != null)
                     Log.d(TAG, "Update state: "+registryListener.getSelectedRenderer().getFriendlyName());
                  else
                     Log.d(TAG, "Update state !");


                  count++;

                  //updatePositionInfo();

                  if ((count % 3) == 0) {
                     updateVolume();
                     //updateMute();
                     //updateTransportInfo();
                  }

                  /*if ((count % 6) == 0)
                  {
                     updateMediaInfo();
                  } */
               }
               Thread.sleep(1000);
            }
         } catch (InterruptedException e) {
            Log.i(TAG, "State updater interrupt, new state " + ((pause) ? "pause" : "running"));
         }
      }
   }
}
