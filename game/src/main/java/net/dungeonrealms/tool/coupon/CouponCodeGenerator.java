package net.dungeonrealms.tool.coupon;

import com.google.gson.*;
import io.netty.util.concurrent.CompleteFuture;
import lombok.SneakyThrows;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Created by Rar349 on 7/7/2017.
 */
public class CouponCodeGenerator {

    private static final String API_KEY = "uZ0gr8985RK6SRPttqF863rHCURzM5Pprwb875RjjWYNVbc4";

    //public static void generate

    public static void generatePercentCouponCode(int discountAmount,UUID whoClaimed, Consumer<String> callback) {
        generateCouponCode(true,callback, discountAmount, true, 1, 1, new UUID[] {whoClaimed}, null);
    }

    public static void generateCouponCode(boolean runAsync,Consumer<String> callback,int discountAmount, boolean isPercentDiscount, int numberOfUses, int numberOfUsesPerPerson, UUID[] uuidsWhoCanClaim, int... itemIdsThatApply) {

        if(runAsync) {
            CompletableFuture.runAsync(() -> {
                generateCouponCode(false, callback, discountAmount, isPercentDiscount, numberOfUses, numberOfUsesPerPerson, uuidsWhoCanClaim, itemIdsThatApply);
            });
            return;
        }

        try {

            JsonObject object = new JsonObject();
            object.addProperty("jsonrpc", "2.0");
            object.addProperty("id", "12345");
            JsonObject params = new JsonObject();
            params.addProperty("api_key", API_KEY);
            params.addProperty("preset_id", 20237813);
            params.addProperty("discount_amount", discountAmount);
            params.addProperty("discount_type", isPercentDiscount ? "percent" : "value");
            params.addProperty("expiry_type", "redeem_limit");
            if (itemIdsThatApply != null) {
                JsonArray array = new JsonArray();
                for (int itemId : itemIdsThatApply) array.add(new JsonPrimitive(itemId));
                params.add("effective_on", array);
            }
            params.addProperty("start_date", System.currentTimeMillis() / 1000);
            params.addProperty("expiry_value", numberOfUses);
            params.addProperty("redeem_limit_per_user", numberOfUsesPerPerson);
            if (uuidsWhoCanClaim != null) {
                JsonArray array = new JsonArray();
                for (UUID uuid : uuidsWhoCanClaim) array.add(new JsonPrimitive(uuid.toString()));
                params.add("restrict_to_mc_players", array);
            }
            object.add("params", params);
            object.addProperty("method", "Shop.createCoupon");


            String postUrl = "http://www.dungeonrealms.net/api/v1/api.php";// put in your url
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost(postUrl);
            StringEntity postingString = new StringEntity(object.toString());
            post.setEntity(postingString);
            post.setHeader("Content-type", "application/json");
            HttpResponse response = httpClient.execute(post);
            if (response == null) {
                callback.accept(null);
                return;
            }

            InputStream in = response.getEntity().getContent();
            JsonParser parser = new JsonParser();
            JsonObject responseObject = (JsonObject) parser.parse(new InputStreamReader(in, "UTF-8"));
            if(responseObject == null) {
                callback.accept(null);
                return;
            }

            JsonObject result = (JsonObject)responseObject.get("result");
            if(result == null) {
                callback.accept(null);
                return;
            }

            JsonArray code = result.getAsJsonArray("coupon_codes");
            JsonElement codeElement = code.get(0);
            if(codeElement == null) {
                callback.accept(null);
                return;
            }


            String codePrimitive = codeElement.getAsString();
            callback.accept(codePrimitive);
        } catch(Exception e) {
            e.printStackTrace();
            callback.accept(null);
        }

    }
}
