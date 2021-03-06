package com.elando.darkoandreev.dobritesasedi;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.elando.darkoandreev.dobritesasedi.DocumentsModel.Documents;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;

import static com.elando.darkoandreev.dobritesasedi.R.layout.property;

/**
 * Created by darko.andreev on 6/2/2017.
 */

public class PartidiView extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public TextView partidaNomer, partidaBalance, partidaProperyRefs;
    private ListView partidaList;
    private String partidaNomerJson;
    private String [] partidi;
    private boolean hasLoggedIn;
    private MaterialSearchView searchView;
    private TextView partida, propertyRefs, saldoPartidi, holderAccountText;
    private int cnt = 0;
    private String isActive;
    private AlertDialog.Builder alertDialog;


    ArrayList<PartidiInfo> arrayOfDocuments = new ArrayList<PartidiInfo>();


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(property);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //searchView = (MaterialSearchView) findViewById(R.id.searchBar);

        partidaList = (ListView) findViewById(R.id.partidaList);
        final MyPartidiAdapter adapter = new MyPartidiAdapter(this, arrayOfDocuments);
        partidaList.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        partidaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DocumentsTask task = new DocumentsTask(PartidiView.this);
                task.execute(new String[]{"http://vrod.dobritesasedi.bg/rest/accounts/" + partidi[position] + "/statement"});
                partidaList.setClickable(false);
            }
        });


        partidiJSONParse();
        //searchItems();
        sortPartidi(adapter);
        sortImot(adapter);
        sortSaldo(adapter);
    }




    //Search items by string in listview

    /*
    public void searchItems () {

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {


            }

            @Override
            public void onSearchViewClosed() {

                partidaList = (ListView) findViewById(R.id.partidaList);
                MyPartidiAdapter adapter = new MyPartidiAdapter(PartidiView.this, arrayOfDocuments);
                partidaList.setAdapter(adapter);

                sortSaldo(adapter);
                sortPartidi(adapter);
                sortImot(adapter);

            }
        });




        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if(query != null && !query.isEmpty()) {
                    final ArrayList<PartidiInfo> foundList = new ArrayList<>();
                    for(PartidiInfo info  : arrayOfDocuments) {
                        if(info.getHolderAccount().toLowerCase().contains(query.toLowerCase()) || info.getPartidaNomer().toLowerCase().contains(query.toLowerCase()) || info.getPartidaBalance().toLowerCase().contains(query.toLowerCase()) || info.getPartidaPropertyRefs().toLowerCase().contains(query.toLowerCase()))
                            foundList.add(info);
                    }

                    partidaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            DocumentsTask task = new DocumentsTask(PartidiView.this);
                            task.execute(new String[]{"http://vrod.dobritesasedi.bg/rest/accounts/" + foundList.get(position).getPartidaNomer().toString() + "/statement"});
                        }
                    });

                    MyPartidiAdapter adapter = new MyPartidiAdapter(PartidiView.this, foundList);
                    partidaList.setAdapter(adapter);

                } else {
                    partidaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            DocumentsTask task = new DocumentsTask(PartidiView.this);
                            task.execute(new String[]{"http://vrod.dobritesasedi.bg/rest/accounts/" + partidi[position] + "/statement"});
                        }
                    });
                    MyPartidiAdapter adapter = new MyPartidiAdapter(PartidiView.this, arrayOfDocuments);
                    partidaList.setAdapter(adapter);

                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {


                return false;
            }
        });


    }
*/
    //Sorting Partidi ListView by ASC and DSC
    public void sortPartidi(final MyPartidiAdapter adapter) {

        partida = (TextView) findViewById(R.id.partida);
        final ArrayList<PartidiInfo> newFoundList = new ArrayList<>();
        final ArrayList<PartidiInfo> newFoundList2 = new ArrayList<>();
        partida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cnt++;

                switch (cnt) {
                    case 1: partida.setText("Партида↑");
                        propertyRefs.setText("Имот");
                        saldoPartidi.setText("Салдо");
                        sortItemsASC(adapter, newFoundList);
                        break;

                    case 2: partida.setText("Партида↓");
                        propertyRefs.setText("Имот");
                        saldoPartidi.setText("Салдо");
                        sortItemsDSC(adapter, newFoundList2);

                        cnt = 0;
                        break;

                }

            }
        }) ;

    }

    public void sortSaldo (final MyPartidiAdapter adapter) {

        saldoPartidi = (TextView) findViewById(R.id.saldo);
        final ArrayList<PartidiInfo> newFoundList = new ArrayList<>();
        final ArrayList<PartidiInfo> newFoundList2 = new ArrayList<>();
        saldoPartidi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cnt++;

                switch(cnt) {
                    case 1: saldoPartidi.setText("Салдо↑");
                        propertyRefs.setText("Имот");
                        partida.setText("Партида");
                        sortSaldoASC(adapter, newFoundList);
                        break;

                    case 2: saldoPartidi.setText("Салдо↓");
                        propertyRefs.setText("Имот");
                        partida.setText("Партида");
                        sortSaldoDSC(adapter, newFoundList2);
                        cnt = 0;
                        break;
                }

            }
        }) ;
    }

    public void sortImot(final MyPartidiAdapter adapter) {

        propertyRefs = (TextView) findViewById(R.id.imot);
        final ArrayList<PartidiInfo> newFoundList = new ArrayList<>();
        final ArrayList<PartidiInfo> newFoundList2 = new ArrayList<>();
        propertyRefs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cnt++;

                switch(cnt) {
                    case 1: propertyRefs.setText("Имот↑");
                        saldoPartidi.setText("Салдо");
                        partida.setText("Партида");
                        sortItemsASC(adapter, newFoundList);
                        break;

                    case 2: propertyRefs.setText("Имот↓");
                        saldoPartidi.setText("Салдо");
                        partida.setText("Партида");
                        sortItemsDSC(adapter, newFoundList2);
                        cnt = 0;
                        break;
                }

            }
        }) ;

    }

    public void sortSaldoASC (MyPartidiAdapter adapter, final ArrayList<PartidiInfo> newFoundList) {
        adapter.sort(new Comparator<PartidiInfo>() {
            @Override
            public int compare(PartidiInfo o1, PartidiInfo o2) {
                Double saldo1 = Double.parseDouble(o1.getPartidaBalance());
                Double saldo2 = Double.parseDouble(o2.getPartidaBalance());

                return saldo1.compareTo(saldo2);
            }
        });

        for (PartidiInfo info : arrayOfDocuments) {
            newFoundList.add(info);
        }

        partidaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DocumentsTask task = new DocumentsTask(PartidiView.this);
                task.execute(new String[]{"http://vrod.dobritesasedi.bg/rest/accounts/" + newFoundList.get(position).getPartidaNomer().toString() + "/statement"});
            }
        });
    }

    public void sortSaldoDSC (MyPartidiAdapter adapter, final ArrayList<PartidiInfo> newFoundList) {
        adapter.sort(new Comparator<PartidiInfo>() {
            @Override
            public int compare(PartidiInfo o1, PartidiInfo o2) {
                Double saldo1 = Double.parseDouble(o1.getPartidaBalance());
                Double saldo2 = Double.parseDouble(o2.getPartidaBalance());

                return saldo2.compareTo(saldo1);
            }
        });

        for (PartidiInfo info : arrayOfDocuments) {
            newFoundList.add(info);
        }

        partidaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DocumentsTask task = new DocumentsTask(PartidiView.this);
                task.execute(new String[]{"http://vrod.dobritesasedi.bg/rest/accounts/" + newFoundList.get(position).getPartidaNomer().toString() + "/statement"});
            }
        });
    }

    public void sortItemsASC (MyPartidiAdapter adapter, final ArrayList<PartidiInfo> newFoundList) {

        adapter.sort( new Comparator<PartidiInfo>() {
            @Override
            public int compare(PartidiInfo o1, PartidiInfo o2) {

                return o1.getPartidaNomer().compareToIgnoreCase(o2.getPartidaNomer());
            }
        });
        for (PartidiInfo info : arrayOfDocuments) {
            newFoundList.add(info);
        }

        partidaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DocumentsTask task = new DocumentsTask(PartidiView.this);
                task.execute(new String[]{"http://vrod.dobritesasedi.bg/rest/accounts/" + newFoundList.get(position).getPartidaNomer().toString() + "/statement"});
            }
        });

    }

    public void sortItemsDSC (MyPartidiAdapter adapter, final ArrayList<PartidiInfo> newFoundList) {

        adapter.sort( new Comparator<PartidiInfo>() {
            @Override
            public int compare(PartidiInfo o1, PartidiInfo o2) {

                return o2.getPartidaNomer().compareToIgnoreCase(o1.getPartidaNomer());
            }
        });
        for (PartidiInfo info : arrayOfDocuments) {
            newFoundList.add(info);
        }

        partidaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DocumentsTask task = new DocumentsTask(PartidiView.this);
                task.execute(new String[]{"http://vrod.dobritesasedi.bg/rest/accounts/" + newFoundList.get(position).getPartidaNomer().toString() + "/statement"});
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_menu, menu);
        inflater.inflate(R.menu.search_menu, menu);
        inflater.inflate(R.menu.refresh_menu, menu);

        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView search = (SearchView) menu.findItem(R.id.searchItem).getActionView();
        SearchableInfo si = manager.getSearchableInfo(getComponentName());

        int options = search.getImeOptions();
        search.setImeOptions(options | EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);

        search.setSearchableInfo(si);
        search.setMaxWidth(Integer.MAX_VALUE);

        search.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                partidaList = (ListView) findViewById(R.id.partidaList);
                MyPartidiAdapter adapter = new MyPartidiAdapter(PartidiView.this, arrayOfDocuments);
                partidaList.setAdapter(adapter);

                sortSaldo(adapter);
                sortPartidi(adapter);
                sortImot(adapter);
                return true;
            }
        });
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {


            @Override
            public boolean onQueryTextSubmit(String query) {

                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if(query != null && !query.isEmpty()) {
                    final ArrayList<PartidiInfo> foundList = new ArrayList<>();
                    for(PartidiInfo info  : arrayOfDocuments) {
                        if(info.getHolderAccount().toLowerCase().contains(query.toLowerCase()) || info.getPartidaNomer().toLowerCase().contains(query.toLowerCase()) || info.getPartidaBalance().toLowerCase().contains(query.toLowerCase()) || info.getPartidaPropertyRefs().toLowerCase().contains(query.toLowerCase()))
                            foundList.add(info);
                    }

                    partidaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            DocumentsTask task = new DocumentsTask(PartidiView.this);
                            task.execute(new String[]{"http://vrod.dobritesasedi.bg/rest/accounts/" + foundList.get(position).getPartidaNomer().toString() + "/statement"});
                        }
                    });

                    MyPartidiAdapter adapter = new MyPartidiAdapter(PartidiView.this, foundList);
                    partidaList.setAdapter(adapter);

                } else {
                    partidaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            DocumentsTask task = new DocumentsTask(PartidiView.this);
                            task.execute(new String[]{"http://vrod.dobritesasedi.bg/rest/accounts/" + partidi[position] + "/statement"});
                        }
                    });
                    MyPartidiAdapter adapter = new MyPartidiAdapter(PartidiView.this, arrayOfDocuments);
                    partidaList.setAdapter(adapter);

                }
                return true;
            }

        });

        //searchView.setMenuItem(item);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.logout_id) {
            Intent intent = new Intent(PartidiView.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);

            SharedPreferences preferences = getSharedPreferences("Login",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.commit();

            finish();
        }

        if(item.getItemId() == R.id.refresh_id) {

            if(isNetworkAvailable()) {
                partidaList = (ListView) findViewById(R.id.partidaList);
                MyPartidiAdapter adapter = new MyPartidiAdapter(PartidiView.this, arrayOfDocuments);
                partidaList.setAdapter(adapter);

                partidaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        DocumentsTask task = new DocumentsTask(PartidiView.this);
                        task.execute(new String[]{"http://vrod.dobritesasedi.bg/rest/accounts/" + partidi[position] + "/statement"});
                        partidaList.setClickable(false);
                    }
                });
            } else {
                alertDialog = new AlertDialog.Builder(PartidiView.this);
                alertDialog.setTitle("Грешка")
                        .setMessage("Няма интернет конекция")
                        .setIcon(R.drawable.icon_error)
                        .setPositiveButton(android.R.string.yes, null)
                        .show();
            }

        }


        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
            moveTaskToBack(true);

    }

    public void partidiJSONParse () {

        partidaNomer = (TextView) findViewById(R.id.partidaNomer);
        partidaBalance = (TextView) findViewById(R.id.partidaBalance);
        partidaProperyRefs = (TextView) findViewById(R.id.partidaPropertyRefs);
        holderAccountText = (TextView) findViewById(R.id.inovice);

        SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
        hasLoggedIn = sp.getBoolean("hasLoggedIn", false);
        String extras = null;

        if(hasLoggedIn) {
            extras = sp.getString("finalJson", null);
        }

        try {
            JSONObject parentObject = new JSONObject(extras);
            JSONObject accountObject = parentObject.getJSONObject("cssc:AccountsWithUser");
            JSONArray parentArray = accountObject.getJSONArray("cssc:Account");

                partidi = new String[parentArray.length()];
                int cnt = 0;

                for (int i = 0; i < parentArray.length(); i++) {

                    JSONObject dolar;
                    JSONArray dolarArray;
                    PartidiInfo info = new PartidiInfo();

                    JSONObject holderAccountArray = parentArray.getJSONObject(i);
                    dolar = holderAccountArray.getJSONObject("cssc:Holder");
                    info.setHolderAccount(dolar.getString("$"));
                    Log.d("Titulqr:", dolar.getString("$"));


                    JSONObject partidaNomerObject = parentArray.getJSONObject(i);
                    dolarArray = partidaNomerObject.getJSONArray("cssc:Uid");

                    JSONObject active = partidaNomerObject.getJSONObject("ct:Active");
                    isActive = active.getString("$");

                    Log.d("Active", isActive);

                    if(isActive.equals("true")) {
                        for (int j = 0; j < dolarArray.length(); j++) {

                            dolar = dolarArray.getJSONObject(j);
                            info.setPartidaNomer(dolar.getString("$"));
                            partidaNomerJson = dolar.getString("$");
                            partidi[cnt] = partidaNomerJson;
                            //partidaNomer.setText(info.getPartidaNomer());
                            Log.d("NomerPartida", dolar.getString("$"));

                        }


                        JSONObject partidaBalanceObject = parentArray.getJSONObject(i);
                        dolar = partidaBalanceObject.getJSONObject("ct:Balance");
                        info.setPartidaBalance(dolar.getString("$"));
                        // partidaBalance.setText(info.getPartidaBalance());
                        Log.d("Balance", dolar.getString("$"));

                        JSONObject partidaPropertyRefsObject = parentArray.getJSONObject(i);
                        if (!partidaPropertyRefsObject.has("cssc:PropertyRefs")) {

                        } else {
                            JSONArray refArray = partidaPropertyRefsObject.getJSONArray("cssc:PropertyRefs");
                            for (int k = 0; k < refArray.length(); k++) {
                                dolar = refArray.getJSONObject(k);
                                JSONObject refObject = dolar.getJSONObject("ct:Ref");
                                JSONObject uIDObject = refObject.getJSONObject("ct:Uid");
                                if (uIDObject.has("@ct:id-type")) {
                                    info.setPartidaPropertyRefs(uIDObject.getString("$"));
                                }
                                Log.d("Refs:", uIDObject.getString("$"));

                            }
                        }

                            arrayOfDocuments.add(info);
                       cnt++;
                    }
                }

        } catch(Exception e){
            e.printStackTrace();
        }


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}

