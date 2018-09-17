package com.divaga.tecnologico;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.divaga.tecnologico.adapter.AvisosAdapter;
import com.divaga.tecnologico.adapter.PublicacionAdapter;
import com.divaga.tecnologico.model.Avisos;
import com.divaga.tecnologico.model.Publicacion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AvisosActivity extends AppCompatActivity implements AvisosAdapter.OnAvisosSelectedListener{


    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private AvisosAdapter mAdapter;

    private static final int LIMIT = 50;


    @BindView(R.id.recycler_avisos)
    RecyclerView mAvisosRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avisos);

        ButterKnife.bind(this);

        FirebaseFirestore.setLoggingEnabled(true);

        // Firestore
        mFirestore = FirebaseFirestore.getInstance();



        // Get ${LIMIT} restaurants
        mQuery = mFirestore.collection("avisos")
                //.orderBy("numComents", Query.Direction.DESCENDING)
                .limit(LIMIT);

        // RecyclerView
        mAdapter = new AvisosAdapter(mQuery, this) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mAvisosRecycler.setVisibility(View.GONE);
                    // mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mAvisosRecycler.setVisibility(View.VISIBLE);
                    //mEmptyView.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };

        mAvisosRecycler.setLayoutManager(new LinearLayoutManager(this));
        mAvisosRecycler.setAdapter(mAdapter);

        mAvisosRecycler.setNestedScrollingEnabled(false);




         writeOnServer();
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }


    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public void OnAvisosSelected(DocumentSnapshot avisos) {

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.KEY_RESTAURANT_ID, avisos.getId());

        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

    }

    public void writeOnServer() {

        WriteBatch batch = mFirestore.batch();

        DocumentReference restRef = mFirestore.collection("avisos").document();
        Avisos avisos = new Avisos();


        avisos.setUsername("Depto. Sistema y Computacion");
        avisos.setDescription("Se les comunica a los estudiantes ");
        avisos.setUser_photo("https://www.ittux.edu.mx/sites/default/files/styles/medium/public/tec-transp.png?itok=v_2qwjVj");




        // Add restaurant
        batch.set(restRef, avisos);

        /*
        // Add  to subcollection
        for (Comentario comentarios : comentarioss) {
            batch.set(restRef.collection("comentarios").document(), comentario);
        }
        */

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("AvisosActivity", "Write batch succeeded.");
                } else {
                    Log.w("AvisosActivity", "write batch failed.", task.getException());
                }
            }
        });
    }


}
