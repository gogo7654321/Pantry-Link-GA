package com.example.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Response structures for Google Places Autocomplete API
data class PlacesAutocompleteResponse(
    val predictions: List<AutocompletePrediction> = emptyList(),
    val status: String
)

data class AutocompletePrediction(
    val description: String,
    val place_id: String,
    val structured_formatting: StructuredFormatting? = null
)

data class StructuredFormatting(
    val main_text: String,
    val secondary_text: String? = null
)

// Response structures for Google Places Details API
data class PlaceDetailsResponse(
    val result: PlaceDetailsResult?,
    val status: String
)

data class PlaceDetailsResult(
    val address_components: List<AddressComponent> = emptyList(),
    val formatted_address: String? = null
)

data class AddressComponent(
    val long_name: String,
    val short_name: String,
    val types: List<String>
)

interface GooglePlacesApiService {
    @GET("maps/api/place/autocomplete/json")
    suspend fun autocomplete(
        @Query("input") input: String,
        @Query("key") apiKey: String,
        @Query("types") types: String = "address",
        @Query("components") components: String = "country:us"
    ): PlacesAutocompleteResponse

    @GET("maps/api/place/details/json")
    suspend fun getDetails(
        @Query("place_id") placeId: String,
        @Query("key") apiKey: String,
        @Query("fields") fields: String = "address_components"
    ): PlaceDetailsResponse
}

object GooglePlacesClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GooglePlacesApiService = retrofit.create(GooglePlacesApiService::class.java)

    // Check if the API key is valid (not a placeholder)
    fun isApiKeyValid(apiKey: String): Boolean {
        return apiKey.isNotBlank() && 
               !apiKey.contains("PLACEHOLDER") && 
               !apiKey.contains("YOUR_") && 
               !apiKey.contains("MY_") &&
               apiKey.length > 15
    }

    // High-fidelity search-as-you-type Mock implementation for elegant local-first experience
    fun mockAutocomplete(query: String): List<AutocompletePrediction> {
        if (query.isBlank()) return emptyList()
        val trimmed = query.trim().lowercase()

        val database = listOf(
            Pair("1722 Peachtree Rd NW, Atlanta, GA, USA", "ChIJuSg22v8E9YgR9PeeO_uS9A0"),
            Pair("75 Piedmont Ave NE, Atlanta, GA, USA", "ChIJS_kS7V8E9YgRuEeeOnS9X04"),
            Pair("100 Cobb Pkwy SE, Smyrna, GA, USA", "ChIJmZ9kSg8E9YgRLeeOOoS9G12"),
            Pair("1200 Marietta Blvd NW, Atlanta, GA, USA", "ChIJeePjS88E9YgRM_eeO_uS632"),
            Pair("650 Ponce De Leon Ave NE, Atlanta, GA, USA", "ChIJgPeeSVE9YgRR_eeO0oS9P98"),
            Pair("1280 West Peachtree St NW, Atlanta, GA, USA", "ChIJzeePeVEE9YgRv_eeO2uS443"),
            Pair("100 Northside Dr NW, Atlanta, GA, USA", "ChIJsEee_VIE9YgRt_eeOSuS182"),
            Pair("520 Powder Springs St, Marietta, GA, USA", "ChIJf_kfSg4E9YgRneeO_uS9921"),
            Pair("1300 Abernathy Rd NE, Sandy Springs, GA, USA", "ChIJ0_ffSgAE9YgReeeO_uS2032"),
            Pair("440 Atlanta St, Roswell, GA, USA", "ChIJS_ffSgEE9YgR_eeO_uS9281"),
            Pair("1600 Amphitheatre Pkwy, Mountain View, CA, USA", "ChIJ2eUgeAK6j4ARbn5u_w7SPi4"),
            Pair("1600 Pennsylvania Ave NW, Washington, DC, USA", "ChIJGV7TI_0EyEkR79698p3LQi8"),
            Pair("350 Fifth Ave, New York, NY, USA", "ChIJ4zGv77b1wokRni4Y2S0")
        )

        val matches = database.filter { it.first.lowercase().contains(trimmed) }
        
        // Dynamic search-as-you-type options generator for address-like entries
        val words = query.trim().split(" ")
        val firstWord = words.firstOrNull() ?: ""
        val isNumeric = firstWord.all { it.isDigit() }
        val dynamicMatches = mutableListOf<Pair<String, String>>()
        if (isNumeric && firstWord.length >= 1) {
            val remaining = query.trim().removePrefix(firstWord).trim()
            val popularStreets = listOf(
                "Peachtree St", "Piedmont Ave", "Main St", "Northside Dr", "Cobbs Rd",
                "Marietta Blvd", "Ponce De Leon Ave", "Webb Bridge Rd", "Broadway", "Peachtree Rd NW"
            )
            val filteredStreets = if (remaining.isNotBlank()) {
                popularStreets.filter { it.lowercase().contains(remaining.lowercase()) }
            } else {
                popularStreets
            }
            filteredStreets.forEachIndexed { i, street ->
                dynamicMatches.add(Pair("$firstWord $street, Atlanta, GA, USA", "ch_dyn_${firstWord}_$i"))
            }
        }

        val combined = (matches + dynamicMatches).distinctBy { it.second }.take(6)
        return combined.map { item ->
            val parts = item.first.split(",")
            val mainText = parts.getOrNull(0)?.trim() ?: item.first
            val secText = parts.drop(1).joinToString(",").trim()
            AutocompletePrediction(
                description = item.first,
                place_id = item.second,
                structured_formatting = StructuredFormatting(
                    main_text = mainText,
                    secondary_text = secText
                )
            )
        }
    }

    // Function to mock Place Details call
    fun mockDetails(placeId: String): PlaceDetailsResponse {
        if (placeId.startsWith("ch_dyn_")) {
            val parsed = placeId.removePrefix("ch_dyn_").split("_")
            val num = parsed.getOrNull(0) ?: "100"
            val idxStr = parsed.getOrNull(1) ?: "0"
            val idx = idxStr.toIntOrNull() ?: 0
            val popularStreets = listOf(
                "Peachtree St", "Piedmont Ave", "Main St", "Northside Dr", "Cobbs Rd",
                "Marietta Blvd", "Ponce De Leon Ave", "Webb Bridge Rd", "Broadway", "Peachtree Rd NW"
            )
            val street = popularStreets.getOrElse(idx) { "Peachtree St" }
            return PlaceDetailsResponse(
                result = PlaceDetailsResult(
                    address_components = listOf(
                        AddressComponent(num, num, listOf("street_number")),
                        AddressComponent(street, street, listOf("route")),
                        AddressComponent("Atlanta", "Atlanta", listOf("locality")),
                        AddressComponent("GA", "GA", listOf("administrative_area_level_1")),
                        AddressComponent("30303", "30303", listOf("postal_code")),
                        AddressComponent("United States", "US", listOf("country"))
                    ),
                    formatted_address = "$num $street, Atlanta, GA 30303, USA"
                ),
                status = "OK"
            )
        }

        val mapping = mapOf(
            "ChIJuSg22v8E9YgR9PeeO_uS9A0" to Pair("1722 Peachtree Rd NW", Triple("Atlanta", "GA", "30309")),
            "ChIJS_kS7V8E9YgRuEeeOnS9X04" to Pair("75 Piedmont Ave NE", Triple("Atlanta", "GA", "30303")),
            "ChIJmZ9kSg8E9YgRLeeOOoS9G12" to Pair("100 Cobb Pkwy SE", Triple("Smyrna", "GA", "30080")),
            "ChIJeePjS88E9YgRM_eeO_uS632" to Pair("1200 Marietta Blvd NW", Triple("Atlanta", "GA", "30318")),
            "ChIJgPeeSVE9YgRR_eeO0oS9P98" to Pair("650 Ponce De Leon Ave NE", Triple("Atlanta", "GA", "30308")),
            "ChIJzeePeVEE9YgRv_eeO2uS443" to Pair("1280 West Peachtree St NW", Triple("Atlanta", "GA", "30309")),
            "ChIJsEee_VIE9YgRt_eeOSuS182" to Pair("100 Northside Dr NW", Triple("Atlanta", "GA", "30314")),
            "ChIJf_kfSg4E9YgRneeO_uS9921" to Pair("520 Powder Springs St", Triple("Marietta", "GA", "30064")),
            "ChIJ0_ffSgAE9YgReeeO_uS2032" to Pair("1300 Abernathy Rd NE", Triple("Sandy Springs", "GA", "30328")),
            "ChIJS_ffSgEE9YgR_eeO_uS9281" to Pair("440 Atlanta St", Triple("Roswell", "GA", "30075")),
            "ChIJ2eUgeAK6j4ARbn5u_w7SPi4" to Pair("1600 Amphitheatre Pkwy", Triple("Mountain View", "CA", "94043")),
            "ChIJGV7TI_0EyEkR79698p3LQi8" to Pair("1600 Pennsylvania Ave NW", Triple("Washington", "DC", "20500")),
            "ChIJ4zGv77b1wokRni4Y2S0" to Pair("350 Fifth Ave", Triple("New York", "NY", "10118"))
        )

        val item = mapping[placeId] ?: Pair("1722 Peachtree Rd NW", Triple("Atlanta", "GA", "30309"))
        val street = item.first
        val city = item.second.first
        val state = item.second.second
        val zip = item.second.third

        val parts = street.split(" ")
        val streetNo = parts.firstOrNull() ?: ""
        val route = street.removePrefix(streetNo).trim()

        return PlaceDetailsResponse(
            result = PlaceDetailsResult(
                address_components = listOf(
                    AddressComponent(streetNo, streetNo, listOf("street_number")),
                    AddressComponent(route, route, listOf("route")),
                    AddressComponent(city, city, listOf("locality")),
                    AddressComponent(state, state, listOf("administrative_area_level_1")),
                    AddressComponent(zip, zip, listOf("postal_code")),
                    AddressComponent("United States", "US", listOf("country"))
                ),
                formatted_address = "$street, $city, $state $zip, USA"
            ),
            status = "OK"
        )
    }
}
