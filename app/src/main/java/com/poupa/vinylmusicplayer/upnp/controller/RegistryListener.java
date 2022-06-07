package com.poupa.vinylmusicplayer.upnp.controller;

import android.util.Log;


public class RegistryListener {

   private static final String TAG = "TOTO_RegistryListener";

   //  Should not be useful as device doesn't change
   // private /*final*/ ArrayList<IDeviceDiscoveryObserver> observerList;
   //observerList = new ArrayList<IDeviceDiscoveryObserver>();

   //protected Observable rendererObservable;

   protected UpnpDevice renderer;
   protected UpnpDevice contentDirectory;

   public RegistryListener() {
      //rendererObservable = new Observable();
   }

   public void deviceAdded(final UpnpDevice device)
   {
      //Log.v(TAG, "New device detected : " + device.getDisplayString()+", hydrated: "+device.isFullyHydrated()+", call: "+device.getExtendedInformation());

      if (device.isFullyHydrated() && filter(device))
      {
         if (isSelected(device))
         {
            Log.d(TAG, "Reselect device to refresh it");
            select(device);
         }

         //notifyAdded(device);
      }
   }

   public void deviceRemoved(final UpnpDevice device)
   {
      Log.v(TAG, "Device removed : " + device.getFriendlyName());

      if (filter(device))
      {
         if (isSelected(device))
         {
            Log.d(TAG, "Selected device have been removed");
            removed(device);
         }

         //notifyRemoved(device);
      }
   }

   /*  Should not be useful as device doesn't change
   public void notifyAdded(UpnpDevice device)
   {
      for (IDeviceDiscoveryObserver o : observerList)
         o.addedDevice(device);
   }

   public void notifyRemoved(UpnpDevice device)
   {
      for (IDeviceDiscoveryObserver o : observerList)
         o.removedDevice(device);
   }*/

   public boolean filter(UpnpDevice device)
   {
      try
      {
         return device.asService("RenderingControl");
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return false;
   }

   protected boolean isSelected(UpnpDevice device)
   {
      if (renderer != null)
         return device.equals(renderer);

      return false;
   }

   protected void select(UpnpDevice device)
   {
      setSelectedRenderer(device, true);
   }

   protected void removed(UpnpDevice device)
   {
      setSelectedRenderer(null, false);
   }

   public void setSelectedRenderer(UpnpDevice renderer, boolean force) {
      if (!force && renderer != null && this.renderer != null && renderer.equals(this.renderer))
         return;

      if (renderer != null)
         Log.d(TAG, "Set renderer "+renderer.getFriendlyName());

      this.renderer = renderer;
      /*  needed for updating renderer list fragment when network change
      	rendererObservable.setChanged();
		rendererObservable.notifyObservers();
       */
   }

   public void setSelectedContentDirectory(UpnpDevice contentDirectory, boolean force) {
      // Skip if no change and no force
      if (!force && contentDirectory != null && this.contentDirectory != null && this.contentDirectory.equals(contentDirectory))
         return;

      if (contentDirectory != null)
         Log.d(TAG, "Set contentDirectory "+contentDirectory.getFriendlyName());

      this.contentDirectory = contentDirectory;
      //contentDirectoryObservable.notifyAllObservers(); Should not be useful as device doesn't change
   }

   public UpnpDevice getSelectedRenderer() { return renderer; }


    /*public void startControlPoint()
    {
        if (renderer == null)
        {
            if (device != null)
            {
                Log.i(TAG, "Current renderer have been removed");
                device = null;

                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        try {
                            hide();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            return;
        }

        if (device == null || rendererState == null || rendererCommand == null
                || !device.equals(Main.upnpServiceController.getSelectedRenderer()))
        {
            device = Main.upnpServiceController.getSelectedRenderer();

            Log.i(TAG, "Renderer changed !!! " + Main.upnpServiceController.getSelectedRenderer().getDisplayString());

            rendererState = Main.factory.createRendererState();
            rendererCommand = Main.factory.createRendererCommand(rendererState);

            if (rendererState == null || rendererCommand == null)
            {
                Log.e(TAG, "Fail to create renderer command and/or state");
                return;
            }

            rendererCommand.resume();

            rendererState.addObserver(this);
            rendererCommand.updateFull();
        }
        updateRenderer();
    }*/

   /* Should not be useful as device doesn't change
   public void addObserver(IDeviceDiscoveryObserver o)
   {
      observerList.add(o);

      final Collection<UpnpDevice> upnpDevices = getFilteredDeviceList();
      for (UpnpDevice d : upnpDevices)
         o.addedDevice(d);
   }

   public void removeObserver(IDeviceDiscoveryObserver o)
   {
      observerList.remove(o);
   }*/
}
