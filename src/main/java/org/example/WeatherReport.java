package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherReport {

    private static final String API_KEY = System.getenv("YANDEX_WEATHER_TOKEN");
    private static final String BASE_URL = "https://api.weather.yandex.ru/v2/forecast";

    public static void main(String[] args) {
        double latitude = 55.75; // Широта Москвы
        double longitude = 37.62; // Долгота Москвы
        int forecastDays = 7; // Количество дней прогноза

        if (isApiKeyMissing()) {
            return;
        }

        try {
            String requestUrl = buildRequestUrl(latitude, longitude, forecastDays);
            HttpResponse<String> weatherResponse = sendWeatherRequest(requestUrl);

            if (weatherResponse.statusCode() == 200) {
                handleWeatherResponse(weatherResponse.body());
            } else {
                System.err.println("Ошибка при запросе данных: " + weatherResponse.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static boolean isApiKeyMissing() {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("API-ключ не найден. Установите переменную окружения 'YANDEX_WEATHER_TOKEN'.");
            return true;
        }
        return false;
    }

    private static String buildRequestUrl(double latitude, double longitude, int limit) {
        return String.format("%s?lat=%s&lon=%s&limit=%d", BASE_URL, latitude, longitude, limit);
    }

    private static HttpResponse<String> sendWeatherRequest(String url) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Yandex-Weather-Key", API_KEY)
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static void handleWeatherResponse(String responseBody) {
        JSONObject jsonResponse = new JSONObject(responseBody);

        System.out.println(jsonResponse);

        JSONObject fact = jsonResponse.getJSONObject("fact");
        int currentTemperature = fact.getInt("temp");
        System.out.println("Текущая температура: " + currentTemperature + "°C");

        JSONArray forecasts = jsonResponse.getJSONArray("forecasts");
        double averageTemperature = calculateAverageTemperature(forecasts);
        System.out.printf("Средняя температура за указанный период: %.2f°C\n", averageTemperature);
    }

    private static double calculateAverageTemperature(JSONArray forecasts) {
        int temperatureSum = 0;
        int daysCount = forecasts.length();

        for (int i = 0; i < daysCount; i++) {
            JSONObject day = forecasts.getJSONObject(i);
            JSONObject parts = day.getJSONObject("parts");
            JSONObject dayPart = parts.getJSONObject("day");
            temperatureSum += dayPart.getInt("temp_avg");
        }

        return (double) temperatureSum / daysCount;
    }
}