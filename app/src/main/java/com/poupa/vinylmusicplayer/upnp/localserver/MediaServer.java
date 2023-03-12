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

package com.poupa.vinylmusicplayer.upnp.localserver;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import com.poupa.vinylmusicplayer.R;
import fi.iki.elonen.nanohttpd.protocols.http.IHTTPSession;
import fi.iki.elonen.nanohttpd.protocols.http.NanoHTTPD;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import fi.iki.elonen.nanohttpd.protocols.http.response.Status;
import fi.iki.elonen.nanohttpd.protocols.http.request.Method;
import fi.iki.elonen.nanohttpd.protocols.http.response.Response;
import fi.iki.elonen.nanohttpd.webserver.SimpleWebServer;

@SuppressWarnings("rawtypes")
public class MediaServer extends SimpleWebServer
{
    private final static String TAG = "TOTO_MediaServer";

    public final static String AUDIO_PREFIX = "a-";

    private UDN udn = null;
    private LocalDevice localDevice = null;
    private Context ctx = null;

    private final static int port = 8090; //8080; //8192; TODO: was already used by droidupnp i suppose, what to do??
    private final InetAddress localAddress;

    public MediaServer(Context ctx, ControlPoint controlPoint) throws ValidationException,  UnknownHostException
    {
        super(null, port, (File)null, true);

        udn = UDN.valueOf(new UUID(0,10).toString());
        this.ctx = ctx;
        this.localAddress = getLocalIpAddress(this.ctx);
        createLocalDevice();

        Log.i(TAG, "Creating media server: "+this.localAddress);
    }

    public void restart()
    {
        Log.d(TAG, "Restart mediaServer");
//		try {
//			stop();
//			createLocalDevice();
//			start();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
    }

    public void createLocalDevice() throws ValidationException
    {
        String version = "";
        try {
            version = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Application version name not found");
        }

        DeviceDetails details = new DeviceDetails(
                android.os.Build.MODEL,
                new ManufacturerDetails(ctx.getString(R.string.app_name), ctx.getString(R.string.app_url)),
                new ModelDetails(ctx.getString(R.string.app_name), ctx.getString(R.string.app_url)),
                ctx.getString(R.string.app_name), version);

        List<ValidationError> l = details.validate();
        for( ValidationError v : l )
        {
            Log.e(TAG, "Validation pb for property "+ v.getPropertyName());
            Log.e(TAG, "Error is " + v.getMessage());
        }


        DeviceType type = new UDADeviceType("MediaServer", 1);

        localDevice = new LocalDevice(new DeviceIdentity(udn), type, details, (LocalService) null);
    }


    public LocalDevice getDevice() {
        return localDevice;
    }

    public String getAddress() {
        return localAddress.getHostAddress() + ":" + port;
    }

    @SuppressLint("DefaultLocale")
    private InetAddress getLocalIpAddress(Context ctx) throws UnknownHostException
    {
        WifiManager wifiManager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        if(ipAddress!=0)
            return InetAddress.getByName(String.format("%d.%d.%d.%d",
                    (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff)));

        Log.d(TAG, "No ip address available throught wifi manager, try to get it manually");

        InetAddress inetAddress;

        inetAddress = getLocalIpAddressFromInterfaceName("wlan0");
        if(inetAddress!=null)
        {
            Log.d(TAG, "Got an ip for interface wlan0");
            return inetAddress;
        }

        inetAddress = getLocalIpAddressFromInterfaceName("usb0");
        if(inetAddress!=null)
        {
            Log.d(TAG, "Got an ip for interface usb0");
            return inetAddress;
        }

        return InetAddress.getByName("0.0.0.0");
    }

