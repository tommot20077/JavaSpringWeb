indexWeather = document.getElementById('index-weather');
function fetchWeather() {
    const weatherCache = localStorage.getItem('weatherCache');
    const weatherData = weatherCache && JSON.parse(weatherCache);
    indexWeather.innerHTML =
        `<div class="loadingio-spinner-dual-ball-l2u3038qtw8">
                     <div class="ldio-4pqo44ipw4">
                     <div></div><div></div><div></div>
                     </div></div>`;

    if (weatherData && (new Date().getTime() - weatherData.timestamp < 1000 * 60 * 15)) {
        updateIndexWithWeatherData(weatherData.data);
    } else {
        fetch(`/weather`)
            .then(response => {
                if (!response.ok) {
                    return response.text().then(textBody => {
                        throw new Error(textBody);
                    });
                }
                return response.json();
            })
            .then(data => {
                updateIndexWithWeatherData(data);
                localStorage.setItem('weatherCache', JSON.stringify({
                    timestamp: new Date().getTime(),
                    data: data
                }));
            })
            .catch(error => {
                indexWeather.innerHTML = `<p>${error}</p>`;
            });
    }
}

function updateIndexWithWeatherData(weatherData) {
    const indexWeather = document.getElementById('index-weather');
    const iconUrl = `https:${weatherData.current.condition.icon}`;
    indexWeather.innerHTML = `
        <table>
            <tr>
                <th colspan="2"><h2>${weatherData.location.name} - ${weatherData.location.country} </h2></th>
            </tr>
            <tr>
                <td colspan="2"><h3>天氣: ${weatherData.current.condition.text}<img src="${iconUrl}" class="weather-icon" alt="Weather icon"></h3></td>
            </tr>
            <tr>
                <td>溫度:</td>
                <td>${weatherData.current.temp_c}° C</td>
            </tr>
            <tr>
                <td>體感溫度:</td>
                <td>${weatherData.current.feelslike_c}° C</td>
            </tr>
            <tr>
                <td>UV 指數:</td>
                <td>${weatherData.current.uv}</td>
            </tr>
            <tr>
                <td>風向:</td>
                <td>${weatherData.current.wind_dir}</td>
            </tr>
            <tr>
                <td>風速:</td>
                <td>${weatherData.current.wind_kph} km/h</td>
            </tr>
            <tr>
                <td>濕度:</td>
                <td>${weatherData.current.humidity}%</td>
            </tr>
            <tr>
                <td>降雨量:</td>
                <td>${weatherData.current.precip_mm} mm</td>
            </tr>
            
            <tr>
                <td>PM2.5:</td>
                <td>${weatherData.current.air_quality.pm2_5} mm</td>
            </tr>
            <tr>
                <td>US-EPA分級:</td>
                <td>${weatherData.current.air_quality['us-epa-index']}</td>
            </tr>
            <tr>
                <td>上次更新時間:</td>
                <td>${weatherData.location.localtime}</td>
            </tr>
        </table>
        <a href="https://www.weatherapi.com/" title="Free Weather API"><img src='//cdn.weatherapi.com/v4/images/weatherapi_logo.png' alt="Weather data by WeatherAPI.com"></a>
    `;
}