class DocumentsTask extends AsyncTask<String, String, ArrayList<Documents>> {
    private Context context;
    public String finalJsonDocuments;
    private ProgressDialog progressDialog;
    private AlertDialog.Builder alertDialog;
    public DocumentsTask (Context context) {
        this.context = context.getApplicationContext();
        progressDialog = new ProgressDialog(context);
        alertDialog = new AlertDialog.Builder(context);

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    String unm, pass;
    @Override
    protected ArrayList<Documents> doInBackground(String... urls) {

        SharedPreferences sp1 = context.getSharedPreferences("Login", Context.MODE_PRIVATE);

        unm = sp1.getString("username", null);
        pass = sp1.getString("password", null);

        String credentials = unm + ":" + pass;


        String credBase64 = Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT).replace("\n", "");

        String s = "";
        for (String url : urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            StringBuilder builder = new StringBuilder();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Authorization", "Basic " + credBase64);
            httpGet.setHeader("Accept", "application/json;q=0.9,*/*;q=0.8");
            httpGet.setHeader("Accept-Encoding", "gzip, deflate");
            httpGet.setHeader("Content-Type", "application/json");

            try {
                HttpResponse execute = client.execute(httpGet);
                StatusLine statusLine = execute.getStatusLine();
                int statusCode = statusLine.getStatusCode();

                if(statusCode == 200) {
                    InputStream content = execute.getEntity().getContent();

                    String status = execute.getStatusLine().toString();
                    Log.d("Status", status);

                    BufferedReader bufferReader = new BufferedReader(new InputStreamReader(content));
                    StringBuffer buffer = new StringBuffer();


                    while ((s = bufferReader.readLine()) != null) {
                        buffer.append(s);
                        Log.d("Response", String.valueOf(builder));
                    }

                    finalJsonDocuments = buffer.toString();

                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return null;

    }

    @Override
    protected void onPreExecute() {

        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        super.onPreExecute();

    }

    @Override
    protected void onPostExecute(ArrayList<Documents> documents) {

        if(isNetworkAvailable()) {
            Intent intent = new Intent(this.context, ClientDocuments.class);
            intent.putExtra("finalPartidiJson", finalJsonDocuments);
            intent.putExtra("username", unm);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            progressDialog.dismiss();
        } else {
            progressDialog.dismiss();
            alertDialog.setTitle("Грешка")
                    .setMessage("Няма интернет конекция")
                    .setIcon(R.drawable.icon_error)
                    .setPositiveButton(android.R.string.yes, null)
                    .show();
        }

        super.onPostExecute(documents);
    }
}
