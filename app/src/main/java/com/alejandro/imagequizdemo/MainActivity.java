package com.alejandro.imagequizdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MainActivity extends AppCompatActivity {


    private static String SPONGEBOB_RANKING_URL = "https://www.ranker.com/list/the-best-spongebob-squarepants-character/tvs-frank";
    private static String QUERY = "body article.list__article h2.listItem figure.listItem__figure img";
    private static String IMG_NOT_FOUND = "//cdn2.rnkr-static.com/271/img/__v2/app/ranker_noimage--small.svg";

    private HashMap<String, String> characters;
    private String correctAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(characters == null) {
            characters = loadCharacters();
            refreshQuiz();
        }

    }

    private HashMap<String, String> loadCharacters(){
        HashMap<String, String> result = new HashMap<>();
        try {
            Log.d("SPONGEBOB", "Fetching HTML...");
            Document document = new DocumentDownloader().execute(SPONGEBOB_RANKING_URL).get();
            Log.d("SPONGEBOB", "HTML fetched and parsed!");
            Elements elements = document.select(QUERY);

            String name;
            String imgURL;
            for(Element element : elements){
                imgURL = element.attr("src");
                name = element.attr("alt").split("is listed")[0];
                if(name.length() != 0 && !imgURL.equals(IMG_NOT_FOUND)) {
                    name = name.substring(0, name.length() - 1);
                    if (!result.containsKey(name))
                        result.put(name, imgURL);
                }
            }
            return result;
        } catch (InterruptedException e) {
            Log.d("SPONGEBOB", "shrekt: "+e.getMessage());
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.d("SPONGEBOB", "shrekt: "+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private List<String> fetchRandomNames(){
        Random rng = new Random();
        ArrayList<String> keys = new ArrayList<>(characters.keySet());
        ArrayList<String> names = new ArrayList<>();
        String name;
        do{
            name = keys.get(rng.nextInt(keys.size()));
            if(!names.contains(name))
                names.add(name);
        }
        while(names.size() < 4);
        return names;
    }

    private void setImageView(String characterName)
    {
        try {
            String url = characters.get(characterName);
            ImageView view = findViewById(R.id.imageView);
            Log.d("SPONGEBOB", "Fetching image bitmap...");
            Bitmap bitmap = new ImageDownloader().execute(url).get();
            Log.d("SPONGEBOB", "Image fetched!");
            view.setImageBitmap(bitmap);
        } catch(Exception e){
            Log.d("SPONGEBOB", "shrekt: "+e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshQuiz()
    {
        List<String> names = fetchRandomNames();
        Random rng = new Random();
        correctAnswer = names.get(rng.nextInt(names.size()));
        setImageView(correctAnswer);
        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        Button button3 = findViewById(R.id.button3);
        Button button4 = findViewById(R.id.button4);
        button1.setText(names.get(0));
        button2.setText(names.get(1));
        button3.setText(names.get(2));
        button4.setText(names.get(3));
    }

    public void buttonClicked(View view){
        String choice = ((Button)view).getText().toString();
        if(choice != correctAnswer)
            Toast.makeText(this, "Wrong!", Toast.LENGTH_SHORT).show();
        else{
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            refreshQuiz();
        }
    }

    // Best practice: descargar contenido en un thread distinto al thread de UI
    // Primero: tipo de variable que le vamos a mandar a la clases para decirle qué hacer. En este caso, un url
    // Segundo: nombre de método que podríamos usar para mostrar el progreso de la tarea
    // Tercero: tipo de variable que va a ser retornado por la clase
    public class DocumentDownloader extends AsyncTask<String, Void, Document> {

        @Override
        protected Document doInBackground(String... urls) {
            // Agregar permiso en AndroidManifest.xml
            try {
                Document document = Jsoup.connect(urls[0]).get();
                return document;
            }
            catch (Exception e){
                e.printStackTrace();
                Log.d("SPONGEBOB", "shrekt: "+e.getMessage());
            }
            return null;
        }
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                return BitmapFactory.decodeStream(inputStream);
            }
            catch (Exception e){
                Log.d("SPONGEBOB", "shrekt: "+e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }
}
