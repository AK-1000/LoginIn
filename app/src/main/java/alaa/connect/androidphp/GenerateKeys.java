package alaa.connect.androidphp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;


import alaa.connect.simplifiedcoding.R;

public class GenerateKeys extends AppCompatActivity {

    Button generatekey;
    EditText pubkey, prikey;
    private PrivateKey privateKeyToString;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_keys);
        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        findViewById(R.id.btnGoTo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GenerateKeys.this, ProfileActivity.class);
                intent.putExtra("a",2);
                startActivity(intent);
            }
        });




        findViewById(R.id.generatekey).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                RSA objectRSA = new RSA();
                PrivateKey pv = objectRSA.privateKey;
                PublicKey pb = objectRSA.publicKey;
                String pvString = Base64.getEncoder().encodeToString(pv.getEncoded());
                String pbString = Base64.getEncoder().encodeToString(pb.getEncoded());;
                TextView publicKeyText = findViewById(R.id.pubkey);
                TextView privateKeyText = findViewById(R.id.prikey);
                System.out.println("----------------------------------------------");
                System.out.println("Public Key is: " + pbString);
                System.out.println("Private Key is: " + pvString);
                System.out.println("----------------------------------------------");
                publicKeyText.setText(pbString);
                privateKeyText.setText(pvString);
                saveKeys(pbString,pvString);
                System.out.println("Keys Saved Successfully");
                System.out.println("Preference is working: \n " + getPbKey());

            }
        });
    }


    private void saveKeys(String pb, String pv){
        SharedPreferences.Editor editor = getSharedPreferences("Keys", MODE_PRIVATE).edit();
        editor.putString("PublicKey", pb);
        editor.putString("PrivateKey", pv);
        editor.apply();

    }

    private String getPbKey(){
        SharedPreferences prefs = getSharedPreferences("Keys", MODE_PRIVATE);
        final String Pb = prefs.getString("PublicKey", "No public key defined");
        return Pb;
    }



    private byte publicKeyToString() {
        TextView pubkey = findViewById(R.id.pubkey);
        pubkey.setText(publicKeyToString());
        return 0;
    }

    private byte privateKeyToString() {
        TextView prikey = findViewById(R.id.prikey);
        prikey.setText(privateKeyToString());
        return 0;
    }


    public class RSA {

        private PrivateKey privateKey;
        private PublicKey publicKey;

        public RSA() {
            try {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(1024);
                KeyPair pair = generator.generateKeyPair();
                privateKey = pair.getPrivate();
                publicKey = pair.getPublic();

            } catch (Exception ignored) {
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String publicKeyToString(PublicKey pub1) {
        String publickey = Base64.getEncoder().encodeToString(pub1.getEncoded());
        return publickey;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String privateKeyToString(PrivateKey pri1) {
        final String privateKey = Arrays.toString(Base64.getDecoder().decode(String.valueOf(pri1)));
        return privateKey;


    }


}
