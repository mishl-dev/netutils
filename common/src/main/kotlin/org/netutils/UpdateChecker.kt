package org.netutils

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.concurrent.thread

/**
 * This class only makes a single GET request to GitHub's public API.
 * It does NOT download files, execute code, or open browsers automatically.
 */
object UpdateChecker {
    private const val GITHUB_API_URL = "https://api.github.com/repos/mishl-dev/NetUtils/releases/latest"
    
    var updateAvailable = false
        private set
    var latestVersion: String? = null
        private set
    const val currentVersion = "1.0.0" 
    
    /**
     * Asynchronously check GitHub for the latest release.
     * This ONLY reads version info - no downloads, no browser opens.
     */
    fun checkForUpdates() {
        thread(name = "NetUtils-UpdateChecker", isDaemon = true) {
            try {
                val client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build()
                
                val request = HttpRequest.newBuilder()
                    .uri(URI.create(GITHUB_API_URL))
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "NetUtils/$currentVersion")
                    .GET()
                    .build()
                
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                
                if (response.statusCode() == 200) {
                    val json = Gson().fromJson(response.body(), JsonObject::class.java)
                    // Remove 'v' prefix if present for comparison
                    json?.get("tag_name")?.asString?.removePrefix("v")?.let { version ->
                        latestVersion = version
                        updateAvailable = version != currentVersion
                        
                        if (updateAvailable) {
                            NetUtilsCommon.LOGGER.info("Update available: $version (current: $currentVersion)")
                        } else {
                            NetUtilsCommon.LOGGER.info("NetUtils is up to date ($currentVersion)")
                        }
                    }
                } else {
                    NetUtilsCommon.LOGGER.debug("Update check returned status: ${response.statusCode()}")
                }
            } catch (e: Exception) {
                NetUtilsCommon.LOGGER.debug("Update check failed: ${e.message}")
                // Silent failure - don't bother user if update check fails
            }
        }
    }
    
    /**
     * Get the GitHub releases page URL for manual update.
     * User must click this themselves - we never open it automatically.
     */
    fun getReleasesUrl() = "https://github.com/mishl-dev/NetUtils/releases"
}

