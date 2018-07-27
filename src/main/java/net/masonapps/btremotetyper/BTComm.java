/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.masonapps.btremotetyper;

import com.intel.bluetooth.RemoteDeviceHelper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import javax.swing.SwingUtilities;

/**
 *
 * @author ims_3
 */
public class BTComm extends Thread implements Closeable{

    private final BTRemoteApplication application;
    private BufferedWriter writer = null;
    private BufferedReader reader = null;
    private final UUID uuid;

    public BTComm(BTRemoteApplication application, UUID uuid) {
        this.application = application;
        this.uuid = uuid;
    }

    @Override
    public void run() {
        try {
            LocalDevice localDevice = LocalDevice.getLocalDevice();
            localDevice.setDiscoverable(DiscoveryAgent.GIAC);
            System.out.println("uuid: " + uuid);
            String connectionStr = "btspp://localhost:" + uuid + ";name=RemoteBluetooth";
            StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier) Connector.open(connectionStr);
            System.out.println("BT server started");
            StreamConnection connection = streamConnNotifier.acceptAndOpen();
            RemoteDevice device = RemoteDevice.getRemoteDevice(connection);
            System.out.print("connected to device: " + device.getBluetoothAddress());
            
            reader = new BufferedReader(new InputStreamReader(connection.openInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(connection.openOutputStream()));
            
            while(true){
                String line;
                while((line = reader.readLine()) != null){
                    System.out.println("line read: " + line);
                    final String cpy = line + "";
                    SwingUtilities.invokeLater(() -> {
                        application.onLineRecieved(cpy);
                    });
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(BTComm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized void writeln(String line) throws IOException{
        if(writer != null){
            System.out.println("writing line: " + line);
            writer.write(line);
            writer.newLine();
        }
    }

    @Override
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
            writer = null;
        }
        if (reader != null) {
            reader.close();
            reader = null;
        }
    }
}
