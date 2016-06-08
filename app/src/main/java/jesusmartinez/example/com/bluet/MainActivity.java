package jesusmartinez.example.com.bluet;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    final int REQUEST_ENABLE_BLUETOOTH=1;
    final int REQUEST_DISCOVERABLE=2;

    BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtengo el conector
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
// Si no tengo bluetooth
        if (bluetoothAdapter == null) {
            showMessage("No tienes bluetooth");
            finish();
            return;
        }
        // Cambio el estado inicial
        if (bluetoothAdapter.isEnabled()) {
            ((TextView) findViewById(R.id.textViewState)).setText("Encendido");

        } else {
            ((TextView) findViewById(R.id.textViewState)).setText("Apagado");

        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    public void onClickTurnOn(View view) {
// Si no está activo solicito que se active
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent,
                    REQUEST_ENABLE_BLUETOOTH);
            showMessage("Ya está activo");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == RESULT_OK) {
                    showMessage("El usuario aceptó encender");
                } else {
                    showMessage("El usuario rechazó encender");
                }
                break;

            case REQUEST_DISCOVERABLE:
        if (resultCode==RESULT_CANCELED){
            showMessage("El usuario rechazó ser visible");
        } else {
            showMessage("El dispositivo ya es visible durante "+resultCode+" segundos");
        }
            break;
        }
    }

    @Override
    protected void onStart() {
// Registro el receiver para saber el estado del bluetooh
        registerReceiver(bluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));


        // Registro el receiver para saber si se encuentran dispositivos
        registerReceiver(bluetoothDiscoveryReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(bluetoothDiscoveryReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(bluetoothDiscoveryReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        // Desregistro el receiver de estado
        unregisterReceiver(bluetoothStateReceiver); super.onStop();

        // Desregistro el receiver de busqueda
        unregisterReceiver(bluetoothDiscoveryReceiver);
    }

    // Recibe el estado del bluetooth
    public BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView stateTextView = (TextView) MainActivity.this.findViewById(R.id.textViewState);
            switch (intent.getExtras().getInt(BluetoothAdapter.EXTRA_STATE)) {
                // Apagando
                case BluetoothAdapter.STATE_TURNING_OFF:
                    stateTextView.setText("Apagando");
                    break;
                // Encendiendo
                case BluetoothAdapter.STATE_TURNING_ON:
                    stateTextView.setText("Encenciendo");
                    break;
                // Apagado
                case BluetoothAdapter.STATE_OFF:
                    stateTextView.setText("Apagado");
                    break;
                // Encendido
                case BluetoothAdapter.STATE_ON:
                    stateTextView.setText("Encendido");
                    break;
                // Conectando
                case BluetoothAdapter.STATE_CONNECTING:
                    stateTextView.setText("Conectando");
                    break;
                // Desconectando
                case BluetoothAdapter.STATE_DISCONNECTING:
                    stateTextView.setText("Desconectando");
                    break;
                // Conectado
                case BluetoothAdapter.STATE_CONNECTED:
                    stateTextView.setText("Conectado");
                    break;
                // Desconectado
                case BluetoothAdapter.STATE_DISCONNECTED:
                    stateTextView.setText("Desconectado");
                    break;
            }
        }
    };




    public void onClickPaired (View view) {
        // Obtengo la lista de dispositivos vinculados
        BluetoothDevice[] pairedDevices = bluetoothAdapter.getBondedDevices().toArray(
                new BluetoothDevice[]{});
        // Si no hay dispositivos vinculados
        if (pairedDevices.length == 0) {
            showMessage("No hay dispositivos vinculados"); return;
        }
        String []pairedDevicesNames = new String[pairedDevices.length];
        for (int i = 0; i < pairedDevices.length; i ++) { pairedDevicesNames[i] = pairedDevices[i].getName();
        }
        // Muestro un cuadro de diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dispositivos vinculados") .setItems(pairedDevicesNames, null)
                .setPositiveButton("Aceptar",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
        AlertDialog alert = builder.create(); alert.show();
    }

    public void onClickDiscovery(View view) {
        // Si no está buscando comienzo a buscar
        if (!bluetoothAdapter.isDiscovering()) {
            Log.v("onclicdiscovery", "no está decubriendo");
            bluetoothAdapter.startDiscovery();
            // Paro la búsqueda
        } else {
            bluetoothAdapter.cancelDiscovery();
        }
    }


    // Recibe los dispositivos que encuentra
    public BroadcastReceiver bluetoothDiscoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView stateTextView = (TextView) MainActivity.this.findViewById(R.id.textViewState);
            Button discoveryButton = ((Button) findViewById(R.id.buttonDiscovery));
            // Comienza una búsqueda
            if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {

                // Cambio el texto del botón
                discoveryButton.setText("Buscando..."); // Cambio el estado
                stateTextView.setText("Buscando...");
                // Termina la búsqueda
            } else if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                // Cambio el texto del botón
                discoveryButton.setText("Comenzar búsqueda"); // Cambio el estado
                stateTextView.setText("Encendido");
                // Se encuentra un dispositivo
            } else if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {

                // Obtengo el dispositivo
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Muestro el nombre del dispositivo
                showMessage("Dispositivo encontrado: " + device.getName());
            }
        }
    };

    public void onClickMakeDiscoverable(View view) {

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
        startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);
    }


}
