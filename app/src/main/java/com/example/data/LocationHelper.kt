package com.example.data

import android.content.Context
import android.location.Geocoder
import android.util.Log

object LocationHelper {
    // Dynamic Geocoder that uses Android Location API to fetch PRECISE coordinates for a formatted address
    fun getPreciseCoords(context: Context?, address: String, zip: String): Pair<Double, Double>? {
        if (context == null) return null
        val query = listOf(address.trim(), zip.trim())
            .filter { it.isNotEmpty() }
            .joinToString(", ")
        
        if (query.isEmpty() || query.lowercase().trim() == "georgia" || query.lowercase().trim() == "ga") {
            return null
        }
        
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context)
                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocationName(query, 1)
                if (!results.isNullOrEmpty()) {
                    val lat = results[0].latitude
                    val lng = results[0].longitude
                    Log.d("LocationHelper", "Geocoded successfully: '$query' -> ($lat, $lng)")
                    return Pair(lat, lng)
                }
            }
        } catch (e: Exception) {
            Log.e("LocationHelper", "Geocoding failed for: '$query'", e)
        }
        return null
    }

    fun getCoordsForAddress(address: String, zip: String): Pair<Double, Double> {
        return getCoordsForAddressWithContext(null, address, zip)
    }

    fun getCoordsForAddressWithContext(context: Context?, address: String, zip: String): Pair<Double, Double> {
        // Try precise geocoding first!
        val precise = getPreciseCoords(context, address, zip)
        if (precise != null) {
            return precise
        }

        // Graceful fallbacks if geocoding fails or isn't available
        val addrLower = address.trim().lowercase()
        val zipTrim = zip.trim()
        return when {
            // Smyrna - ChIJmZ9kSg8E9YgRLeeOOoS9G12 (100 Cobb Pkwy SE) or ZIP
            addrLower.contains("smyrna") || addrLower.contains("cobb pkwy") || zipTrim == "30080" || zipTrim == "30082" -> Pair(33.8821, -84.4811)
            // Kennesaw / Kennesaw State University - Cobb County
            addrLower.contains("kennesaw") || zipTrim == "30144" || zipTrim == "30152" || addrLower.contains("ksu") -> Pair(34.0234, -84.6155)
            // Acworth
            addrLower.contains("acworth") || zipTrim == "30101" || zipTrim == "30102" -> Pair(34.0659, -84.6769)
            // Woodstock
            addrLower.contains("woodstock") || zipTrim == "30188" || zipTrim == "30189" -> Pair(34.1013, -84.5194)
            // Marietta - ChIJf_kfSg4E9YgRneeO_uS9921 (520 Powder Springs St) or ZIP
            addrLower.contains("marietta") || addrLower.contains("powder springs") || zipTrim == "30064" || zipTrim == "30060" || zipTrim == "30008" -> Pair(33.9407, -84.5587)
            // Sandy Springs - ChIJ0_ffSgAE9YgReeeO_uS2032 (1300 Abernathy Rd NE) or ZIP
            addrLower.contains("sandy springs") || addrLower.contains("abernathy") || zipTrim == "30328" || zipTrim == "30350" -> Pair(33.9379, -84.3486)
            // Roswell - ChIJS_ffSgEE9YgR_eeO_uS9281 (440 Atlanta St) or ZIP
            addrLower.contains("roswell") || addrLower.contains("atlanta st") || zipTrim == "30075" || zipTrim == "30076" -> Pair(34.0175, -84.3612)
            // Peachtree Rd - 1722 Peachtree Rd NW
            addrLower.contains("1722 peachtree") || zipTrim == "30309" -> Pair(33.8016, -84.3897)
            // Piedmont Ave - 75 Piedmont Ave NE
            addrLower.contains("75 piedmont") || zipTrim == "30303" -> Pair(33.7563, -84.3853)
            // Marietta Blvd - 1200 Marietta Blvd NW
            addrLower.contains("marietta blvd") || zipTrim == "30318" -> Pair(33.7885, -84.4285)
            // Ponce De Leon - 650 Ponce De Leon Ave NE
            addrLower.contains("ponce de leon") || zipTrim == "30308" || zipTrim == "30344" -> Pair(33.7725, -84.3663)
            // West Peachtree - 1280 West Peachtree St NW
            addrLower.contains("1280 west peachtree") -> Pair(33.7903, -84.3879)
            // Northside Dr - 100 Northside Dr NW
            addrLower.contains("northside") || zipTrim == "30314" -> Pair(33.7569, -84.4079)
            // Other Georgian Cities
            zipTrim == "30507" || zipTrim == "30501" -> Pair(34.2582, -83.8185) // Gainesville
            zipTrim == "30909" -> Pair(33.4735, -82.0649) // Augusta
            zipTrim == "31401" -> Pair(32.0809, -81.0912) // Savannah
            zipTrim == "30601" || zipTrim == "30605" || zipTrim == "30606" -> Pair(33.9519, -83.3576) // Athens
            else -> getCoordsForZip(zip)
        }
    }

    fun getCoordsForZip(zip: String): Pair<Double, Double> {
        val zipTrim = zip.trim()
        return when (zipTrim) {
            "30308", "30344" -> Pair(33.7756, -84.3963)
            "30075", "30076" -> Pair(34.0232, -84.3615)
            "30507", "30501" -> Pair(34.2582, -83.8185)
            "30080", "30082" -> Pair(33.8821, -84.4811) // Smyrna
            "30064", "30060" -> Pair(33.9407, -84.5587) // Marietta
            "30328" -> Pair(33.9379, -84.3486) // Sandy Springs
            "30144", "30152" -> Pair(34.0234, -84.6155) // Kennesaw
            "30101", "30102" -> Pair(34.0659, -84.6769) // Acworth
            "30188", "30189" -> Pair(34.1013, -84.5194) // Woodstock
            else -> Pair(33.7490, -84.3880) // default GA Atlanta center
        }
    }
}
