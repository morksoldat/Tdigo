package proyectocetrammetro.tdigo;

import java.util.Locale;

import proyectocetrammetro.tdigo.AudioDbAdapter;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class ConfiguracionesAudio extends MainActivity implements OnInitListener {
	
	// botones
	private Button btnDownPitch;
	private Button btnUpPitch;
	private Button btnDownSpeed;
	private Button btnUpSpeed;
	private Button btnsave;
	// textos de mensaje
	private String strg_dpitch;
	private String strg_upitch;
	private String strg_dspeed;
	private String strg_uspeed;
	
	private String strg_audioconfig;
	
	private float delta=(float)0.1;
	private float confpitch;
	private float confspeed;
	
	private AudioDbAdapter mDbHelper;
	
	TextToSpeech tts;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configuraciones_audio); 
		// textos de lectura configuraciones
		strg_dpitch="Disminuyendo Tono";
		strg_upitch="Aumentando Tono";
		strg_dspeed="Disminuyendo Velocidad";
		strg_uspeed="Aumentando Velocidad";
		
		strg_audioconfig="Configuraciones de Audio, no olvide guardar antes de salir";
		
		tts = new TextToSpeech(getApplicationContext(), this);
		// abrir base de datos
		mDbHelper = new AudioDbAdapter(this);
        mDbHelper.open();
		// variables de audio
        Cursor conf_pitch = mDbHelper.fetchData(1);
        startManagingCursor(conf_pitch);
        confpitch = conf_pitch.getFloat(
	            conf_pitch.getColumnIndexOrThrow(AudioDbAdapter.KEY_BODY));
        Cursor conf_speed = mDbHelper.fetchData(2);
        startManagingCursor(conf_speed);
        confspeed = conf_speed.getFloat(
	            conf_speed.getColumnIndexOrThrow(AudioDbAdapter.KEY_BODY));
        
		// botones
		btnDownPitch = (Button) findViewById(R.id.btnDownPitch);
		btnUpPitch = (Button) findViewById(R.id.btnUpPitch);
		btnDownSpeed = (Button) findViewById(R.id.btnDownSpeed);
		btnUpSpeed = (Button) findViewById(R.id.btnUpSpeed);
		btnsave = (Button) findViewById(R.id.btnsave);
		 // button down pitch on click event
		btnDownPitch.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View arg0) {
	            	confpitch=confpitch-delta;
	            	mDbHelper.updateData((long)1, "pitch", confpitch); 
	                speakOut(strg_dpitch);
	            }
	        });
		 // button down pitch on click event
		btnUpPitch.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View arg0) {
	            	confpitch=confpitch+delta;
	            	mDbHelper.updateData((long)1, "pitch", confpitch); 
	                speakOut(strg_upitch);
	            }
	        });
		 // button down pitch on click event
		btnDownSpeed.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View arg0) {
	            	confspeed=confspeed-delta;
	            	mDbHelper.updateData((long)2, "speechrate", confspeed); 
	                speakOut(strg_dspeed);
	            }
	        });
		 // button down pitch on click event
		btnUpSpeed.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View arg0) {
	            	confspeed=confspeed+delta;
	            	mDbHelper.updateData((long)2, "speechrate", confspeed); 
	                speakOut(strg_uspeed);
	            }
	        });
		// button Guardar
		btnsave.setOnClickListener(new View.OnClickListener() {
			            @Override
			            public void onClick(View arg0) {
			            	Intent i = getIntent();
			            	onDestroy();
			            	mDbHelper.close();
			                setResult(RESULT_OK, i);
			                finish();
			            }
		     });
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.configuraciones_audio, menu);
		return true;
	}
	
	// funcion de lectura por voz
		public void speakOut(String text) {
			Cursor pitch = mDbHelper.fetchData(1);
            Cursor speed = mDbHelper.fetchData(2);
	        startManagingCursor(pitch);
	        tts.setPitch(pitch.getFloat(
	            pitch.getColumnIndexOrThrow(AudioDbAdapter.KEY_BODY)));
	        startManagingCursor(speed);
		    tts.setSpeechRate(speed.getFloat(
		        speed.getColumnIndexOrThrow(AudioDbAdapter.KEY_BODY)));
	        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
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
	        	
	            int result = tts.setLanguage(locSpanish);
	 
	            if (result == TextToSpeech.LANG_MISSING_DATA
	                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
	                Log.e("TTS", "This Language is not supported");
	            } else {
	            	speakOut(strg_audioconfig);
	            }
	 
	        } else {
	            Log.e("TTS", "Initilization Failed!");
	        }
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
			 
		            	onDestroy();
		            	mDbHelper.close();
		                finish();
			             
			        } else {
			            Log.d(TAG, "Wrong mime type: " + type);
			        }
			    }
		 }	
	
}
