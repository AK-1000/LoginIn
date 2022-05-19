package alaa.connect.androidphp;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.MediaStore;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;


import alaa.connect.simplifiedcoding.R;

public class ProfileActivity extends AppCompatActivity {

    Button buttonLogout, LoadPicture, Hash;
    ImageView imageView;
    int SELECT_PICTURE = 200;

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imageView = findViewById(R.id.imageView);

        ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
//      Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
      //  bitmap.compress(Bitmap.CompressFormat.JPEG , 100 , stream);
        byte[] bytesData = stream.toByteArray();




        findViewById(R.id.buttonLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPrefManager.getInstance(getApplicationContext()).logout();
            }
        });

        LoadPicture = findViewById(R.id.LoadPicture);
        imageView = findViewById(R.id.imageView);

        // handle the Choose Image button to trigger
        // the image chooser function
        LoadPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();


            }
        });
        Hash = findViewById(R.id.Hash);
        Hash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getSHA (String.copyValueOf(imageString[]);

            }
        });


    }

    public static int hashBitmap(Bitmap bitmap) {
        int hash_result = 0;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        hash_result = (hash_result << 7) ^ h;
        hash_result = (hash_result << 7) ^ w;
        for (int pixel = 0; pixel < 20; ++pixel) {
            int x = (pixel * 50) % w;
            int y = (pixel * 100) % h;
            hash_result = (hash_result << 7) ^ bitmap.getPixel(x, y);
        }
        return hash_result;
    }

    // this function is triggered when
    // the Select Image Button is clicked
    void imageChooser() {

        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    private String getPbKey(){
        SharedPreferences prefs = getSharedPreferences("Keys", MODE_PRIVATE);
        final String Pb = prefs.getString("PublicKey", "No public key defined");
        return Pb;
    }



    private String getPvKey(){
        SharedPreferences prefs = getSharedPreferences("Keys", MODE_PRIVATE);
        final String Pb = prefs.getString("PrivateKey", "No public key defined");
        return Pb;
    }

    public static String sha256(final String base) {
        try{
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(base.getBytes("UTF-8"));
            final StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                final String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static String sign(String plainText, PrivateKey privateKey) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(plainText.getBytes(UTF_8));

        byte[] signature = privateSignature.sign();

        return Base64.getEncoder().encodeToString(signature);
    }


    // this function is triggered when user
    // selects the image from the imageChooser
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    imageView.setImageURI(selectedImageUri);

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        System.out.println("Bitmap saved");
                        int imageHash = hashBitmap(bitmap);
                        String stringImageHash = Integer.toString(imageHash);
                        System.out.println("Bitmap saved: " + imageHash);
                        String publickey = getPbKey();
                        String publicHash = sha256(publickey);
                        String description ="7ayala shi ente 3a asas tzide";
                        String descriptionHash = sha256(description);
                        String startHash =  publickey + descriptionHash + stringImageHash;
                        String finalHash = sha256(startHash);
                        String privateKey = getPvKey();
                        //Convert BackToPrivateKey
                        byte[] keyBytes = Base64.getDecoder().decode(privateKey);
                        KeyFactory kf = KeyFactory.getInstance("RSA"); // or "EC" or whatever
                        PrivateKey privateKeyReverted = kf.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
                        String signedData = sign(finalHash,privateKeyReverted);
                        System.out.println("Data was signed: " + signedData);
                        String userName = SharedPrefManager.getInstance(this).getUser().getUsername();
                        int id =  SharedPrefManager.getInstance(this).getUser().getId();
                        System.out.println("User Logged is: " +userName);
                        insertData(id,userName,stringImageHash,description,publickey,signedData);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private void insertData(int id,String username, String imHash, String descHash, String publicKey, String signedData) {



        class InsertData extends AsyncTask<Void, Void, String> {

            private ProgressBar progressBar2;

            @Override
            protected String doInBackground(Void... voids) {
                //creating request handler object
                RequestHandler requestHandler = new RequestHandler();

                //creating request parameters
                HashMap<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("imageHash", imHash);
                params.put("description", descHash);
                params.put("publicKey", publicKey);
                params.put("signedData", signedData);

                //returing the response
                return requestHandler.sendPostRequest(URLs.URL_DATA, params);
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //displaying the progress bar while user registers on the server
                progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
                progressBar2.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                //hiding the progressbar after completion
                progressBar2.setVisibility(View.GONE);

                try {
                    //converting response to json object
                    //JSONObject obj = new JSONObject(s.toString());

                    //if no error in response
                    if (true) {
                        Toast.makeText(getApplicationContext(), "NOOOOOOOOOOOOOOOOOOOOOO EEEEEEEEEEEEEEEErrrrrrrrrrrrrrrrorrrrr", Toast.LENGTH_SHORT).show();
                        System.out.println("NOOOOOOOOOOOOOOOOOOOOOO EEEEEEEEEEEEEEEErrrrrrrrrrrrrrrrorrrrr");

                        //getting the user from the response
                        //JSONObject userJson = obj.getJSONObject("data");

                        //creating a new user object
                        Data data = new Data(
                                id,
                                username,
                                imHash,
                                descHash,
                                publicKey,
                                signedData
                        );

                        //storing the user in shared preferences
                        SharedPrefManager.getInstance(getApplicationContext()).saveData(data);

                        //starting the profile activity
                        finish();
                        startActivity(new Intent(getApplicationContext(), GenerateKeys.class));
                    } else {
                        System.out.println("Fi ERRRRRRRRRRRRRORRRRRR");
                        Toast.makeText(getApplicationContext(), "Some error occurred", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    System.out.println("Fi BUUUUUUUUUUUUUUUGGGGGGG");
                    e.printStackTrace();
                }
            }
        }

        //executing the async task
        InsertData idd = new InsertData();
        idd.execute();
    }

  /*  public static byte [] getSHA (@androidx.annotation.NonNull String imageString) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        return md.digest(imageString.getBytes(StandardCharsets.UTF_8));
    }*/
}

