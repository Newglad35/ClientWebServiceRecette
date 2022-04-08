package com.example.clientwebservicerecette;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    TextView edtResultat;
    Spinner spinCourse;
    ArrayList<String> lesNomsDeCourses;
    String typeDeContenu;
    // L'ADRESSE IP SERA A REMPLACER PAR L'IP DU POSTE CONTENANT LE WEB SERVICE
    String url ="";
    String url_base = "http://192.168.1.137:8080/WebService_Recette/webresources/cuisine";
    String param = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("PASSAGE0");
        initialisations();

        // Lancement de la tache asynchrone (obligatoire car l'appel à un WebService est une "tache longue")
        AccesWebServices accesWS = new AccesWebServices();
        BufferedReader rd = null;
        String retourWS = "";
        try
        {
            url = url_base;
            //Récupération et exploitation de la valeur de retour
            HttpResponse rep = accesWS.execute().get();
            System.out.println("PASSAGE1");
            rd = new BufferedReader(new InputStreamReader(rep.getEntity().getContent()));
            System.out.println("PASSAGE2");
            retourWS = rd.readLine();
            System.out.println("PASSAGE3");
            System.out.println("RETOUR:" + retourWS);
        }
        catch(Exception e)
        {
            System.out.println("ERREUR APPEL DU WEBSERVICE : " + e.getMessage());
        }
        /* PARSING DU TEXTE (retourWS) RETOURNE (format JSON) */
        // Création du tableau JSON
        JSONArray jTab = null;
        try {
            jTab = new JSONArray(retourWS);
        }
        catch (Exception e)
        {
            System.out.println("ERREUR TABLEAU JSON : " + e.getMessage());
        }
        String[] nomCourse = new String[jTab.length()];
        // Pour exploiter le tableau JSON
        for (int i=0; i < jTab.length(); i++)
        {
            try
            {
                nomCourse[i] = jTab.getJSONObject(i).getString("libelle");
            }
            catch (JSONException jse)
            {
                System.out.println("ERREUR OBJET JSON : " + jse.getMessage());
            }
        }
        /* FIN DU PARSING */

        // "Adaptation" du spinner a partir du tableau
        SpinnerAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nomCourse);
        spinCourse.setAdapter(adapter);
    }
    private void initialisations()
    {
        edtResultat = findViewById(R.id.edtResultat);
        spinCourse = findViewById(R.id.spinCuisine);
        /* On met un ecouteur sur le changement d'item du spinner (liste déroulante). Instruction pour l'instant en remarque */
        spinCourse.setOnItemSelectedListener(new ClickSurSpinner());
    }

    private class AccesWebServices extends AsyncTask<Void, Void, HttpResponse>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Début du traitement asynchrone", Toast.LENGTH_LONG).show();
        }
        @Override
        protected HttpResponse doInBackground(Void... params)
        {
            try
            {
                HttpClient c=new DefaultHttpClient();
                HttpGet req=new HttpGet(url);
                req.addHeader("Accept", "application/json");
                req.addHeader("Content-Type", "application/json");
                HttpResponse reponse = c.execute(req);
                return reponse;
            }
            catch (Exception ex)
            {
                System.out.println("ERREUR HTTP : " + ex.getMessage());
                return null;
            }
        }
    }
    private class ClickSurSpinner implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            param = spinCourse.getSelectedItem().toString();
            param = URLEncoder.encode(param);//change les caracteres inadequats
            url = url_base + "/listeIngredient?id=" + param;
            System.out.println("PASSAGE : " + url);

            //Lancement de la tache asynchrone
            AccesWebServices accesWS = new AccesWebServices();
            try {
                // Recupération et exploitation de la valeur de retour (de type HttpResponse)
                HttpResponse rep2 = accesWS.execute().get();
                BufferedReader rd = new BufferedReader(new InputStreamReader(rep2.getEntity().getContent()));
                String retourWS = rd.readLine();
                edtResultat.setText(retourWS); // Juste pour le test

                /* PARSING DU TEXTE (retourWS) RETOURNé */
                // Création du tableau JSON
                JSONArray jTab = new JSONArray(retourWS);

                String texte = "";
                String ident, libelle, unite = "";
                // Pour exploiter le tableau JSON
                for (int i=0; i < jTab.length(); i++)
                {
                    try
                    {
                        nom = jTab.getJSONObject(i).getString("nomCoureur");
                        prenom = jTab.getJSONObject(i).getString("prenomCoureur");
                        place = jTab.getJSONObject(i).getString("place");
                        texte += place + "-" + nom + " " + prenom + " " + tps + "\n";
                    }
                    catch (JSONException e)
                    {
                        // A FAIRE
                    }
                }
                /* FIN DU PARSING */
                edtResultat.setText(texte);

            } catch (Exception e) {
                System.out.println("ERREUR CLICK SUR SPINNER : " + e.getMessage());
            }
        }
// MATHEO
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }
}