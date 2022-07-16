package com.poupa.vinylmusicplayer.upnp;

/**
 * Copyright (C) 2013 Aurélien Chabot <aurelien@chabot.fr>
 *
 * This file is part of DroidUPNP.
 *
 * DroidUPNP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DroidUPNP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DroidUPNP.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.android.AndroidRouter;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.binding.xml.DeviceDescriptorBinder;
import org.fourthline.cling.binding.xml.RecoveringUDA10DeviceDescriptorBinderImpl;
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder;
import org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderImpl;
import org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderImpl;
import org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.Router;

import android.content.Intent;
import android.util.Log;

public class UpnpService extends AndroidUpnpServiceImpl {

   @Override
   protected AndroidUpnpServiceConfiguration createConfiguration()
   {
      return new AndroidUpnpServiceConfiguration() {

         @Override
         public int getRegistryMaintenanceIntervalMillis()
         {
            return 7000;
         }

         /*@Override
         public DeviceDescriptorBinder getDeviceDescriptorBinderUDA10() {
            // Recommended for best interoperability with broken UPnP stacks!
            return new RecoveringUDA10DeviceDescriptorBinderImpl();
         }*/

         @Override
         public ServiceDescriptorBinder getServiceDescriptorBinderUDA10() {
            return new UDA10ServiceDescriptorBinderImpl(); // UDA10ServiceDescriptorBinderSAXImpl doesnt work with last version of cling library
         }
         /*@Override
         protected DeviceDescriptorBinder createDeviceDescriptorBinderUDA10() {
            return new RecoveringUDA10DeviceDescriptorBinderImpl();
         }

         @Override
         protected ServiceDescriptorBinder createServiceDescriptorBinderUDA10() {
            return new UDA10ServiceDescriptorBinderSAXImpl();
         }*/
      };
   }

   @Override
   public boolean onUnbind(Intent intent)
   {
      Log.d(this.getClass().getName(), "Unbind");
      return super.onUnbind(intent);
   }

   /*@Override
   public void onCreate() {
      super.onCreate();

      upnpService = new UpnpServiceImpl(createConfiguration()) {

         @Override
         protected Router createRouter(ProtocolFactory protocolFactory, Registry registry) {
            return UpnpService.this.createRouter(
                    getConfiguration(),
                    protocolFactory,
                    UpnpService.this
            );
         }

         @Override
         public synchronized void shutdown() {
            // First have to remove the receiver, so Android won't complain about it leaking
            // when the main UI thread exits.
            ((AndroidRouter)getRouter()).unregisterBroadcastReceiver();

            // Now we can concurrently run the Cling shutdown code, without occupying the
            // Android main UI thread. This will complete probably after the main UI thread
            // is done.
            super.shutdown(true);
         }

         @Override
         public DeviceDescriptorBinder getDeviceDescriptorBinderUDA10() {
            // Recommended for best interoperability with broken UPnP stacks!
            return new RecoveringUDA10DeviceDescriptorBinderImpl();
         }

         @Override
         public ServiceDescriptorBinder getServiceDescriptorBinderUDA10() {
            return new UDA10ServiceDescriptorBinderSAXImpl();
         }
         @Override
         protected DeviceDescriptorBinder createDeviceDescriptorBinderUDA10() {
            return new RecoveringUDA10DeviceDescriptorBinderImpl();
         }

         @Override
         protected ServiceDescriptorBinder createServiceDescriptorBinderUDA10() {
            return new UDA10ServiceDescriptorBinderSAXImpl();
         }
      };
   }*/
}

