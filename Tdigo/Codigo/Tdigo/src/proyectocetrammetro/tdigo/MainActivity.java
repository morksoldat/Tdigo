package proyectocetrammetro.tdigo;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Locale;

import proyectocetrammetro.tdigo.AudioDbAdapter;

import android.speech.tts.TextToSpeech;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.database.Cursor;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
	
	public static final String MIME_TEXT_PLAIN = "text/plain";
	public static final String TAG = "Nfc";
	
	private static final int ACTIVITY_AUDIOCONFIG=1;
	 
    private TextView mTextView;
    private NfcAdapter mNfcAdapter;
    
    private TextToSpeech tts;
    private Button btnSpeak;
    private boolean lctra_init=false; // determina si se lee el primer texto o se va a settings
    
    private AudioDbAdapter mDbHelper;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mTextView = (TextView) findViewById(R.id.textView_explanation);
		
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		 
		tts = new TextToSpeech(this, this);   
		
		mDbHelper = new AudioDbAdapter(this);
        mDbHelper.open();
		// seteo de voz
        Cursor DataBase = mDbHelper.fetchAllData();
        startManagingCursor(DataBase);
        if(DataBase.getCount()>0){
        	Cursor pitch = mDbHelper.fetchData(1);
            Cursor speed = mDbHelper.fetchData(2);
	        startManagingCursor(pitch);
	        tts.setPitch(pitch.getFloat(
	            pitch.getColumnIndexOrThrow(AudioDbAdapter.KEY_BODY)));
	        startManagingCursor(speed);
		    tts.setSpeechRate(speed.getFloat(
		        speed.getColumnIndexOrThrow(AudioDbAdapter.KEY_BODY)));
       } else {
    	   mDbHelper.createData("pitch",(float)1.1);
    	   mDbHelper.createData("speechrate",(float)1.0);
    	   tts.setPitch((float)1.1);
	       tts.setSpeechRate((float)1.0);
       }
	    handleIntent(getIntent());
	    btnSpeak = (Button) findViewById(R.id.btnSpeak);
	 // button on click event
	    btnSpeak.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View arg0) {
                speakOut();
            }
        });
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        if (!mNfcAdapter.isEnabled()){  
        	lctra_init=false;
        	speakOut_Act_NFC();
            LayoutInflater inflater = getLayoutInflater();  
               View dialoglayout = inflater.inflate(R.layout.nfc_settings_layout,(ViewGroup) findViewById(R.id.nfc_settings_layout));  
            new AlertDialog.Builder(this).setView(dialoglayout)  
                   .setPositiveButton("Ajustes", new DialogInterface.OnClickListener() {  
                        public void onClick(DialogInterface arg0, int arg1) {   
                        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(intent);
                            }
                        }  
                   })  
                .setOnCancelListener(new DialogInterface.OnCancelListener() {  
                     public void onCancel(DialogInterface dialog) {  
                          finish(); // exit application if user cancels  
                  }                      
                }).create().show();  
                } else {
                	
                	lctra_init=true;
                } 
        if(lctra_init){
        	speakOut();
        }
        setupForegroundDispatch(this, mNfcAdapter);
    }
	
	@Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, mNfcAdapter);
         
        super.onPause();
    }
	
	public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    } 
	
	public void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 
        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);
 
        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};
 
        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }
        
        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }
	
	@Override
    protected void onNewIntent(Intent intent) { 
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         * 
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }
	
	 private void handleIntent(Intent intent) {
		 String action = intent.getAction();
		    if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
		         
		        String type = intent.getType();
		        if (MIME_TEXT_PLAIN.equals(type)) {
		 
		            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		            new NdefReaderTask().execute(tag);
		             
		        } else {
		            Log.d(TAG, "Wrong mime type: " + type);
		        }
		    }
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
		                    Log.e(TAG, "Unsupported Encoding", e);
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
	            mTextView.setText("" + result);
	            if(lctra_init){
	            	speakOut();
	            }
	        }
	    }
	 }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu); 
		return true;
	}
	
	// BARRA SUPERIOR
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
			    // Handle presses on the action bar items
		switch (item.getItemId()) {
		    	case R.id.Audio_config:  
		    		Audio_Configs();
		    		return true;
		        default:
		            return super.onOptionsItemSelected(item);
		    }
		}
	
	/** Configuraciones de audio */
	public void Audio_Configs() {
	    // Do something in response to button
		Intent intent = new Intent(this, ConfiguracionesAudio.class);
		onDestroy();
		mDbHelper.close();
		startActivityForResult(intent, ACTIVITY_AUDIOCONFIG);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode==RESULT_OK){
	        switch (requestCode) {
	        case ACTIVITY_AUDIOCONFIG:
	        	tts = new TextToSpeech(this, this); 
	        	mDbHelper.open();
	        	Cursor pitch = mDbHelper.fetchData(1);
	            Cursor speed = mDbHelper.fetchData(2);
		        startManagingCursor(pitch);
		        tts.setPitch(pitch.getFloat(
		            pitch.getColumnIndexOrThrow(AudioDbAdapter.KEY_BODY)));
		        startManagingCursor(speed);
			    tts.setSpeechRate(speed.getFloat(
			        speed.getColumnIndexOrThrow(AudioDbAdapter.KEY_BODY)));
	        	//speakOut();
	            break;
	        }
        }
        else {
        	tts = new TextToSpeech(this, this); 
        	mDbHelper.open();
        	Cursor pitch = mDbHelper.fetchData(1);
            Cursor speed = mDbHelper.fetchData(2);
	        startManagingCursor(pitch);
	        tts.setPitch(pitch.getFloat(
	            pitch.getColumnIndexOrThrow(AudioDbAdapter.KEY_BODY)));
	        startManagingCursor(speed);
		    tts.setSpeechRate(speed.getFloat(
		        speed.getColumnIndexOrThrow(AudioDbAdapter.KEY_BODY)));
        }
        //String msj=intent.getExtras().getString("EXTRA_MESSAGE");
        //Toast.makeText(this, msj , Toast.LENGTH_LONG ).show();
    }
	
	// Funciones de speech
	@Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
	@Override
    public void onInit(int status) {
 
        if (status == TextToSpeech.SUCCESS) {
        	
        	Locale locSpanish = new Locale("spa", "MEX");
        	//Locale locSpanish = new Locale("spa");
        	
            int result = tts.setLanguage(locSpanish);
 
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                speakOut();
            }
 
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
 
    }
	// funcion de lectura por voz
	private void speakOut() {
		if(mNfcAdapter.isEnabled()){ 
        String text = mTextView.getText().toString();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		} else { speakOut_Act_NFC();}
    }
	
	private void speakOut_Act_NFC() {
		 
        String text = "NFC desactivado, presione botón para ir a Ajustes y Activar NFC";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
				
	
}
