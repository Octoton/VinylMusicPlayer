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

package com.poupa.vinylmusicplayer.upnp.controller;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.types.UDAServiceType;


@SuppressWarnings("rawtypes")
public class UpnpDevice {

    private static final String TAG = "TOTO_ClingDevice";

    Device device;

    public UpnpDevice(Device device)
    {
        this.device = device;
    }

    public Device getDevice()
    {
        return device;
    }

    public String getDisplayString()
    {
        return device.getDisplayString();
    }

    public String getFriendlyName()
    {
        return (device.getDetails() != null && device.getDetails().getFriendlyName() != null) ? device.getDetails()
                .getFriendlyName() : getDisplayString();
    }

    public boolean equals(UpnpDevice otherDevice)
    {
        if (this == otherDevice)
            return true;
        return device.getIdentity().getUdn().equals(otherDevice.getDevice().getIdentity().getUdn());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UpnpDevice that = (UpnpDevice) o;
        return this.equals(that);
    }

    @Override
    public int hashCode()
    {
        if (device == null)
            return 0;

        return device.hashCode();
    }

    @Override
    public String toString()
    {
        if (device == null)
            return "";

        return getFriendlyName();
    }

    /*public String getUID()
    {
        return device.getIdentity().getUdn().toString();
    }

    public String getExtendedInformation()
    {
        String info = "";
        if (device.findServiceTypes() != null)
            for (ServiceType cap : device.findServiceTypes())
            {
                info += "\n\t" + cap.getType() + " : " + cap.toFriendlyString();
            }
        ;
        return info;
    }

    public void printService()
    {
        Service[] services = device.findServices();
        for (Service service : services)
        {
            Log.i(TAG, "\t Service : " + service);
            for (Action a : service.getActions())
            {
                Log.i(TAG, "\t\t Action : " + a);
            }
        }
    }*/

    public boolean asService(String service)
    {
        return (device.findService(new UDAServiceType(service)) != null);
    }

    /*public String getManufacturer()
    {
        return device.getDetails().getManufacturerDetails().getManufacturer();
    }

    public String getManufacturerURL()
    {
        try {
            return device.getDetails().getManufacturerDetails().getManufacturerURI().toString();
        } catch (Exception e) {
            return "";
        }
    }

    public String getModelName()
    {
        try {
            return device.getDetails().getModelDetails().getModelName();
        } catch (Exception e) {
            return "";
        }
    }

    public String getModelDesc()
    {
        try {
            return device.getDetails().getModelDetails().getModelDescription();
        } catch (Exception e) {
            return "";
        }
    }

    public String getModelNumber()
    {
        try {
            return device.getDetails().getModelDetails().getModelNumber();
        } catch (Exception e) {
            return "";
        }
    }

    public String getModelURL()
    {
        try {
            return device.getDetails().getModelDetails().getModelURI().toString();
        } catch (Exception e) {
            return "";
        }
    }

    public String getXMLURL()
    {
        try {
            return device.getDetails().getBaseURL().toString();
        } catch (Exception e) {
            return "";
        }
    }

    public String getPresentationURL()
    {
        try {
            return device.getDetails().getPresentationURI().toString();
        } catch (Exception e) {
            return "";
        }
    }

    public String getSerialNumber()
    {
        try {
            return device.getDetails().getSerialNumber();
        } catch (Exception e) {
            return "";
        }
    }

    public String getUDN()
    {
        try {
            return device.getIdentity().getUdn().toString();
        } catch (Exception e) {
            return "";
        }
    }*/

    public boolean isFullyHydrated()
    {
        return device.isFullyHydrated();
    }
}
