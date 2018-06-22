package com.example.santiagorook.selectaroute.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.santiagorook.selectaroute.R;
import com.example.santiagorook.selectaroute.database.AccessDB;
import com.example.santiagorook.selectaroute.entities.User;

import java.io.IOException;


public class RegisterAUser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Create button and set a listening event for the click
        Button register = (Button) findViewById(R.id.registerButton);
        register.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                register();
            }
        });
    }

    public void register() {

        // DB access for everyone in the method
        final AccessDB busTrackerDB = new AccessDB(this);

        try {
            busTrackerDB.createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            busTrackerDB.openDatabase();
        } catch (SQLException sqle) {
            throw sqle;
        }

        // Grab text field from XML and convert it to Strings
        // to be used by signIn method
        EditText selectUsernameText = (EditText) findViewById(R.id.userNameText);
        String username = selectUsernameText.getText().toString();

        // If username field is null, display an alert
        // and reload the page so they can try again
        if (username.equals("")) {
            final AlertDialog.Builder message = new AlertDialog.Builder(RegisterAUser.this);

            message.setMessage("Username field is empty");
            message.setTitle("Register Error");
            message.setCancelable(false);
            message.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    //Toast.makeText(RegisterAUser.this, "try again", Toast.LENGTH_LONG).show();

                    // reload page so they can try again
                    Intent intent = new Intent(RegisterAUser.this, RegisterAUser.class);
                    startActivity(intent);
                }
            });

            AlertDialog alert = message.create();
            alert.show();
        } else {
            try {
                // See if the user already exists
                User existingUser = busTrackerDB.getUser(username);

                if (!existingUser.equals(null)) {
                    // If user exists display alert message and reload page
                    final AlertDialog.Builder message = new AlertDialog.Builder(RegisterAUser.this);

                    message.setMessage("Username already exists");
                    message.setTitle("Register Error");
                    message.setCancelable(false);
                    message.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            //Toast.makeText(RegisterAUser.this, "try again", Toast.LENGTH_LONG).show();

                            // reload page so they can try again
                            Intent intent = new Intent(RegisterAUser.this, RegisterAUser.class);
                            startActivity(intent);
                        }
                    });

                    AlertDialog alert = message.create();
                    alert.show();
                }
            } catch (NullPointerException np) {
                // If it doesn't already exist, check to make sure passwords match
                EditText selectPasswordText = (EditText) findViewById(R.id.passwordText);
                String password = selectPasswordText.getText().toString();

                EditText selectReTypePasswordText = (EditText) findViewById(R.id.reTypePasswordText);
                String reTypePassword = selectReTypePasswordText.getText().toString();

                // If they do, go ahead and create the new user
                if (password.equals(reTypePassword) && !password.equals("")) {

                    int success = 0;

                    // Create User
                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setPassword(password);

                    // Add User to Database
                    success = busTrackerDB.insertUser(newUser);

                    // If the insertion was successful, display success message to user
                    // and take them back to the login page
                    if (success == 1) {
                        final AlertDialog.Builder message = new AlertDialog.Builder(RegisterAUser.this);

                        message.setMessage("Account creation successful!");
                        message.setTitle("Registration");
                        message.setCancelable(false);
                        message.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                // reload page so they can try again
                                Intent intent = new Intent(RegisterAUser.this, HomeLogin.class);
                                startActivity(intent);
                            }
                        });

                        AlertDialog alert = message.create();
                        alert.show();
                    } else if (success == 0) {
                        // If insertion of user was not successful, notify user to try again
                        AlertDialog.Builder message = new AlertDialog.Builder(RegisterAUser.this);

                        message.setMessage("User could not be added");
                        message.setTitle("Register Error");
                        message.setCancelable(false);
                        message.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                //Toast.makeText(RegisterAUser.this, "try again", Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(RegisterAUser.this, RegisterAUser.class);
                                startActivity(intent);
                            }
                        });

                        AlertDialog alert = message.create();
                        alert.show();
                    }
                }

                // User left one or both password fields blank
                else {
                    AlertDialog.Builder message = new AlertDialog.Builder(RegisterAUser.this);

                    message.setMessage("One or both password fields are empty or don't match");
                    message.setTitle("Register Error");
                    message.setCancelable(false);
                    message.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            //Toast.makeText(RegisterAUser.this, "try again", Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(RegisterAUser.this, RegisterAUser.class);
                            startActivity(intent);
                        }
                    });

                    AlertDialog alert = message.create();
                    alert.show();
                }
            }


        }
    }
}