    private InetAddress getLocalIpAddressFromInterfaceName(String interfaceName)
    {
        try
        {
            NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
            if(networkInterface.isUp())
            {
                for (Enumeration<InetAddress> enumIpAddress = networkInterface.getInetAddresses(); enumIpAddress.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddress.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address)
                        return inetAddress;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Unable to get ip address for interface " + interfaceName);
        }
        return null;
    }

    public class InvalidIdentificatorException extends java.lang.Exception
    {
        public InvalidIdentificatorException(){super();}
        public InvalidIdentificatorException(String message){super(message);}
    }

    class ServerObject
    {
        ServerObject(String path, String mime)
        {
            this.path = path;
            this.mime = mime;
        }
        public String path;
        public String mime;
    }

    private ServerObject getFileServerObject(String id) throws InvalidIdentificatorException
    {
        try
        {
            // Remove extension
            int dot = id.lastIndexOf('.');
            if (dot >= 0)
                id = id.substring(0,dot);

            // Try to get media id
            int mediaId = Integer.parseInt(id.substring(3));
            Log.v(TAG, "media of id is " + mediaId);

            MediaStore.MediaColumns mediaColumns = null;
            Uri uri = null;

            if(id.startsWith("/"+AUDIO_PREFIX))
            {
                Log.v(TAG, "Ask for audio");
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                mediaColumns = new MediaStore.Audio.Media();
            }

            if(uri!=null && mediaColumns!=null)
            {
                String[] columns = new String[]{mediaColumns.DATA, mediaColumns.MIME_TYPE};
                String where = mediaColumns._ID + "=?";
                String[] whereVal = {"" + mediaId};

                String path = null;
                String mime = null;
                Cursor cursor = ctx.getContentResolver().query(uri, columns, where, whereVal, null);

                if(cursor.moveToFirst())
                {
                    path = cursor.getString(cursor.getColumnIndexOrThrow(mediaColumns.DATA));
                    mime = cursor.getString(cursor.getColumnIndexOrThrow(mediaColumns.MIME_TYPE));
                }
                cursor.close();

                if(path!=null)
                    return new ServerObject(path, mime);
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "Error while parsing " + id);
            Log.e(TAG, "exception", e);
        }

        throw new InvalidIdentificatorException(id + " was not found in media database");
    }

    /*@Override
    public Response serve(String uri, Method method, Map<String, String> header, Map<String, String> parms,
            Map<String, String> files)*/
    @Override
    public Response serve(IHTTPSession session)
    {
        Map<String, String> header = session.getHeaders();
        Map<String, String> parms = session.getParms();
        String uri = session.getUri();

        Response res = null;

        NanoHTTPD.LOG.log(Level.SEVERE, "HEADER: "+header);
        NanoHTTPD.LOG.log(Level.SEVERE, "PARAM: "+parms);
        NanoHTTPD.LOG.log(Level.SEVERE, "URI: "+uri);
        NanoHTTPD.LOG.log(Level.SEVERE, "METHOD: "+session.getMethod());

        Log.i(TAG, "Serve uri : " + uri);

        for(Map.Entry<String, String> entry : header.entrySet())
            Log.d(TAG, "Header : key=" + entry.getKey() + " value=" + entry.getValue());

        for(Map.Entry<String, String> entry : parms.entrySet())
            Log.d(TAG, "Params : key=" + entry.getKey() + " value=" + entry.getValue());

        //for(Map.Entry<String, String> entry : session.getInputStream(). files.entrySet())
        //    Log.d(TAG, "Files : key=" + entry.getKey() + " value=" + entry.getValue());

        try
        {
            try
            {
                ServerObject obj = getFileServerObject(uri);

                Log.i(TAG, "Will serve " + obj.path);
                res = serveFile(uri, header, new File(obj.path), obj.mime); // new File(obj.path), obj.mime, header);

                NanoHTTPD.LOG.log(Level.SEVERE, "TRY");
            }
            catch(InvalidIdentificatorException e)
            {
                return Response.newFixedLengthResponse(Status.NOT_FOUND, MIME_PLAINTEXT, "Error 404, file not found.");
            }

            if( res != null )
            {
                String version = "1.0";
                try {
                    version = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "Application version name not found");
                }

                // Some DLNA header option
                //res.addHeader("realTimeInfo.dlna.org", "DLNA.ORG_TLAG=*");
                //res.addHeader("contentFeatures.dlna.org", "");
                res.addHeader("Cache-Control", "no-cache");
                //res.addHeader("Last-Modified", "Sun, 09 Aug 2020 15:35:00 GMT");
                res.addHeader("transferMode.dlna.org", "Streaming");
                res.addHeader("contentFeatures.dlna.org", "DLNA.ORG_PN=MP3;DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01700000000000000000000000000000");
                res.addHeader("Server", "\"Android, UPnP/1.0 DLNADOC/1.50, VinylMusicPlayer/1.3.0\""); //DLNADOC/1.50 UPnP/1.0 Cling/2.0 DroidUPnP/"+version +" Android/" + Build.VERSION.RELEASE);

                NanoHTTPD.LOG.log(Level.SEVERE, "ADD HEADER");
            } //Android, UPnP/1.0 DLNADOC/1.50, BubbleUPnP/3.6.8.2

            return res;
        }
        catch(Exception e)
        {
            Log.e(TAG, "Unexpected error while serving file");
            Log.e(TAG, "exception", e);
        }

        return Response.newFixedLengthResponse(Status.INTERNAL_ERROR, MIME_PLAINTEXT, "INTERNAL ERROR: unexpected error.");
    }
}
