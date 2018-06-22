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

import com.example.santiagorook.selectaroute.R;
import com.example.santiagorook.selectaroute.database.AccessDB;
import com.example.santiagorook.selectaroute.entities.User;

import java.io.IOException;


public class HomeLogin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        try {
            // Create listening event for registerButton
            Button registerButton = (Button) findViewById(R.id.registerButton);
            registerButton.setOnClickListener(new View.OnClickListener() {

                // Register button onClick event
                // Brings new user to registration page
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(HomeLogin.this, RegisterAUser.class);
                    startActivity(intent);
                }
            });

            // Create listening event for loginButton
            Button logInButton = (Button) findViewById(R.id.loginButton);
            logInButton.setOnClickListener(new View.OnClickListener() {

                // Login button click event
                // Checks to see if provided username matches an existing user
                // and if provided password matches that password
                public void onClick(View view) {
                    login();
                }

            });
        } catch (NullPointerException ex) {
            //repository.insertUsers(new User("first_user", "password", "gold", -1, Role.RIDER));
        }

    }

    public void login() {

        User existingUser;
        final AccessDB busTrackerDB = new AccessDB(this);

        // Grab text field from XML and convert it to Strings
        // to be used by signIn method
        EditText editUsernameText = (EditText) findViewById(R.id.userNameText);
        String username = editUsernameText.getText().toString();

        EditText editPasswordText = (EditText) findViewById(R.id.passwordText);
        String password = editPasswordText.getText().toString();

        try{
            busTrackerDB.createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            busTrackerDB.openDatabase();
        }catch (SQLException sqle){
            throw sqle;
        }

        try {
            // Request user record from repository
            existingUser = busTrackerDB.getUser(username);

            // If the user exists
            if (existingUser != null && !existingUser.getUsername().equals("admin")) {

                // Get the password of the retrieved user record
                String usersPassword = existingUser.getPassword();

                // Username and password match a user record in the User table
                if (usersPassword.compareTo(password) == 0) {

                    // Take user to Home page
                    Intent intent = new Intent(HomeLogin.this, UserHomeActivity.class);
                    intent.putExtra("username", existingUser.getUsername());

                    startActivity(intent);
                }
                // Invalid password
                else {
                    AlertDialog.Builder message = new AlertDialog.Builder(HomeLogin.this);

                    message.setMessage("Invalid password for user");
                    message.setTitle("Login Error");
                    message.setCancelable(false);
                    message.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            // reload page so they can try again
                            Intent intent = new Intent(HomeLogin.this, HomeLogin.class);
                            startActivity(intent);
                        }
                    });

                    AlertDialog alert = message.create();
                    alert.show();
                }
            }
            // An admin is trying to log in
            else if(existingUser != null && existingUser.getUsername().equals("admin")) {
                // Get the password of the retrieved user record
                String adminsPassword = existingUser.getPassword();

                // Username and password match a user record in the User table
                if (adminsPassword.compareTo(password) == 0) {

                    // Take admin to admin Home page
                    Intent intent = new Intent(HomeLogin.this, AdminHomeActivity.class);
                    intent.putExtra("username", existingUser.getUsername());
                    startActivity(intent);
                }
                // Invalid password for admin
                else {
                    final AlertDialog.Builder message = new AlertDialog.Builder(HomeLogin.this);

                    message.setMessage("Invalid password");
                    message.setTitle("Login");
                    message.setCancelable(false);
                    message.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            // reload page so they can try again
                            Intent intent = new Intent(HomeLogin.this, HomeLogin.class);
                            startActivity(intent);
                        }
                    });

                    AlertDialog alert = message.create();
                    alert.show();
                }
            }
            // invalid username or user doesn't exist
            else {
                AlertDialog.Builder message = new AlertDialog.Builder(HomeLogin.this);

                message.setMessage("User does not exist");
                message.setTitle("Login Error");
                message.setCancelable(false);
                message.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        // reload page so they can try again
                        Intent intent = new Intent(HomeLogin.this, HomeLogin.class);
                        startActivity(intent);
                    }
                });

                AlertDialog alert = message.create();
                alert.show();
            }

        } catch (NullPointerException npe) {
            AlertDialog.Builder message = new AlertDialog.Builder(HomeLogin.this);

            message.setMessage("You must enter a username and password!");
            message.setTitle("Login Error");
            message.setCancelable(false);
            message.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    // reload page so they can try again
                    Intent intent = new Intent(HomeLogin.this, HomeLogin.class);
                    startActivity(intent);
                }
            });

            AlertDialog alert = message.create();
            alert.show();
        }
    }
}
