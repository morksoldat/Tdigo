package proyectocetrammetro.mantencionnfctags;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class NFCReadWrite extends Activity {
	
	NfcAdapter adapter;
	PendingIntent pendingIntent;
	IntentFilter writeTagFilters[];
	boolean writeMode;
	Tag mytag;
	Context ctx;
	private TextView message;
	private final static int IMPORTAR = 1;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfcread_write);
		ctx=this;
	    Button btnWrite = (Button) findViewById(R.id.button_write);
	    message = (TextView)findViewById(R.id.edit_message);
	    // Acciones boton que escribe
	    btnWrite.setOnClickListener(new View.OnClickListener()
	    {
	        @Override
	        public void onClick(View v) {
	            try {
	                if(mytag==null){
	                    Toast.makeText(ctx, ctx.getString(R.string.error_detected), Toast.LENGTH_LONG ).show();
	                }else{
	                    // ver si esta escrito de forma permanente
	                	Ndef ndef = Ndef.get(mytag);
	                	if(ndef.isWritable()){
		                	write(ctx,message.getText().toString(),mytag);
		                    Toast.makeText(ctx, ctx.getString(R.string.ok_writing), Toast.LENGTH_LONG ).show();
		                } else{
		                	Toast.makeText(ctx, ctx.getString(R.string.cant_write), Toast.LENGTH_LONG ).show();
		                }
	                }
	            } catch (IOException e) {
	                Toast.makeText(ctx, ctx.getString(R.string.error_writing), Toast.LENGTH_LONG ).show();
	                e.printStackTrace();
	            } catch (FormatException e) {
	                Toast.makeText(ctx, ctx.getString(R.string.error_writing) , Toast.LENGTH_LONG ).show();
	                e.printStackTrace();
	            }
	        }
	    });
	    
	    Button btnRead = (Button) findViewById(R.id.button_read);
	 // Acciones boton que lee
	    btnRead.setOnClickListener(new View.OnClickListener()
	    {
	    	@Override
	        public void onClick(View v) {
	    		try {
	                if(mytag==null){
	                    Toast.makeText(ctx, ctx.getString(R.string.error_detected), Toast.LENGTH_LONG ).show();
	                }else{
	                	// cambiar
	                    read_NFC(mytag);
	                    Toast.makeText(ctx, ctx.getString(R.string.ok_reading), Toast.LENGTH_LONG ).show();
	                }
	            } catch (IOException e) {
	                Toast.makeText(ctx, ctx.getString(R.string.error_reading), Toast.LENGTH_LONG ).show();
	                e.printStackTrace();
	            } catch (FormatException e) {
	                Toast.makeText(ctx, ctx.getString(R.string.error_reading) , Toast.LENGTH_LONG ).show();
	                e.printStackTrace();
	            }
	        }
	    });
	    
	 // Acciones boton para hacer tarjeta read only
	    Button btn_mkreadonly = (Button) findViewById(R.id.btn_mkreadonly);
	    btn_mkreadonly.setOnClickListener(new View.OnClickListener()
	    {
	        @Override
	        public void onClick(View v) {
	            try {
	                if(mytag==null){
	                    Toast.makeText(ctx, ctx.getString(R.string.error_detected), Toast.LENGTH_LONG ).show();
	                }else{
	                	// ver si esta escrito de forma permanente
	                	Ndef ndef = Ndef.get(mytag);
	                	if(ndef.isWritable()){
		                	MakeTagReadOnly(ctx,message.getText().toString(),mytag);
		                    Toast.makeText(ctx, ctx.getString(R.string.ok_makereadonly), Toast.LENGTH_LONG ).show();
		                } else{
		                	Toast.makeText(ctx, ctx.getString(R.string.cant_write), Toast.LENGTH_LONG ).show();
		                }
	                }
	            } catch (IOException e) {
	                Toast.makeText(ctx, ctx.getString(R.string.error_writing), Toast.LENGTH_LONG ).show();
	                e.printStackTrace();
	            } catch (FormatException e) {
	                Toast.makeText(ctx, ctx.getString(R.string.ok_makereadonly) , Toast.LENGTH_LONG ).show();
	                e.printStackTrace();
	            }
	        }
	    });
	    
	 // Acciones boton para solo bloquear tarjeta
	    Button btn_lock = (Button) findViewById(R.id.btn_lock);
	    btn_lock.setOnClickListener(new View.OnClickListener()
	    {
	        @Override
	        public void onClick(View v) {
	            try {
	                if(mytag==null){
	                    Toast.makeText(ctx, ctx.getString(R.string.error_detected), Toast.LENGTH_LONG ).show();
	                }else{
	                	// ver si esta escrito de forma permanente
	                	Ndef ndef = Ndef.get(mytag);
	                	if(ndef.isWritable()){
		                	LockTag(mytag);
		                    Toast.makeText(ctx, ctx.getString(R.string.ok_protected), Toast.LENGTH_LONG ).show();
		                } else{
		                	Toast.makeText(ctx, ctx.getString(R.string.already_protected), Toast.LENGTH_LONG ).show();
		                }
	                }
	            } catch (IOException e) {
	                Toast.makeText(ctx, ctx.getString(R.string.ok_protected), Toast.LENGTH_LONG ).show();
	                e.printStackTrace();
	            } catch (FormatException e) {
	                Toast.makeText(ctx, ctx.getString(R.string.error_protected) , Toast.LENGTH_LONG ).show();
	                e.printStackTrace();
	            }   
	        }
	    });
	    
	    // intent tag detected
	    adapter = NfcAdapter.getDefaultAdapter(this);
	    pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
	    IntentFilter tagDetected = new IntentFilter(NfcAdapter. ACTION_TAG_DISCOVERED );
	    tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
	    writeTagFilters = new IntentFilter[] { tagDetected };
		
	}
	
	// mensaje de la tecnologia del tag detectado
	@Override
	protected void onNewIntent(Intent intent){
	    if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
	        mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	        Toast.makeText(this, this.getString(R.string.ok_detection) + mytag.toString(), Toast.LENGTH_LONG ).show();
	        
	        Ndef ndef = Ndef.get(mytag);
        	if(!ndef.isWritable()){
                Toast.makeText(ctx, ctx.getString(R.string.tag_protected), Toast.LENGTH_LONG ).show();
            }
	    }
	}
	
	// Funcion para escribir NFC
	private void write(Context context ,String text, Tag tag) throws IOException, FormatException {
		NdefRecord[] records = { createRecord(context , text) };
		NdefMessage  message = new NdefMessage(records);
		// Get an instance of Ndef for the tag.
		Ndef ndef = Ndef.get(tag);
		// Enable I/O
		ndef.connect();
		// Write the message
		ndef.writeNdefMessage(message);
		// Close the connection
		ndef.close();
	}
	
	// Funcion que crea el mensaje
	private NdefRecord createRecord(Context context,String text) throws UnsupportedEncodingException {
		String lang       = "en";
		byte[] textBytes  = text.getBytes();
		byte[] langBytes  = lang.getBytes("US-ASCII");
		int    langLength = langBytes.length;
		int    textLength = textBytes.length;
		byte[] payload    = new byte[1 + langLength + textLength];

		// set status byte (see NDEF spec for actual bits)
		payload[0] = (byte) langLength;

		// copy langbytes and textbytes into payload
		System.arraycopy(langBytes, 0, payload, 1,              langLength);
		System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);
		
		// NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, new String("application/" + context.getPackageName()).getBytes(Charset.forName("US-ASCII")),  new byte[0], payload);
		//NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_URI,  new byte[0], payload);
		NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT, new byte[0], payload);
		
		//NdefRecord mimeRecord = NdefRecord.createMime("application/vnd.com.example.android.beam",
		//	    "Beam me up, Android".getBytes(Charset.forName("US-ASCII")));
		
		return recordNFC;
	}
	
	// funciones para proceso de escritura
	@Override
	public void onPause(){
		super.onPause();
		WriteModeOff();
	}

	@Override
	public void onResume(){
		super.onResume();
		WriteModeOn();
	}

	private void WriteModeOn(){
		writeMode = true;
		adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
	}

	private void WriteModeOff(){
		writeMode = false;
		adapter.disableForegroundDispatch(this);
	}
	//
	// LECTURA
	// Funcion para leer NFC
	private void read_NFC( Tag tag) throws IOException, FormatException {
       
		new NdefReaderTask().execute(tag);	
	}
	
	// task para leer tag
		 private class NdefReaderTask extends AsyncTask<Tag, Void, String> {
			 
			    @Override
			    protected String doInBackground(Tag... params) {
			        Tag tag = params[0];
			         
			        Ndef ndef = Ndef.get(tag);
			        if (ndef == null) {
			            // NDEF is not supported by this Tag. 
			            return null;
			        }
			 
			        NdefMessage ndefMessage = ndef.getCachedNdefMessage();
			 
			        NdefRecord[] records = ndefMessage.getRecords();
			        for (NdefRecord ndefRecord : records) {
			            if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
			                try {
			                    return readText(ndefRecord);
			                } catch (UnsupportedEncodingException e) {
			                    Log.e("", "Unsupported Encoding", e);
			                }
			            }
			        }
			 
			        return null;
			    }
			    
		private String readText(NdefRecord record) throws UnsupportedEncodingException {
			        /*
			         * See NFC forum specification for "Text Record Type Definition" at 3.2.1 
			         * 
			         * http://www.nfc-forum.org/specs/
			         * 
			         * bit_7 defines encoding
			         * bit_6 reserved for future use, must be 0
			         * bit_5..0 length of IANA language code
			         */
			 
			        byte[] payload = record.getPayload();
			 
			        // Get the Text Encoding
			        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
			 
			        // Get the Language Code
			        int languageCodeLength = payload[0] & 0063;
			         
			        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
			        // e.g. "en"
			         
			        // Get the Text
			        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
			    }
		 
		@Override
		protected void onPostExecute(String result) {
		        if (result != null) {
		            message.setText("" + result);
		        }
		    }
		 }
	
		// Funcion para hacer NFC read only
			private void MakeTagReadOnly(Context context ,String text, Tag tag) throws IOException, FormatException {
				NdefRecord[] records = { createRecord(context , text) };
				NdefMessage  message = new NdefMessage(records);
				// Get an instance of Ndef for the tag.
				Ndef ndef = Ndef.get(tag);
				// Enable I/O
				ndef.connect();
				// Write the message
				ndef.writeNdefMessage(message);
				// make it read only
				ndef.makeReadOnly();
				// Close the connection
				ndef.close();
			}
	
			// Funcion para hacer NFC read only
			private void LockTag(Tag tag) throws IOException, FormatException {
				// Get an instance of Ndef for the tag.
				Ndef ndef = Ndef.get(tag);
				// Enable I/O
				ndef.connect();
				// make it read only
				ndef.makeReadOnly();
				// Close the connection
				ndef.close();
			}
			
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nfcread_write, menu);
		return true;
	}
	
	// BARRA SUPERIOR
			@Override
			public boolean onOptionsItemSelected(MenuItem item) {
			    // Handle presses on the action bar items
			    switch (item.getItemId()) {
			    	case R.id.Clear:
			    		clear_text();
			    		return true;	
			        default:
			            return super.onOptionsItemSelected(item);
			    }
			}
			
			private void clear_text() {
				// clear text 
				message.setText("");
		    }
			
	
}
