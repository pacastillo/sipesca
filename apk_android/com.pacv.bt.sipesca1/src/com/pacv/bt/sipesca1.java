package com.pacv.bt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class sipesca1 extends Activity {

	static final int CMD0_ACERCADE = 10;
	static final int CMD1_UNO = 11;
	static final int CMD4_PREFS = 14;

	static final int PARADO = 0;
	static final int BUSCANDO = 1;
	int ESTADO = 0;

	// la hebra para reiniciar cada X seg.
	private UpdateThread updater = null;
	
	SharedPreferences prefs = null;
	
	//las cosas del fichero
	String pathEnSD = "BTdevices";
	String nombreFich = "devices.txt";
	
	private static final int REQUEST_ENABLE_BT = 1;
	
    ListView listDevicesFound;
	Button btnScanDevice;
	Button btnStopScan;
	TextView stateBluetooth;
	BluetoothAdapter bluetoothAdapter;
	
	ArrayAdapter<String> btArrayAdapter;
	
	String cadListadoMAC="";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ESTADO = PARADO;
        
        btnScanDevice = (Button)findViewById(R.id.scandevice);
        btnStopScan = (Button)findViewById(R.id.stopscan);
        
        stateBluetooth = (TextView)findViewById(R.id.bluetoothstate);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        listDevicesFound = (ListView)findViewById(R.id.devicesfound);
        btArrayAdapter = new ArrayAdapter<String>(sipesca1.this, android.R.layout.simple_list_item_1);
        listDevicesFound.setAdapter(btArrayAdapter);
        
        CheckBluetoothState();

        btnScanDevice.setOnClickListener(btnScanDeviceOnClickListener);
        btnStopScan.setOnClickListener(btnStopScanOnClickListener);

        //registerReceiver(actionFoundBTReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(eventosBT, filter);

    	//comprobar si la carpeta "pathEnSD" existe; si no existe, la creamos
    	File directorio = new File(Environment.getExternalStorageDirectory(), pathEnSD);
        if (!directorio.exists()) {
            if (!directorio.mkdirs()) {
            	Toast.makeText(this, "ERROR al crear la carpeta en la tarjeta SD", Toast.LENGTH_LONG).show();
            }else{
            	Toast.makeText(this, "Carpeta creada en la tarjeta SD", Toast.LENGTH_LONG).show();
            }
        }
        escribirDato("\n----- INICIO-APLICACION -----");
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(eventosBT); 
	}

	private void CheckBluetoothState(){
    	if (bluetoothAdapter == null){
        	stateBluetooth.setText("Sin bluetooth");
        }else{
        	if (bluetoothAdapter.isEnabled()){
        		if(bluetoothAdapter.isDiscovering()){
        			stateBluetooth.setText("Buscando dispositivos");
        		}else{
        			stateBluetooth.setText("Bluetooth activo");
        			btnScanDevice.setEnabled(true);
        			btnStopScan.setEnabled(true);
        		}
        	}else{
        		stateBluetooth.setText("Bluetooth apagado");
        		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        	}
        }
    }
    
    private Button.OnClickListener btnScanDeviceOnClickListener = new Button.OnClickListener(){
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub	
			if( ESTADO==PARADO ) {
				reiniciarBusqueda();

				ESTADO = BUSCANDO;
			
				// la hebra para reiniciar cada X seg.
				if (updater != null) {
					updater.stop = false;
				}else{
					updater = new UpdateThread();
					updater.start();
				}
			}
		}
	};
	
	private Button.OnClickListener btnStopScanOnClickListener = new Button.OnClickListener(){
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if( ESTADO==BUSCANDO ) {
				bluetoothAdapter.cancelDiscovery();
				Toast.makeText(sipesca1.this, "Paramos la bœsqueda autom‡tica", Toast.LENGTH_LONG).show();

				ESTADO = PARADO;
			
				// parar la hebra para reiniciar cada X seg.
				updater.stop = true;
				if (updater != null) {
					updater.stop = true;
					updater = null;
				}
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(requestCode == REQUEST_ENABLE_BT){
			CheckBluetoothState();
		}
	}
    
	private final BroadcastReceiver eventosBT = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if(BluetoothDevice.ACTION_FOUND.equals(action)) {
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            btArrayAdapter.add(device.getName() + "\n" + device.getAddress());
	            btArrayAdapter.notifyDataSetChanged();

	            //String tmp = device.getName() + " " + device.getAddress() ;
	            String tmp = device.getAddress() ;
	            cadListadoMAC = cadListadoMAC + " " + tmp ;

	            escribirDato( tmp );
	        }
			if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				Toast.makeText(sipesca1.this, "Inicio la bœsqueda de dispositivos BT", Toast.LENGTH_LONG).show();
	            //escribirDato("----- INICIO-BUSQUEDA -----");
		    	btArrayAdapter.clear();
	        }
			if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				Toast.makeText(sipesca1.this, "Fin de la bœsqueda de dispositivos BT", Toast.LENGTH_LONG).show();
	            //escribirDato("----- FIN-BUSQUEDA -----");
				// si no hay MACs que enviar, no hago env’o
				if( !cadListadoMAC.equals("") ) {
					enviarDato(cadListadoMAC);
				}
	            cadListadoMAC="";
	        }
		}
		
	};
	

    public void reiniciarBusqueda() {
    	bluetoothAdapter.cancelDiscovery();
    	btArrayAdapter.clear();
    	bluetoothAdapter.startDiscovery();
    	//Toast.makeText(sipesca1.this, "Reseteamos...", Toast.LENGTH_LONG).show();
    }
    
   	//-------------------------------------------------------------------------------------------
    // http://androiddrawableexplorer.appspot.com/
    // http://developer.android.com/reference/android/R.drawable.html
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, CMD4_PREFS, 0, "Preferencias").setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(0, CMD1_UNO, 0, "Informaci—n").setIcon(android.R.drawable.ic_menu_help);
        menu.add(0, CMD0_ACERCADE, 0, "Acerca de...").setIcon(android.R.drawable.ic_menu_info_details);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case CMD0_ACERCADE:
        	cmd0_acercade();
        	return true;
        case CMD1_UNO:
        	cmd1_informacion();
        	return true;
        case CMD4_PREFS:
        	cmd4_prefs();
        	return true;
        }
        return false;
    }

    private void cmd1_informacion() {
		Intent expIntent = new Intent(sipesca1.this,Informacion.class);
		startActivity(expIntent);
    }

    private void cmd4_prefs() {
    	//Toast.makeText(this, "Comando 4: Preferencias", Toast.LENGTH_LONG).show();
    	Intent prefsIntent = new Intent(this,PrefsActivity.class);
    	startActivity(prefsIntent);
    }
    
    private void cmd0_acercade() {
    	String cad = "Aplicaci—n para buscar los dispositivos bluetooth cercanos.\nProyecto SIPESCA";
    	AlertDialog alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle("Acerca de...");
    	alertDialog.setMessage( cad );
    	alertDialog.setButton("OK", new DialogInterface.OnClickListener() {  
    		public void onClick(DialogInterface dialog, int which) {  
    			return;  
    		} });
    	alertDialog.setIcon(R.drawable.icon);
    	alertDialog.show();    	
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
    	MenuItem acercadeItem = menu.findItem(CMD0_ACERCADE);
    	acercadeItem.setEnabled(true);
    	MenuItem unoItem = menu.findItem(CMD1_UNO);
    	unoItem.setEnabled(true);
    	MenuItem cuatroItem = menu.findItem(CMD4_PREFS);
    	cuatroItem.setEnabled(true);
    	return true;
    }
    
    
    //------------------------------------------------------------------------------------------------------------
    
	private void fileWrite(String filename, String data){
        //acceder al fichero para escribir
    	File directorio = new File(Environment.getExternalStorageDirectory(), pathEnSD);
        if (directorio.exists()) {
        	try {
        		File myFile = new File(directorio, filename);
        		myFile.createNewFile();
        		FileOutputStream fOut = new FileOutputStream(myFile,true);
        		OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
        		myOutWriter.append( data );
        		myOutWriter.close();
        		fOut.close();
        	} catch (Exception e) {
        		Toast.makeText(this, "ERROR al escribir en el fichero", Toast.LENGTH_LONG).show();
        	}
        }else{
            Toast.makeText(this, "ERROR: no existe la carpeta de datos en la tarjeta SD", Toast.LENGTH_LONG).show();
        }
	}
	
	private void escribirDato(String dato) {
		// acceder al fichero para escribir el nuevo dato
		Date date = new Date() ;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd - HH.mm.ss") ;
		String fechaHoy = dateFormat.format(date);
		fileWrite(nombreFich, dato + "   FECHA=" + fechaHoy + "\n" );		
	}
	
	private void enviarDato(String dato) {

		Date date = new Date() ;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd - HH.mm.ss") ;
		String fechaHoy = dateFormat.format(date);
		
		//enviar datos a mercurio
	    HttpClient httpclient = new DefaultHttpClient();
	    try {
		    //HttpPost httppost = new HttpPost("http://mercurio.ugr.es/pedro/testandroid/server.php");
		    HttpPost httppost = new HttpPost(prefs.getString(PrefsActivity.URL_KEY,""));
		    
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
    		String nodoId = prefs.getString(PrefsActivity.NODO_ID_KEY,"");
	        nameValuePairs.add(new BasicNameValuePair("NODO", nodoId ));
	        nameValuePairs.add(new BasicNameValuePair("MAC", dato ));
	        nameValuePairs.add(new BasicNameValuePair("FECHA", fechaHoy ));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        ResponseHandler<String> resHandle = new BasicResponseHandler();
	        String response = httpclient.execute(httppost,resHandle);
	        //Toast.makeText(this, "MERCURIO DICE:"+response, Toast.LENGTH_LONG).show();	        
	    } catch (ClientProtocolException e) {
	    	Toast.makeText(this, "Error al enviar los datos al servidor. Compruebe la conexi—n y la URL en las preferencias.", Toast.LENGTH_LONG).show();
	    } catch (IOException e) {
	    	Toast.makeText(this, "Error al enviar los datos al servidor. Compruebe la conexi—n y la URL en las preferencias", Toast.LENGTH_LONG).show();
	    }		
	} 

	//-------------------------------------------------------------------------------

	// la hebra para reiniciar cada X seg.
	private class UpdateThread extends Thread {
		boolean stop = false;
		public void run() {
			while( !stop ) {
				if( ESTADO == BUSCANDO ){
					// volver a buscar chismes BT
					//reiniciarBusqueda();
					bluetoothAdapter.startDiscovery();
					escribirDato("\n\n----------------loop EN LA HEBRA\n\n");
					//Toast.makeText(sipesca1.this, "reinicio en la hebra", Toast.LENGTH_LONG).show();
				}
	    			
				try {
					Thread.sleep( 15*1000 );  // 15 segundos 
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
	    }
	}

}
