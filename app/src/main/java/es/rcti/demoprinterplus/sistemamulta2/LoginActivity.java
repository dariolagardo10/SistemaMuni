package es.rcti.demoprinterplus.sistemamulta2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText etUsername, etPassword, etNombre, etApellido, etLegajo;
    Button btnLogin, btnRegister;
    ScrollView scrollView; // Añadido para ScrollView
    DatabaseHelper db;
    boolean isRegistering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        db = new DatabaseHelper(this); // Asegúrate de que el constructor es correcto
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etLegajo = findViewById(R.id.etLegajo);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        scrollView = findViewById(R.id.scrollView); // Inicializar ScrollView

        btnLogin.setOnClickListener(v -> {
            if (isRegistering) {
                toggleRegistrationFields(false);
            } else {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    boolean checkUser = db.checkUser(username, password);
                    if (checkUser) {
                        Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("USERNAME", username);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Credenciales inválidas", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnRegister.setOnClickListener(v -> {
            if (isRegistering) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                String nombre = etNombre.getText().toString();
                String apellido = etApellido.getText().toString();
                String legajo = etLegajo.getText().toString();
                if (username.isEmpty() || password.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || legajo.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    // Insertar datos sin foto por ahora
                    boolean insertData = db.insertData(username, password, nombre, apellido, legajo, null);
                    if (insertData) {
                        Toast.makeText(LoginActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                        toggleRegistrationFields(false);
                    } else {
                        Toast.makeText(LoginActivity.this, "Falló el registro", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                toggleRegistrationFields(true);
                scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
            }
        });

    }

    private void toggleRegistrationFields(boolean show) {
        isRegistering = show;
        int visibility = show ? View.VISIBLE : View.GONE;
        etNombre.setVisibility(visibility);
        etApellido.setVisibility(visibility);
        etLegajo.setVisibility(visibility);
        btnLogin.setText(show ? "Cancelar" : "Iniciar Sesión");
        btnRegister.setText(show ? "Confirmar Registro" : "Registrarse");
    }
}
