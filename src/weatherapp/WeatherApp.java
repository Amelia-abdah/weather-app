/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package weatherapp;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

/**
 *
 * @author User
 */
public class WeatherApp extends JFrame{
    private JTextField cityInput;
    private JButton checkWeatherButton, saveButton, loadButton;
    private JLabel weatherLabel, iconLabel;
    private JComboBox<String> favoriteCities;
    private JTable weatherTable;
    private DefaultTableModel tableModel;
    
    public WeatherApp() {
        setTitle("Cek Cuaca Sederhana");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel Input
        JPanel inputPanel = new JPanel();
        cityInput = new JTextField(15);
        checkWeatherButton = new JButton("Cek Cuaca");
        favoriteCities = new JComboBox<>();
        favoriteCities.addItem("Pilih Kota Favorit");
        inputPanel.add(new JLabel("Kota:"));
        inputPanel.add(cityInput);
        inputPanel.add(checkWeatherButton);
        inputPanel.add(favoriteCities);

        // Panel Output
        JPanel outputPanel = new JPanel(new BorderLayout());
        weatherLabel = new JLabel("Data cuaca akan muncul di sini.", JLabel.CENTER);
        iconLabel = new JLabel("", JLabel.CENTER);
        outputPanel.add(weatherLabel, BorderLayout.CENTER);
        outputPanel.add(iconLabel, BorderLayout.SOUTH);

        // Panel Tabel
        JPanel tablePanel = new JPanel(new BorderLayout());
        String[] columnNames = {"Kota", "Deskripsi Cuaca", "Suhu"};
        tableModel = new DefaultTableModel(columnNames, 0);
        weatherTable = new JTable(tableModel);
        tablePanel.add(new JScrollPane(weatherTable), BorderLayout.CENTER);

        saveButton = new JButton("Simpan Data ke CSV");
        loadButton = new JButton("Muat Data dari CSV");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);

        add(inputPanel, BorderLayout.NORTH);
        add(outputPanel, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.SOUTH);

        // Event Handling
        checkWeatherButton.addActionListener(e -> fetchWeather());
        favoriteCities.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                cityInput.setText((String) favoriteCities.getSelectedItem());
            }
        });
        saveButton.addActionListener(e -> saveData());
        loadButton.addActionListener(e -> loadData());

        setSize(600, 400);
        setVisible(true);
    }

    private void fetchWeather() {
        String city = cityInput.getText();
        if (city.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan nama kota!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String apiKey = "84de59b5873d42bbeb09aff6f28c55e6"; // Ganti dengan API key Anda
            String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey + "&units=metric";
            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();

                parseWeatherData(response.toString(), city);
            } else {
                JOptionPane.showMessageDialog(this, "Kota tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mendapatkan data cuaca!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void parseWeatherData(String jsonData, String city) {
        JSONObject json = new JSONObject(jsonData);
        String description = json.getJSONArray("weather").getJSONObject(0).getString("description");
        double temp = json.getJSONObject("main").getDouble("temp");

        weatherLabel.setText("<html><h3>Kota: " + city + "</h3>" +
                "<p>Deskripsi Cuaca: " + description + "</p>" +
                "<p>Suhu: " + temp + " °C</p></html>");
        favoriteCities.addItem(city);

        // Update table
        tableModel.addRow(new Object[]{city, description, temp});
    }

    private void saveData() {
        try (FileWriter writer = new FileWriter("weather_data.csv")) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    writer.write(tableModel.getValueAt(i, j) + ",");
                }
                writer.write("\n");
            }
            JOptionPane.showMessageDialog(this, "Data cuaca berhasil disimpan ke CSV!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadData() {
        try (BufferedReader reader = new BufferedReader(new FileReader("weather_data.csv"))) {
            String line;
            tableModel.setRowCount(0); // Clear table
            while ((line = reader.readLine()) != null) {
                tableModel.addRow(line.split(","));
            }
            JOptionPane.showMessageDialog(this, "Data cuaca berhasil dimuat dari CSV!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WeatherApp::new);
    }
    
}
