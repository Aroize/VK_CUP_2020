package ru.rain.ifmo.taskg.data.requests

import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONObject
import ru.rain.ifmo.taskg.App
import ru.rain.ifmo.taskg.data.models.VKAccountInfo
import ru.rain.ifmo.taskg.data.models.VKCity
import ru.rain.ifmo.taskg.foreach

class VKCitiesCommand : ApiCommand<List<VKCity>>() {
    override fun onExecute(manager: VKApiManager): List<VKCity> {
        val accountCall = VKMethodCall.Builder()
            .method("account.getInfo")
            .args("fields", "country,lang")
            .version(manager.config.version)
            .build()
        val accountInfo = manager.execute(accountCall, AccountInfoParser())
        val countryCodeCall = VKMethodCall.Builder()
            .method("database.getCountries")
            .args("code", accountInfo.country)
            .version(manager.config.version)
            .build()
        val countryCode = manager.execute(countryCodeCall, CountryCodeParser())
        val citiesCall = VKMethodCall.Builder()
            .method("database.getCities")
            .args(
                mapOf(
                    "lang" to "0",
                    "country_id" to "$countryCode"
                )
            )
            .version(manager.config.version)
            .build()
        return manager.execute(citiesCall, CitiesParser())
    }

    private class AccountInfoParser : VKApiResponseParser<VKAccountInfo> {
        override fun parse(response: String): VKAccountInfo {
            try {
                val json = JSONObject(response).getJSONObject("response")
                return VKAccountInfo(
                    country = json.optString("country"),
                    lang = json.optInt("lang")
                )
            } catch (e: Exception) {
                throw VKApiIllegalResponseException(response, e)
            }
        }
    }

    private class CountryCodeParser : VKApiResponseParser<Int> {
        override fun parse(response: String): Int {
            try {
                val json = JSONObject(response)
                    .getJSONObject("response")
                    .getJSONArray("items")
                    .getJSONObject(0)
                return json.optInt("id")
            } catch (e: Exception) {
                throw VKApiIllegalResponseException(response, e)
            }
        }
    }

    private class CitiesParser : VKApiResponseParser<List<VKCity>> {
        override fun parse(response: String): List<VKCity> {
            try {
                val json = JSONObject(response).getJSONObject("response")
                val result = ArrayList<VKCity>(json.optInt("count"))
                val items = json.getJSONArray("items")
                items.foreach {
                    it as JSONObject
                    result.add(VKCity.parse(it))
                }
                return result
            } catch (e: Exception) {
                throw VKApiIllegalResponseException(response, e)
            }
        }
    }
}