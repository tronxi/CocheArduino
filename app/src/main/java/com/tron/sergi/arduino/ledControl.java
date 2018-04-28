package com.tron.sergi.arduino;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import java.io.IOException;
import java.util.UUID;

public class ledControl extends AppCompatActivity implements SensorEventListener
{
    Button btnDis, btnAutonomo, btnSeguir, btnSensores;
    String address = null;
    ImageView acelerar, atras, derecha, izquierda, disminuir, aumentar;
    private TextView texto;

    private ProgressDialog progress;
    private SensorManager sensorAceleracion;
    private int xAnterior, yAnterior, zAnterior;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_control);

        getSupportActionBar().hide();

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS);
        setContentView(R.layout.activity_led_control);

        btnDis = (Button)findViewById(R.id.disconnect);
        btnAutonomo = (Button) findViewById(R.id.autonomo);
        btnSeguir = (Button) findViewById(R.id.seguir);
        btnSensores = (Button) findViewById(R.id.sensores);

        acelerar = (ImageView) findViewById(R.id.acelerar);
        atras = (ImageView) findViewById(R.id.atras);
        derecha = (ImageView) findViewById(R.id.derecha);
        izquierda = (ImageView) findViewById(R.id.izquierda);
        disminuir = (ImageView) findViewById(R.id.disminuir);
        aumentar = (ImageView) findViewById(R.id.aumentar);
        texto = (TextView) findViewById(R.id.texto);

        acelerar.setOnTouchListener(new ManejadorAcelerar());
        atras.setOnTouchListener(new ManejadorAtras());
        derecha.setOnTouchListener(new ManejadorDerecha());
        izquierda.setOnTouchListener(new ManejadorIzquierda());
        disminuir.setOnTouchListener(new ManejadorDisminuir());
        aumentar.setOnTouchListener(new ManejadorAumentar());

        sensorAceleracion = (SensorManager) getSystemService(SENSOR_SERVICE);
        new ConnectBT().execute();

    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        String textoXYZ = "";
        int sentido = 0;
        int x = (int)event.values[0];
        int y = (int)event.values[1];
        int z = (int)event.values[2];
        textoXYZ += "x " + x;
        textoXYZ += "\ny " + y;
        textoXYZ += "\nxA " + xAnterior;
        textoXYZ += "\nyA" + yAnterior;
        texto.setText(textoXYZ);
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            if(y <= - 1)
            {
                enviar('w');
                sentido = 1;
            }
            else if(y == 0)
            {
                enviar(' ');
                sentido = 0;
            }
            else if(y > 0)
            {
                enviar('s');
                sentido = -1;
            }
            if(x == 0)
            {
                enviar('m');
            }
            if(x > 0)
            {
                enviar('a');
            }
            else if(x <= -2)
            {
                enviar('d');
            }

            if(sentido == 1)
            {
                if(yAnterior  > y)
                {
                    enviar('+');
                }
                else if(yAnterior  < y)
                {
                    enviar('-');
                }
            }
            else if(sentido == -1)
            {
                if(yAnterior  > y)
                {
                    enviar('-');
                }
                else if(yAnterior  < y)
                {
                    enviar('+');
                }
            }
            xAnterior = x;
            yAnterior = y;
            zAnterior = z;

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please wait!!!");
        }

        @Override
        protected Void doInBackground(Void... devices)
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    public void dis(View v)
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.close();
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish();
    }
    public void sensores(View v)
    {
        if(btnSensores.getText().toString().equals("sensores"))
        {
            acelerar.setVisibility(View.INVISIBLE);
            atras.setVisibility(View.INVISIBLE);
            izquierda.setVisibility(View.INVISIBLE);
            derecha.setVisibility(View.INVISIBLE);
            disminuir.setVisibility(View.INVISIBLE);
            aumentar.setVisibility(View.INVISIBLE);

            btnAutonomo.setEnabled(false);
            btnSeguir.setEnabled(false);
            btnSensores.setText("parar");
            sensorAceleracion.registerListener((SensorEventListener) this,sensorAceleracion.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        else
        {
            acelerar.setVisibility(View.VISIBLE);
            atras.setVisibility(View.VISIBLE);
            izquierda.setVisibility(View.VISIBLE);
            derecha.setVisibility(View.VISIBLE);
            disminuir.setVisibility(View.VISIBLE);
            aumentar.setVisibility(View.VISIBLE);
            btnAutonomo.setEnabled(true);
            btnSeguir.setEnabled(true);
            btnSensores.setText("sensores");
            sensorAceleracion.unregisterListener((SensorEventListener) this,sensorAceleracion.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        }
    }
    public void autonomo(View v)
    {
        if(btnAutonomo.getText().toString().equals("autonomo"))
        {
            if (btSocket != null)
            {
                try
                {
                    btSocket.getOutputStream().write(104);
                    btnAutonomo.setText("PARAR");
                    btnSeguir.setEnabled(false);
                    btnSensores.setEnabled(false);
                } catch (IOException e) {
                    msg("Error");
                }
            }
        }
        else
        {
            if (btSocket != null)
            {
                try
                {
                    btSocket.getOutputStream().write(120);
                    btnAutonomo.setText("autonomo");
                    btnSeguir.setEnabled(true);
                    btnSensores.setEnabled(true);
                } catch (IOException e) {
                    msg("Error");
                }
            }
        }
    }
    public void seguir(View v)
    {
        if(btnSeguir.getText().toString().equals("seguir"))
        {
            if (btSocket != null)
            {
                try
                {
                    btSocket.getOutputStream().write(117);
                    btnSeguir.setText("PARAR");
                    btnSensores.setEnabled(false);
                    btnAutonomo.setEnabled(false);
                } catch (IOException e) {
                    msg("Error");
                }
            }
        }
        else
        {
            if (btSocket != null)
            {
                try
                {
                    btSocket.getOutputStream().write(120);
                    btnSeguir.setText("seguir");
                    btnSensores.setEnabled(true);
                    btnAutonomo.setEnabled(true);
                } catch (IOException e) {
                    msg("Error");
                }
            }
        }
    }

    class ManejadorAcelerar implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            int maskedAction = event.getActionMasked();

            switch (maskedAction)
            {

                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_HOVER_ENTER:
                {
                    if (btSocket!=null)
                    {
                        try
                        {
                            btSocket.getOutputStream().write(119);
                        }
                        catch (IOException e)
                        {
                            msg("Error");
                        }
                    }
                    break;
                }

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_HOVER_EXIT:
                case MotionEvent.ACTION_OUTSIDE:
                {
                    if (btSocket!=null)
                    {
                        try
                        {
                            btSocket.getOutputStream().write(32);
                        }
                        catch (IOException e)
                        {
                            msg("Error");
                        }
                    }
                    break;
                }
            }
            return true;
        }
    }
    class ManejadorAtras implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            int maskedAction = event.getActionMasked();

            switch (maskedAction)
            {

                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_HOVER_ENTER:
                {
                    if (btSocket!=null)
                    {
                        try
                        {
                            btSocket.getOutputStream().write(115);
                        }
                        catch (IOException e)
                        {
                            msg("Error");
                        }
                    }
                    break;
                }

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_HOVER_EXIT:
                case MotionEvent.ACTION_OUTSIDE:
                {
                    if (btSocket!=null)
                    {
                        try
                        {
                            btSocket.getOutputStream().write(32);
                        }
                        catch (IOException e)
                        {
                            msg("Error");
                        }
                    }
                    break;
                }
            }
            return true;
        }
    }

    class ManejadorDerecha implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            int maskedAction = event.getActionMasked();

            switch (maskedAction)
            {

                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_HOVER_ENTER:
                {
                    if (btSocket!=null)
                    {
                        try
                        {
                            btSocket.getOutputStream().write(100);
                            //msg("apretar derecha");
                        }
                        catch (IOException e)
                        {
                            msg("Error");
                        }
                    }
                    break;
                }

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_HOVER_EXIT:
                case MotionEvent.ACTION_OUTSIDE:
                {
                    if (btSocket!=null)
                    {
                        try
                        {
                            btSocket.getOutputStream().write(109);
                            //msg("levantar derecha");
                        }
                        catch (IOException e)
                        {
                            msg("Error");
                        }
                    }
                    break;
                }
            }
            return true;
        }
    }
    class ManejadorIzquierda implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            int maskedAction = event.getActionMasked();

            switch (maskedAction)
            {

                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_HOVER_ENTER:
                {
                    if (btSocket!=null)
                    {
                        try
                        {
                            //msg("izquierda");
                            btSocket.getOutputStream().write(97);
                        }
                        catch (IOException e)
                        {
                            msg("Error");
                        }
                    }
                    break;
                }

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_HOVER_EXIT:
                case MotionEvent.ACTION_OUTSIDE:
                {
                    if (btSocket!=null)
                    {
                        try
                        {
                            btSocket.getOutputStream().write(109);
                            //msg("lenvatnar izquierda");
                        }
                        catch (IOException e)
                        {
                            msg("Error");
                        }
                    }
                    break;
                }
            }
            return true;
        }
    }


    class ManejadorDisminuir implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            int maskedAction = event.getActionMasked();

            switch (maskedAction)
            {

                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_HOVER_ENTER:
                {
                    if (btSocket!=null)
                    {
                        try
                        {
                            btSocket.getOutputStream().write(45);
                        }
                        catch (IOException e)
                        {
                            msg("Error");
                        }
                    }
                    break;
                }
            }
            return true;
        }
    }
    class ManejadorAumentar implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            int maskedAction = event.getActionMasked();

            switch (maskedAction)
            {

                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_HOVER_ENTER:
                {
                    if (btSocket!=null)
                    {
                        try
                        {
                            btSocket.getOutputStream().write(43);
                        }
                        catch (IOException e)
                        {
                            msg("Error");
                        }
                    }
                    break;
                }
            }
            return true;
        }
    }
    private void enviar(int i)
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write(i);
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }
}
