package com.example.dms_app.feature_trips.presentation

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.dms_app.feature_trips.domain.TripLocation

@Composable
fun TripMapWebView(
    locations: List<TripLocation>,
    alerts: List<TripLocation>,
    modifier: Modifier = Modifier
) {
    val htmlContent = remember(locations, alerts) {
        generateMapHtml(locations, alerts)
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        }
    )
}

private fun generateMapHtml(locations: List<TripLocation>, alerts: List<TripLocation>): String {
    val pathData = locations.joinToString(",") { "[${it.latitude}, ${it.longitude}]" }
    val alertData = alerts.joinToString(",") { "[${it.latitude}, ${it.longitude}]" }
    val startLoc = if (locations.isNotEmpty()) "[${locations.first().latitude}, ${locations.first().longitude}]" else "null"
    val endLoc = if (locations.isNotEmpty()) "[${locations.last().latitude}, ${locations.last().longitude}]" else "null"

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                body { margin: 0; padding: 0; background: #0B0E14; }
                #map { height: 100vh; width: 100vw; background: #0B0E14; }
                .leaflet-container { background: #0B0E14 !important; }
                .leaflet-bar a { background-color: #1A1D24 !important; color: #00F2FF !important; border-bottom: 1px solid #2A2D35 !important; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', {
                    zoomControl: false,
                    attributionControl: false
                });

                // Premium Dark Matter tiles (No API key required for low volume)
                L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
                    maxZoom: 19
                }).addTo(map);

                var path = [$pathData];
                var alerts = [$alertData];
                var start = $startLoc;
                var end = $endLoc;

                if (path.length > 0) {
                    var polyline = L.polyline(path, {
                        color: '#00F2FF',
                        weight: 4,
                        opacity: 0.8,
                        lineJoin: 'round'
                    }).addTo(map);

                    map.fitBounds(polyline.getBounds(), { padding: [30, 30] });

                    // Markers
                    if (start) {
                        L.circleMarker(start, {
                            radius: 6,
                            fillColor: "#00E676",
                            color: "#fff",
                            weight: 2,
                            opacity: 1,
                            fillOpacity: 1
                        }).addTo(map);
                    }

                    if (end) {
                        L.circleMarker(end, {
                            radius: 6,
                            fillColor: "#00F2FF",
                            color: "#fff",
                            weight: 2,
                            opacity: 1,
                            fillOpacity: 1
                        }).addTo(map);
                    }

                    alerts.forEach(function(pos) {
                        L.circleMarker(pos, {
                            radius: 5,
                            fillColor: "#FF3B30",
                            color: "#fff",
                            weight: 1,
                            opacity: 1,
                            fillOpacity: 0.8
                        }).addTo(map);
                    });
                }
            </script>
        </body>
        </html>
    """.trimIndent()
}
