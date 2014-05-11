package proyectocetrammetro.mantencionnfctags;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/** Se apreta boton Escribir NFC */
	public void Config_NFCTag(View view) {
	    // Do something in response to button
		Intent intent = new Intent(this, NFCReadWrite.class);
		startActivity(intent);
	}
	

}
