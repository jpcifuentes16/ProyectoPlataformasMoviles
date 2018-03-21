package com.cracks.proyectoplataformasmoviles;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Login extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener listener;
    View view;
    private DatabaseReference mDatabase;
    String room;
    String info;
    ArrayList<Cuarto> cuartos = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Cuartos");



        view = this.getCurrentFocus();
        mAuth=FirebaseAuth.getInstance();
        //Variables / objetos que no se usan.
        TextView usernameTV = findViewById(R.id.username_id);
        TextView passwordTV = findViewById(R.id.password_id);
        TextView welcomeTV = findViewById(R.id.welcome_tv);
        TextView mensajeTV = findViewById(R.id.mensaje_tv);
        final EditText roomText = findViewById(R.id.room_text);

        //Variables que se usan, por ello se declaran final.
        final TextView loginTV = findViewById(R.id.login_id);
        final Button continueBtn = findViewById(R.id.continue_btn);//Ingresar sesion
        final ConstraintLayout layoutLogin = findViewById(R.id.loginLayout);
        final ConstraintLayout layoutPrincipal = findViewById(R.id.principalLayout);
        final Button nextBtn = findViewById(R.id.entrar_btn);
        final Switch sw = findViewById(R.id.switch_login);


        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent nuevoIntent = new Intent(Login.this, SalaEspera.class);
                startActivityForResult(nuevoIntent, 0);

//                ValueEventListener valueEventListener = new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                            Cuarto c = snapshot.getValue(Cuarto.class);
//                            cuartos.add(c);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                };
//
//                if(!sw.isChecked() && (roomText.getText().toString() != "")){
//                    for(Cuarto c: cuartos){
//                        if(roomText.getText().toString() == c.getRandomCode()){
//                            Toast.makeText(getApplicationContext(),c.getColumnasT(),Toast.LENGTH_LONG).show();
//                        }
//                    }
//                }

            }
        });



        //Listener de la base de datos
        listener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user=mAuth.getCurrentUser();

                if (user==null)
                {// no esta logeado
                    Toast.makeText(getApplicationContext(),"NO LOGEADO",Toast.LENGTH_LONG).show();

                }
                else
                {
                    //esta logeado :)
                    Toast.makeText(getApplicationContext(),"CORRECTO Y LOGEADO",Toast.LENGTH_LONG).show();
                }
            }
        };

        //Inicia la implmementacion si el usuario desea hostear una sesion.
        final CheckBox checkBox = findViewById(R.id.checkBox);



        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    //Si el boton de switch esta activado
                    layoutLogin.setVisibility(View.VISIBLE);
                    layoutPrincipal.setVisibility(View.GONE);

                } else {

                    //Si no esta activado -> Siguiente pantalla
                    layoutLogin.setVisibility(View.GONE);
                    layoutPrincipal.setVisibility(View.VISIBLE);

                }
            }
        });

        //Inicia la implementacion si el usuario quiere hacer una cuenta
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (isChecked) {

                    //Si la casilla esta marcada
                    continueBtn.setText("Register");
                    loginTV.setText("Create Account");

                } else {

                    continueBtn.setText("Continue");
                    loginTV.setText("Login");

                }
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked())
                {
                    EditText usernameText = findViewById(R.id.username_text);
                    EditText passwordText = findViewById(R.id.password_text);

                    final String email = usernameText.getText().toString() + "@gmail.com";
                    final String contra = passwordText.getText().toString();
                    //no tiene usuario
                    if (!email.isEmpty() && !contra.isEmpty())
                    {
                        //si no estan vacios
                        mAuth.createUserWithEmailAndPassword(email,contra);
                        checkBox.setChecked(false);

                    }
                    else

                    {

                    }

                }
                else
                {
                    ingresar();

                }

            }
        });

    }

    private void ingresar()
    {

        //Se declaran las variables que se van a usar en los metodos
        final EditText usernameText = findViewById(R.id.username_text);
        EditText passwordText = findViewById(R.id.password_text);

        //Boton de carga invisible hasta interaccion
        final ProgressBar carga = findViewById(R.id.cargaInicio);

        final Button continueBtn = findViewById(R.id.continue_btn);

        final String email = usernameText.getText().toString() + "@gmail.com";
        final String contra = passwordText.getText().toString();

        if (!email.isEmpty() && !contra.isEmpty())
        {
            AsyncTask<String, String, String> cargaLogin = new AsyncTask<String, String, String>() {

                String estado = "";

                @Override
                protected void onPreExecute() {

                    continueBtn.setClickable(false);
                    carga.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(),"INICIANDO SESION...",Toast.LENGTH_LONG).show();

                }

                @Override
                protected String doInBackground(String... strings) {

                    try {

                        Thread.sleep(3000);

                        mAuth.signInWithEmailAndPassword(email, contra).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                //Si la informacion de la BD es correcta
                                if (task.isSuccessful()) {

                                    estado = "listo";

                                    Toast.makeText(getApplicationContext(),"CORRECTO",Toast.LENGTH_LONG).show();

                                    //Se crea un nuevo intent y se inicia otra pantalla

                                    Intent nuevoIntent = new Intent(Login.this, Configuracion.class);
                                    nuevoIntent.putExtra("usuario",usernameText.getText().toString());
                                    startActivityForResult(nuevoIntent, 1);


                                } else {

                                    estado = "fallo";
                                    Toast.makeText(getApplicationContext(), "INCORRECTO", Toast.LENGTH_LONG).show();

                                }
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return estado;
                }

                @Override
                protected void onPostExecute(String s) {

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    carga.setVisibility(View.GONE);
                    continueBtn.setClickable(true);

                }

            };

            cargaLogin.execute();

        } else {
            Toast.makeText(getApplicationContext(), "Por favor llene todos los campos", Toast.LENGTH_LONG).show();
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener  != null)
        {
            mAuth.removeAuthStateListener(listener);

        }
    }
}

