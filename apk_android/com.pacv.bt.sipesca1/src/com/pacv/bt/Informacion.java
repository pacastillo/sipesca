package com.pacv.bt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;


public class Informacion extends Activity {

	static final int CMD0_ACERCADE = 10;
	    	
//------------------------------------------------------------------------
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.informacion);

        ImageButton boton01 = (ImageButton)findViewById(R.id.Button01);
        boton01.setOnClickListener(new OnClickListener() {
        	// @Override
			public void onClick(View v) {
				finish();
			}
		});
        
        String cadTexto1 = 
        	"<BR>Aplicaci—n para buscar los dispositivos bluetooth cercanos.<BR><BR>Proyecto <b><i>SIPESCA</i></b>.<BR><BR>" + 
        	"La funcionalidad actual de la aplicaci—n se limita a escanear el entorno en busca de dispositivos bluetooth, mostrando la lista, y almacen‡ndola en un archivo de texto en la memoria SD del m—vil o tablet (carpeta <i>sipesca</i>). <BR><BR>" +
        	"Para cada dispositivo encontrado se almacena el nombre, la direcci—n MAC y una marca de tiempo (d’a, hora, minutos y segundos) en que se detect—. <BR><BR>" +
        	"En esta primera versi—n, el periodo de bœsqueda se inicia pulsando el bot—n mostrado en pantalla. En versiones futuras, la aplicaci—n buscar‡ regularmente sin necesidad de intervenci—n del usuario. <BR><BR>" +
        	"Con los datos recopilados en el archivo de texto, se pueden realizar diferentes tipos de an‡lisis. <BR><BR>" ;
		TextView texto1 = (TextView)findViewById(R.id.texto1);
		texto1.setText( Html.fromHtml( cadTexto1 ) );

    }
    
//------------------------------------------------------------------------

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, CMD0_ACERCADE, 0, "Acerca de...").setIcon(android.R.drawable.ic_menu_info_details);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case CMD0_ACERCADE:
        	cmd0_acercade();
        	return true;
        }
        return false;
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
    	return true;
    }

}
