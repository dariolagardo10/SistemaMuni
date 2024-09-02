package es.rcti.demoprinterplus.sistemamulta2;

import android.graphics.Bitmap;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import android.location.Geocoder;
import android.location.Address;
import java.util.List;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Location;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;


import es.rcti.demoprinterplus.sistemamulta2.photos.TrafficViolations;

public class MainActivity extends AppCompatActivity {

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    private OutputStream outputStream;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;

    // Ticket printing views
    private EditText etApellidoNombre, etDomicilio, etLocalidad, etCodPostal, etDepartamento,
            etProvincia, etPais, etLicencia, etClase, etVencimiento, etMultaInfo,
            etDia, etMes, etAnio, etHora, etMinuto, etNumeroDocumento,etEquipo, etMarcaCinemometro, etModeloCinemometro, etSerieCinemometro,
            etCodAprobacionCinemometro, etValorCinemometro;
    private Spinner spinnerTipoDocumento;
    private EditText etDominio, etOtraMarca, etModelo, etOtroTipoVehiculo,etPropietario;
    private Spinner spinnerMarca, spinnerTipoVehiculo;

    private Button btnConectarImprimir;
    private Button btnTomarFoto;
    private TextView tvEstado, tvConductor, tvVehiculo;
    private View layoutConductor, layoutVehiculo;

    private DatabaseHelper db;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private Uri photoURI;

    private static final int REQUEST_PERMISSIONS = 2;

    private ImageView ivPhotoPreview;
    private Button btnConfirmPhoto, btnRetakePhoto;
    private Bitmap capturedBitmap;

    private TextView tvTitulo;
    private View layoutTitulo;
    private TextView tvHecho;
    private View layoutHecho;
    private Spinner spinnerInfraccion;
    private EditText etLugar;
    private TextView tvNumero;
    private TextView  tvEncabezado;
    private EditText etDepartamentoMulta;
    private EditText etMunicipioMulta;
    private EditText etModeloVehiculo;
    private final int ANCHO_IMG_58_MM = 384;
    private static final int MODE_PRINT_IMG = 0;
    private TextView tvEspecificaciones;
    private View layoutEspecificaciones;

    private FusedLocationProviderClient fusedLocationClient;
    private String ubicacionActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtener ubicación
        obtenerUbicacionActual();

        initializeViews();
        setupSpinner();
        setupListeners();
        setupInfraccionSpinner();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        db = new DatabaseHelper(this);


        etLocalidad.setText("Posadas");
        etProvincia.setText("Misiones");
        etDepartamento.setText("Capital");
        etLocalidad.setText("Posadas");
        etProvincia.setText("Misiones");
        etCodPostal.setText("3300");
        etPais.setText("Argentina");
        generateRandomNumber();
        String username = getIntent().getStringExtra("USERNAME");
        if (username != null) {
            String fullName = db.getFullName(username);
            updateSpinnerWithUserName(fullName);
        }

        ivPhotoPreview = findViewById(R.id.ivPhotoPreview);
        btnConfirmPhoto = findViewById(R.id.btnConfirmPhoto);
        btnRetakePhoto = findViewById(R.id.btnRetakePhoto);

