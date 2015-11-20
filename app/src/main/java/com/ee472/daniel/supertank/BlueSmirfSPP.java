package com.ee472.daniel.supertank;

/**
 * Created by daniel on 5/30/15.
 */
import android.util.Log;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BlueSmirfSPP
{
    private static final String TAG = "BlueSmirfSPP";

    // Bluetooth code is based on this example
    // http://groups.google.com/group/android-beginners/browse_thread/thread/322c99d3b907a9e9/e1e920fe50135738?pli=1

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Lock    mLock;
    private boolean mIsConnected;
    private boolean mIsError;
    private String  mBluetoothAddress;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket  mBluetoothSocket;
    private OutputStream     mOutputStream;
    private InputStream      mInputStream;

    public BlueSmirfSPP()
    {
        mLock             = new ReentrantLock();
        mIsConnected      = false;
        mIsError          = false;
        mBluetoothAdapter = null;
        mBluetoothSocket  = null;
        mOutputStream     = null;
        mInputStream      = null;
        mBluetoothAddress = null;
    }

    public boolean connect(String addr)
    {
        mLock.lock();
        try
        {
            if(mIsConnected)
            {
                Log.e(TAG, "connect: already connected");
                return false;
            }
            mBluetoothAddress = addr;
        }
        finally
        {
            mLock.unlock();
        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            Log.e(TAG, "connect: no adapter");
            return false;
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Log.e(TAG, "connect: bluetooth disabled");
            return false;
        }

        try
        {
            addr.toUpperCase();

            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(addr);
            mBluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);

            mBluetoothAdapter.cancelDiscovery();

            mBluetoothSocket.connect();
            mOutputStream = mBluetoothSocket.getOutputStream();
            mInputStream = mBluetoothSocket.getInputStream();
        }
        catch (Exception e)
        {
            Log.e(TAG, "connect: ", e);
            disconnect();
            return false;
        }

        mLock.lock();
        try
        {
            mIsConnected = true;
            mIsError     = false;
        }
        finally
        {
            mLock.unlock();
        }
        return true;
    }

    public void disconnect()
    {
        mLock.lock();
        try
        {
            mIsConnected = false;

            try { mOutputStream.close();    } catch(Exception ignored) { }
            try { mInputStream.close();     } catch(Exception ignored) { }
            try { mBluetoothSocket.close(); } catch(Exception ignored) { }

            mOutputStream     = null;
            mInputStream      = null;
            mBluetoothSocket  = null;
            mBluetoothAdapter = null;
            mIsError          = false;
        }
        finally
        {
            mLock.unlock();
        }
    }

    public boolean isConnected()
    {
        mLock.lock();
        try
        {
            return mIsConnected;
        }
        finally
        {
            mLock.unlock();
        }
    }

    public boolean isError()
    {
        mLock.lock();
        try
        {
            return mIsError;
        }
        finally
        {
            mLock.unlock();
        }
    }

    public String getBluetoothAddress()
    {
        mLock.lock();
        try
        {
            return mBluetoothAddress;
        }
        finally
        {
            mLock.unlock();
        }
    }

    public void write(byte[] buffer)
    {
        try
        {
            mOutputStream.write(buffer);
        }
        catch (Exception e)
        {
            mLock.lock();
            try
            {
                if(mIsConnected && (!mIsError))
                {
                    Log.e(TAG, "write: " + e);
                    mIsError = true;
                }
            }
            finally
            {
                mLock.unlock();
            }
        }
    }

    public int readByte()
    {
        int b = 0;
        try
        {
            b = mInputStream.read();
            if(b == -1)
            {
                disconnect();
            }
        }
        catch (Exception e)
        {
            mLock.lock();
            try
            {
                if(mIsConnected && (!mIsError))
                {
                    Log.e(TAG, "readByte: " + e);
                    mIsError = true;
                }
            }
            finally
            {
                mLock.unlock();
            }
        }
        return b;
    }

    public int read(byte[] buffer, int offset, int length)
    {
        int b = 0;
        try
        {
            b = mInputStream.read(buffer, offset, length);
            if(b == -1)
            {
                disconnect();
            }
        }
        catch (Exception e)
        {
            mLock.lock();
            try
            {
                if(mIsConnected && (!mIsError))
                {
                    Log.e(TAG, "read: " + e);
                    mIsError = true;
                }
            }
            finally
            {
                mLock.unlock();
            }
        }
        return b;
    }

    public void flush()
    {
        try
        {
            mOutputStream.flush();
        }
        catch (Exception e)
        {
            mLock.lock();
            try
            {
                if(mIsConnected && (!mIsError))
                {
                    Log.e(TAG, "flush: " + e);
                    mIsError = true;
                }
            }
            finally
            {
                mLock.unlock();
            }
        }
    }
}