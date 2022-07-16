package com.poupa.vinylmusicplayer.upnp.remoterenderer;


import java.util.Map;

import android.media.MediaPlayer;
import android.util.Log;

import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.upnp.UpnpPlayer;
import com.poupa.vinylmusicplayer.upnp.controller.RegistryListener;
import com.poupa.vinylmusicplayer.upnp.controller.UpnpDevice;
import com.poupa.vinylmusicplayer.upnp.localserver.MediaServer;
import com.poupa.vinylmusicplayer.upnp.remoterenderer.RendererState.State;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo;
import org.fourthline.cling.support.avtransport.callback.GetPositionInfo;
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo;
import org.fourthline.cling.support.avtransport.callback.Pause;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.Seek;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume;
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

@SuppressWarnings("rawtypes")
public class RendererCommand implements Runnable {

   private static final String TAG = "TOTO_RendererCommand";

   private RegistryListener registryListener;
   private RendererState rendererState;
   private ControlPoint controlPoint;

   private MediaPlayer.OnCompletionListener callback;
   private String currentURI;
   private boolean onCompletion = false;

   public Thread thread;
   boolean pause;

   public RendererCommand()
   {
      registryListener = new RegistryListener();

      pause = true;
   }

   public RendererState getRendererState() {
      return rendererState;
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
            updateFull();
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

   public void commandSeek(String relativeTimeTarget)
   {
      if (getAVTransportService() == null)
         return;


      Log.d(TAG, "Try seeking: "+relativeTimeTarget);

      controlPoint.execute(new Seek(getAVTransportService(), relativeTimeTarget) {
         // TODO fix it, what is relativeTimeTarget ? :)

         @Override
         public void success(ActionInvocation invocation)
         {
            Log.v(TAG, "Success seeking !");
            // TODO update player state
         }

         @Override
         public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
         {
            Log.w(TAG, "Fail to seek ! " + arg2);
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
   public void launchItem(Song song, String uri) //String id, String title, String artist, String genre, String artURI, String res, String uri)
   {
      if (getAVTransportService() == null)
         return;

      // TODO: put song inside TrackMetadata
      final TrackMetadata trackMetadata = new TrackMetadata(Long.toString(song.id), song.title,
              song.artistNames.get(0), song.genre, null, "object.item." + "audioItem", 0, MusicUtil.getReadableHoursDurationString(song.duration));

      Log.i(TAG, "TrackMetadata : "+trackMetadata.getXML());

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

   public void updateTransportInfo()
   {
      if (getAVTransportService() == null)
         return;

      controlPoint.execute(new GetTransportInfo(getAVTransportService()) {
         @Override
         public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
         {
            Log.w(TAG, "Fail to get transport info ! " + arg2);
         }

         @Override
         public void received(ActionInvocation arg0, TransportInfo arg1)
         {
            Log.d(TAG, "Receive transport info ! " + arg1);
            rendererState.setTransportInfo(arg1);
         }
      });
   }

   public void updateMediaInfo()
   {
      if (getAVTransportService() == null)
         return;

      controlPoint.execute(new GetMediaInfo(getAVTransportService()) {
         @Override
         public void received(ActionInvocation arg0, MediaInfo arg1)
         {
            Log.d(TAG, "Receive media info ! " + arg1);
            Log.d(TAG, "Receive media info hash code " + arg1.hashCode());
            if (rendererState.getMediaInfo() != null)
               Log.d(TAG, "Current media info hash code " + rendererState.getMediaInfo().hashCode());
            rendererState.setMediaInfo(arg1);
         }

         @Override
         public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
         {
            Log.w(TAG, "Fail to get media info ! " + arg2);
         }
      });
   }

   public void updatePositionInfo()
   {
      if (getAVTransportService() == null)
         return;

      Log.d(TAG, "Send position info !");

      controlPoint.execute(new GetPositionInfo(getAVTransportService()) {
         @Override
         public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
         {
            Log.w(TAG, "Fail to get position info ! " + arg2);
         }

         @Override
         public void received(ActionInvocation arg0, PositionInfo arg1)
         {
            Log.d(TAG, "Receive position info ! " + arg1.getRelTime());
            rendererState.setPositionInfo(arg1);
         }

         @Override
         public void success(ActionInvocation invocation) {
            //PositionInfo positionInfo = new PositionInfo(invocation.getOutputMap());
            String str = "";
            for (ActionArgument el : invocation.getAction().getArguments()) {
               str = str.concat(el.getName());
               str = str.concat(", ");
            }
            Log.d(TAG, "Success position info !! "+ str);
            //super.success(invocation);
            Map<String, ActionArgumentValue> args = invocation.getOutputMap();
            long track = 0l;//(long) args.get("Track").getValue();
            String trackDuration = (String) args.get("TrackDuration").getValue();
            String trackMetaData = (String) args.get("TrackMetaData").getValue();
            String trackURI = (String) args.get("TrackURI").getValue();
            String relTime = (String) args.get("RelTime").getValue();
            String absTime = (String) args.get("AbsTime").getValue();
            int relCount = (Integer) args.get("RelCount").getValue();
            int absCount = 0;//(Integer) args.get("AbsCount").getValue();
            PositionInfo positionInfo = new PositionInfo(track, trackDuration, trackMetaData, trackURI, relTime, absTime, relCount, absCount);
            Log.d(TAG, "Receive manual position info ! " + positionInfo);

            rendererState.setPositionInfo(positionInfo);
         }
         /*@Override
         protected void failure(ActionInvocation invocation, UpnpResponse operation) {
            Log.d(TAG, "failure position !");
            super.failure(invocation, operation);
         }
         @Override
         protected String createDefaultFailureMessage(ActionInvocation invocation, UpnpResponse operation) {
            Log.d(TAG, "failure msg !");
            return super.createDefaultFailureMessage(invocation, operation);
         }*/
         /*@Override
         public void run() {
            Log.d(TAG, "run !");
            super.run();
         }*/
      });
   }

   public void updateFull()
   {
      updateMediaInfo();
      updatePositionInfo();
      updateVolume();
      //updateMute();
      updateTransportInfo();
   }

   public void setOnCompletion(MediaPlayer.OnCompletionListener callback, String currentURI) {
      this.callback = callback;
      this.currentURI = currentURI;
   }

   @Override
   public void run() //TODO: kill thread when app is killed, add callback to mediaplayer fragment to change state of play when needed
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

                  if (getRendererState().getState() == State.PLAY)
                     updatePositionInfo();

                  if (!onCompletion && callback != null && rendererState.getMediaInfo() != null && currentURI.equals(rendererState.getMediaInfo().getCurrentURI()) &&
                          rendererState.getElapsedPercent() >= 98) { //TODO: ne permet pas le skip track
                     onCompletion = true;
                     callback.onCompletion(null);
                  }

                  if (onCompletion && callback != null && rendererState.getMediaInfo() != null && !currentURI.equals(rendererState.getMediaInfo().getCurrentURI())){ // &&
                  //rendererState.getElapsedPercent() < 98) {
                     onCompletion = false;
                  }

                  Log.d(TAG, "ON COMPLETION: "+onCompletion+", time: "+rendererState.getElapsedPercent());
                  if (rendererState.getMediaInfo() != null) {
                     Log.d(TAG, "INTERNAL URI: "+currentURI);
                     Log.d(TAG, "EXTERNAL URI: "+rendererState.getMediaInfo().getCurrentURI());
                  }

                  if ((count % 3) == 0) {
                     //updateMute();
                     updateVolume();
                     updateTransportInfo();
                  }

                  if ((count % 6) == 0)
                  {
                     updateMediaInfo();
                  }
               }
               Thread.sleep(1000);
            }
         } catch (InterruptedException e) {
            Log.i(TAG, "State updater interrupt, new state " + ((pause) ? "pause" : "running"));
         }
      }
   }
}
