package com.example.italo.tp02;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

public class NoActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {

    MyRecyclerViewAdapter adapter;
    Button noButton;
    Button filhoButton;
    ArrayList<Nodo> nodos;
    CriaBanco dbHelper;
    SQLiteDatabase writeDB;
    SQLiteDatabase readDB;
    long idPai;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();
        idPai = b.getLong("idPai");
        String nomePai = b.getString("name");
        this.setTitle(nomePai);

        dbHelper = new CriaBanco(this);

        // Ler do banco
        readDB = dbHelper.getReadableDatabase();
        String[] columns = {
                CriaBanco.ID,
                CriaBanco.NAME,
                CriaBanco.PAI,
                CriaBanco.TIPO
        };

        String sortOrder = CriaBanco.ID + " ASC";

        // Filter results WHERE "pai" = '1'
        String selection = CriaBanco.PAI + " = ?";
        String[] selectionArgs = { String.valueOf(idPai) };

        Cursor cursor = readDB.query(
                CriaBanco.TABELA,   // The table to query
                columns,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        // data to populate the RecyclerView with
        nodos = new ArrayList<>();
        while(cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(CriaBanco.ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(CriaBanco.NAME));
            String tipo = cursor.getString(cursor.getColumnIndexOrThrow(CriaBanco.TIPO));
            if (tipo.equals("NO")) {
                nodos.add(new Nodo(id, name, true));
            } else {
                nodos.add(new Nodo(id, name, false));
            }

        }
        cursor.close();

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, nodos);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        noButton = findViewById(R.id.no);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNo();
                adapter.notifyItemInserted(nodos.size());
            }
        });

        filhoButton = findViewById(R.id.filho);
        filhoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFilho();
                adapter.notifyItemInserted(nodos.size());
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onItemClick(View view, int position) {
        if (adapter.getItem(position).isNo) {
            Intent intent =  new Intent(NoActivity.this, NoActivity.class);
            Bundle b = new Bundle();
            b.putString("name", adapter.getItem(position).name);
            b.putLong("idPai", adapter.getItem(position).id);
            intent.putExtras(b);
            startActivityForResult(intent,position);
        } else {
            Toast.makeText(this,"Não é possivel abrir um filho", Toast.LENGTH_SHORT).show();
        }
    }

    private void addNo() {
        // Insere no banco
        writeDB = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String name = "No " + countNo();
        values.put(CriaBanco.NAME, name);
        values.put(CriaBanco.PAI, idPai);
        values.put(CriaBanco.TIPO, "NO");
        long id = writeDB.insert(CriaBanco.TABELA, null, values);
        nodos.add(new Nodo(id, name, true));
    }

    private int countNo() {
        readDB = dbHelper.getReadableDatabase();
        Cursor mCount= readDB.rawQuery("select count(*) from nodos where tipo='NO'", null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();
        return count;
    }

    private void addFilho() {
        // Insere no banco
        writeDB = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String name = "Filho " + countFilho();
        values.put(CriaBanco.NAME, name);
        values.put(CriaBanco.PAI, idPai);
        values.put(CriaBanco.TIPO, "FILHO");
        long id = writeDB.insert(CriaBanco.TABELA, null, values);
        nodos.add(new Nodo(id, name, false));
    }

    private int countFilho() {
        readDB = dbHelper.getReadableDatabase();
        Cursor mCount= readDB.rawQuery("select count(*) from nodos where tipo='FILHO'", null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();
        return count;
    }

}