        btnConfirmPhoto.setOnClickListener(v -> saveViolationToDatabase());
        btnRetakePhoto.setOnClickListener(v -> tomarFoto());
    }

    private void obtenerUbicacionActual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS);
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    if (addresses != null && !addresses.isEmpty()) {
                                        Address address = addresses.get(0);
                                        String thoroughfare = address.getThoroughfare(); // Calle
                                        String subThoroughfare = address.getSubThoroughfare(); // Número de calle

                                        String addressText = "";
                                        if (thoroughfare != null) {
                                            addressText += thoroughfare;
                                            if (subThoroughfare != null) {
                                                addressText += " " + subThoroughfare;
                                            }
                                        }

                                        if (addressText.trim().isEmpty()) {
                                            addressText = String.format("Lat: %.4f, Lon: %.4f", location.getLatitude(), location.getLongitude());
                                        }

                                        ubicacionActual = addressText;
                                        etLugar.setText(ubicacionActual);
                                    } else {
                                        ubicacionActual = String.format("Lat: %.4f, Lon: %.4f", location.getLatitude(), location.getLongitude());
                                        etLugar.setText(ubicacionActual);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    ubicacionActual = String.format("Lat: %.4f, Lon: %.4f", location.getLatitude(), location.getLongitude());
                                    etLugar.setText(ubicacionActual);
                                }
                            } else {
                                mostrarMensaje("Ubicación no disponible");
                                ubicacionActual = "Ubicación no disponible";
                                etLugar.setText(ubicacionActual);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        mostrarMensaje("Error al obtener la ubicación: " + e.getMessage());
                        ubicacionActual = "Error al obtener la ubicación";
                        etLugar.setText(ubicacionActual);
                    });
        }
    }



    private void saveViolationToDatabase() {
        if (capturedBitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            capturedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] photoBytes = stream.toByteArray();

            TrafficViolations dbHelper = new TrafficViolations(this);

            // Get current date and time
            String fechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            boolean isInserted = dbHelper.insertViolation(
                    "username", // Replace with actual username
                    photoBytes,
                    etApellidoNombre.getText().toString(),
                    etDomicilio.getText().toString(),
                    etLocalidad.getText().toString(),
                    spinnerTipoDocumento.getSelectedItem().toString(),
                    etNumeroDocumento.getText().toString(),
                    etDominio.getText().toString(),
                    spinnerMarca.getSelectedItem().toString(),
                    etModelo.getText().toString(),
                    spinnerTipoVehiculo.getSelectedItem().toString(),
                    spinnerInfraccion.getSelectedItem().toString(),
                    etLugar.getText().toString(),
                    fechaHora
            );

            if (isInserted) {
                mostrarMensaje("Imagen guardada en la base de datos.");
            } else {
                mostrarMensaje("Error al guardar la imagen en la base de datos.");
            }

            // Reset the view
            ivPhotoPreview.setVisibility(View.GONE);
            btnConfirmPhoto.setVisibility(View.GONE);
            btnRetakePhoto.setVisibility(View.GONE);
            btnTomarFoto.setVisibility(View.VISIBLE);
        }
    }

    private void generateRandomNumber() {
        try {
            Random random = new Random();
            int randomNumber = random.nextInt(100000);
            tvNumero.setText(" " + String.valueOf(randomNumber));
        } catch (Exception e) {
            mostrarMensaje("Error al generar el número: " + e.getMessage());
        }
    }

    private void initializeViews() {
        etEquipo = findViewById(R.id.etEquipo);
        etMarcaCinemometro = findViewById(R.id.etMarcaCinemometro);
        etModeloCinemometro = findViewById(R.id.etModeloCinemometro);
        etSerieCinemometro = findViewById(R.id.etSerieCinemometro);
        etCodAprobacionCinemometro = findViewById(R.id.etCodAprobacionCinemometro);
        etValorCinemometro = findViewById(R.id.etValorCinemometro);

        tvEspecificaciones = findViewById(R.id.tvEspecificaciones);
        layoutEspecificaciones = findViewById(R.id.layoutEspecificaciones);
        tvNumero = findViewById(R.id.tvNumero);
        etModeloVehiculo = findViewById(R.id.etModeloVehiculo);
        etDepartamentoMulta = findViewById(R.id.etDepartamentoMulta);
        etMunicipioMulta = findViewById(R.id.etMunicipioMulta);
        tvHecho = findViewById(R.id.tvHecho);
        layoutHecho = findViewById(R.id.layoutHecho);
        spinnerInfraccion = findViewById(R.id.spinnerInfraccion);
        etLugar = findViewById(R.id.etLugar);
        etPropietario = findViewById(R.id.etPropietario);
        etApellidoNombre = findViewById(R.id.etApellidoNombre);
        etDomicilio = findViewById(R.id.etDomicilio);
        etLocalidad = findViewById(R.id.etLocalidad);
        etCodPostal = findViewById(R.id.etCodPostal);
        etDepartamento = findViewById(R.id.etDepartamento);
        etProvincia = findViewById(R.id.etProvincia);
        etPais = findViewById(R.id.etPais);
        etLicencia = findViewById(R.id.etLicencia);
        etClase = findViewById(R.id.etClase);
        etVencimiento = findViewById(R.id.etVencimiento);
        etMultaInfo = findViewById(R.id.etMultaInfo);
        etNumeroDocumento = findViewById(R.id.etNumeroDocumento);
        spinnerTipoDocumento = findViewById(R.id.spinnerTipoDocumento);
        btnConectarImprimir = findViewById(R.id.btnConectarImprimir);
        tvEstado = findViewById(R.id.tvEstado);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);

        etDominio = findViewById(R.id.etDominio);
        spinnerMarca = findViewById(R.id.spinnerMarca);
        etOtraMarca = findViewById(R.id.etOtraMarca);

        spinnerTipoVehiculo = findViewById(R.id.spinnerTipoVehiculo);

        tvConductor = findViewById(R.id.tvConductor);
        tvVehiculo = findViewById(R.id.tvVehiculo);
        layoutConductor = findViewById(R.id.layoutConductor);
        layoutVehiculo = findViewById(R.id.layoutVehiculo);

        Calendar calendar = Calendar.getInstance();
        int dia = calendar.get(Calendar.DAY_OF_MONTH);
        int mes = calendar.get(Calendar.MONTH) + 1;
        int anio = calendar.get(Calendar.YEAR);
        int hora = calendar.get(Calendar.HOUR_OF_DAY);
        int minuto = calendar.get(Calendar.MINUTE);
    }

    private void setupListeners() {

        btnConectarImprimir.setOnClickListener(v -> checkBluetoothPermissions());
        btnTomarFoto.setOnClickListener(v -> tomarFoto());
        spinnerTipoDocumento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDocumentType = (String) parent.getItemAtPosition(position);
                etNumeroDocumento.setText("");

                try {
                    Random random = new Random();
                    int randomNumber = random.nextInt(100000);
                   // etNumero.setText(String.valueOf(randomNumber));
                } catch (Exception e) {
                    mostrarMensaje("Error al generar el número: " + e.getMessage());
                }

                switch (selectedDocumentType) {
                    case "DNI":
                        etNumeroDocumento.setHint("Ingrese su DNI");
                        break;
                    case "Pasaporte":
                        etNumeroDocumento.setHint("Ingrese su Pasaporte");
                        break;
                    case "C.I.":
                        etNumeroDocumento.setHint("Ingrese su C.I.");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                etNumeroDocumento.setText("");
            }
        });

        tvConductor.setOnClickListener(v -> toggleSectionVisibility(layoutConductor));
        tvVehiculo.setOnClickListener(v -> toggleSectionVisibility(layoutVehiculo));
        tvHecho.setOnClickListener(v -> toggleSectionVisibility(layoutHecho));
        tvEspecificaciones.setOnClickListener(v -> toggleSectionVisibility(layoutEspecificaciones));
    }



    private void tomarFoto() {
        // Verificar permisos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permisos
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS);
        } else {
            // Permisos ya concedidos, proceder a tomar la foto
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error al crear el archivo
                }
                if (photoFile != null) {
                    photoURI = FileProvider.getUriForFile(this,
                            "es.rcti.demoprinterplus.sistemamulta2.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionActual();
            } else {
                mostrarMensaje("Permisos de ubicación necesarios.");
            }
        }

        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permisos concedidos, intentar tomar la foto nuevamente
                tomarFoto();
            } else {
                // Permisos denegados, mostrar mensaje al usuario
                Toast.makeText(this, "Permisos necesarios para tomar la foto", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                // Convertir la foto capturada en un Bitmap
                capturedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);

                // Mostrar la imagen en el ImageView
                ivPhotoPreview.setImageBitmap(capturedBitmap);
                ivPhotoPreview.setVisibility(View.VISIBLE);

                // Mostrar botones de confirmación y retomar foto
                btnConfirmPhoto.setVisibility(View.VISIBLE);
                btnRetakePhoto.setVisibility(View.VISIBLE);

                // Ocultar el botón de tomar foto
                btnTomarFoto.setVisibility(View.GONE);

            } catch (IOException e) {
                e.printStackTrace();
                mostrarMensaje("Error al procesar la foto.");
            }
        }
    }


    private void toggleSectionVisibility(View layout) {
        if (layout.getVisibility() == View.GONE) {
            layout.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.GONE);
        }
    }
    private void setupInfraccionSpinner() {
        List<String> infraccionesList = new ArrayList<>();
        infraccionesList.add("Seleccione una infraccion");
        infraccionesList.add("Circular sin luces bajas encendidas (Art.47 INC A LEY 24449)");
        infraccionesList.add("Estacionamiento Indebido (Art.49 LEY 24449)");
        infraccionesList.add("No respetar senalizacion de semaforos (Art.44 INC A LEY 24449)");
        infraccionesList.add("Circular con Moto sin Casco (Art.77 INC S LEY 24449)");
        infraccionesList.add("Exceso de Velocidad (Art.51 LEY 24449)");
        infraccionesList.add("Circular usando TEL CEL O AURICULARES (Art.48 INC X LEY 24449)");
        infraccionesList.add("Alcohol en Sangre (Art.48 INC A LEY 24449)");
        infraccionesList.add("Ocupantes no usan cinturon de Seguridad (Art.40 INC k LEY 24449)");
        infraccionesList.add("Documentacion Conductor Automotor  (Art.40 INC A,B,C LEY 24449)");
        infraccionesList.add("LIC Vencida (Art.13 INC C LEY 24449)");

        ArrayAdapter<String> adapterInfraccion = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, infraccionesList);
        adapterInfraccion.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInfraccion.setAdapter(adapterInfraccion);
    }
    private void setupSpinner() {
        List<String> tipoDocumentoList = new ArrayList<>();
        tipoDocumentoList.add("Seleccione un documento");
        tipoDocumentoList.add("DNI");
        tipoDocumentoList.add("Pasaporte");
        tipoDocumentoList.add("C.I.");


        ArrayAdapter<String> adapterTipoDocumento = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, tipoDocumentoList);
        adapterTipoDocumento.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoDocumento.setAdapter(adapterTipoDocumento);

        List<String> expedidaPorList = new ArrayList<>();
        expedidaPorList.add("Seleccione una opción");
        ArrayAdapter<String> adapterExpedidaPor = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, expedidaPorList);
        adapterExpedidaPor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       // spinnerExpedidaPor.setAdapter(adapterExpedidaPor);

        List<String> marcasList = new ArrayList<>();
        marcasList.add("Seleccione una marca");
        marcasList.add("Toyota");
        marcasList.add("Ford");
        marcasList.add("Chevrolet");
        marcasList.add("Volkswagen");
        marcasList.add("Otro");

        ArrayAdapter<String> adapterMarca = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, marcasList);
        adapterMarca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMarca.setAdapter(adapterMarca);

        List<String> tiposVehiculoList = new ArrayList<>();
        tiposVehiculoList.add("Seleccione un tipo de vehiculo");
        tiposVehiculoList.add("Automovil");
        tiposVehiculoList.add("Camioneta");
        tiposVehiculoList.add("Moto");
        tiposVehiculoList.add("Omnibus");
        tiposVehiculoList.add("Camion");
        tiposVehiculoList.add("Acoplado");
        tiposVehiculoList.add("Trailer");
        tiposVehiculoList.add("Otro");

        ArrayAdapter<String> adapterTipoVehiculo = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, tiposVehiculoList);
        adapterTipoVehiculo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoVehiculo.setAdapter(adapterTipoVehiculo);
    }

    private void updateSpinnerWithUserName(String fullName) {
       // ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerExpedidaPor.getAdapter();
       // adapter.insert(fullName, 1);
       // adapter.notifyDataSetChanged();
       // spinnerExpedidaPor.setSelection(1);
    }

    private void checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        REQUEST_BLUETOOTH_PERMISSION);
            } else {
                conectarImpresora();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                        REQUEST_BLUETOOTH_PERMISSION);
            } else {
                conectarImpresora();
            }
        }
    }

    private void conectarImpresora() {
        if (bluetoothAdapter == null) {
            mostrarMensaje("Bluetooth no disponible en este dispositivo");
            tvEstado.setText("Estado: Bluetooth no disponible");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            mostrarMensaje("Por favor, active el Bluetooth");
            tvEstado.setText("Estado: Bluetooth desactivado");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                buscarYConectarDispositivo();
            } else {
                mostrarMensaje("No se tienen los permisos necesarios para Bluetooth");
                tvEstado.setText("Estado: Permisos de Bluetooth insuficientes");
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                buscarYConectarDispositivo();
            } else {
                mostrarMensaje("No se tienen los permisos necesarios para Bluetooth");
                tvEstado.setText("Estado: Permisos de Bluetooth insuficientes");
            }
        }
    }

    private void buscarYConectarDispositivo() {
        try {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.isEmpty()) {
                mostrarMensaje("No se encontraron dispositivos emparejados");
                tvEstado.setText("Estado: No hay dispositivos Bluetooth emparejados");
                return;
            }

            for (BluetoothDevice device : pairedDevices) {
                try {
                    bluetoothDevice = device;
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                    bluetoothSocket.connect();
                    mostrarMensaje("Conectado a: " + device.getName());
                    tvEstado.setText("Estado: Conectado a " + device.getName());
                    imprimirMulta();
                    return;
                } catch (IOException e) {
                    mostrarMensaje("Error al conectar con " + device.getName() + ": " + e.getMessage());
                }
            }
            mostrarMensaje("No se pudo conectar a ninguna impresora");
            tvEstado.setText("Estado: No se pudo conectar a ninguna impresora");
        } catch (SecurityException e) {
            mostrarMensaje("Error de seguridad: " + e.getMessage());
            tvEstado.setText("Estado: Error de seguridad en Bluetooth");
        }
    }



    public void imprimirMulta() {
        // Verificar los permisos necesarios
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                mostrarMensaje("No se tienen los permisos necesarios para imprimir");
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                mostrarMensaje("No se tienen los permisos necesarios para imprimir");
                return;
            }
        }

        // Verificar la conexión Bluetooth
        if (bluetoothSocket == null || !bluetoothSocket.isConnected()) {
            mostrarMensaje("No hay conexión con la impresora");
            return;
        }

        try {
            outputStream = bluetoothSocket.getOutputStream();

            // Inicializar la impresora
            outputStream.write(new byte[]{0x1B, 0x40}); // Reset
            outputStream.write(new byte[]{0x1B, 0x4D, 0x01}); // Fuente pequeña

            int lineWidth = 32; // Caracteres por línea para 57mm

            // Obtener la fecha y hora actual
            Calendar calendar = Calendar.getInstance();
            int dia = calendar.get(Calendar.DAY_OF_MONTH);
            int mes = calendar.get(Calendar.MONTH) + 1;
            int anio = calendar.get(Calendar.YEAR);
            int hora = calendar.get(Calendar.HOUR_OF_DAY);
            int minuto = calendar.get(Calendar.MINUTE);

            // Imprimir cada campo
            imprimirCampoEnLinea("SERIE A - 2024", "", lineWidth);
            imprimirCampoEnLinea("Numero de Boleta", tvNumero.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Fecha", String.format("%02d/%02d/%04d", dia, mes, anio), lineWidth);
            imprimirCampoEnLinea("Hora", String.format("%02d:%02d", hora, minuto), lineWidth);
            imprimirCampoEnLinea("Apellido y Nombre", etApellidoNombre.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Domicilio", etDomicilio.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Localidad", etLocalidad.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Codigo Postal", etCodPostal.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Departamento", etDepartamento.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Provincia", etProvincia.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Pais", etPais.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Licencia", etLicencia.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Tipo de Documento", spinnerTipoDocumento.getSelectedItem().toString(), lineWidth);
            imprimirCampoEnLinea("Numero de Documento", etNumeroDocumento.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Propietario", etPropietario.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Dominio", etDominio.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Marca", spinnerMarca.getSelectedItem().toString(), lineWidth);
            imprimirCampoEnLinea("Modelo Vehículo", etModeloVehiculo.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Tipo de Vehiculo", spinnerTipoVehiculo.getSelectedItem().toString(), lineWidth);
            imprimirCampoEnLinea("Ubicacion actual", ubicacionActual, lineWidth);
            imprimirCampoEnLinea("Lugar de infraccion", etLugar.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Departamento", etDepartamentoMulta.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Municipio", etMunicipioMulta.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Informacion de la multa", etMultaInfo.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Cinemometro", etModeloCinemometro.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Modelo Cinemometro", etModeloCinemometro.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Equipo", etEquipo.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Marca", etMarcaCinemometro.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Modelo", etModeloCinemometro.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Numero de Serie", etSerieCinemometro.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Codigo de Aprobacion", etCodAprobacionCinemometro.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Valor", etValorCinemometro.getText().toString(), lineWidth);


            // Imprimir la imagen
            printPhoto(R.drawable.prueba);

            // Avanzar papel y cortar
            outputStream.write(new byte[]{0x0A, 0x0A, 0x0A, 0x0A});
            outputStream.write(new byte[]{0x1D, 0x56, 0x01}); // Corte parcial

            outputStream.flush();
            mostrarMensaje("Multa impresa con Éxito");

        } catch (IOException e) {
            mostrarMensaje("Error al imprimir: " + e.getMessage());
        } catch (SecurityException e) {
            mostrarMensaje("Error de seguridad al imprimir: " + e.getMessage());
        }
    }

    public void printPhoto(int img) {
        try {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), img);
            if(bmp != null){
                byte[] command = Utils.decodeBitmap(bmp);
                outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                printText(command);
            } else {
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }
    }

    private void printText(byte[] msg) {
        try {
            // Print normal text
            outputStream.write(msg);
            printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printNewLine() {
        try {
            outputStream.write(PrinterCommands.FEED_LINE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void imprimirCampoEnLinea(String etiqueta, String valor, int lineWidth) throws IOException {
        if (valor == null) {
            valor = "";
        }

        StringBuilder linea = new StringBuilder(etiqueta + ": " + valor);

        while (linea.length() > 0) {
            if (linea.length() <= lineWidth) {
                outputStream.write((linea.toString() + "\n").getBytes());
                break;
            } else {
                String lineaParcial = linea.substring(0, lineWidth);
                outputStream.write((lineaParcial + "\n").getBytes());
                linea = new StringBuilder(linea.substring(lineWidth));
            }
        }
    }
    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
