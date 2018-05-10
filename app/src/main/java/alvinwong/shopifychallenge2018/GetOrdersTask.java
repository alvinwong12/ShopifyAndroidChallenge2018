package alvinwong.shopifychallenge2018;

//import android.content.Context;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetOrdersTask extends AsyncTask<Void, Void, String> {

    private String URL = "https://shopicruit.myshopify.com/admin/orders.json?page=1&access_token=c32313df0d0ef512ca64d5b336a0d7c6";
    private String TAG = "TASK";
    OkHttpClient client = new OkHttpClient();
    private Activity activity;

    public GetOrdersTask(Activity activity){
        this.activity = activity;
    }

    @Override
    protected String doInBackground(Void... voids) {
        String ordersString = "";
        try {
            ordersString = retreiveOrdersString();

        }catch (IOException e) {
            ordersString = "";
        }finally {
            return ordersString;
        }
    }

    protected void onPostExecute(String ordersString) {
        JSONArray orders;
        LinearLayout list1 = (LinearLayout) activity.findViewById(R.id.list1);
        try {
            orders = parseOrders(ordersString);

        }catch (JSONException e){
            orders = new JSONArray();
        }
        SortedMap<String, Integer> sortedProvinceOrders = orderByProvince(orders);
        SortedMap<String, Integer> sortedYearsOrders = orderByYears(orders);
        displayListProvince(sortedProvinceOrders);
        displayListYears(sortedYearsOrders);
    }

    private String retreiveOrdersString() throws IOException {
        Request request = new Request.Builder()
                .url(URL)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();

        }
    }

    private JSONArray parseOrders(String ordersString) throws JSONException {
        JSONArray orders;
        JSONObject obj = new JSONObject(ordersString);
        orders = obj.getJSONArray("orders");
        return orders;
    }

    private SortedMap<String,Integer> orderByProvince(JSONArray orders){
        SortedMap<String,Integer> obp = new TreeMap<String, Integer>();

        for(int i = 0; i < orders.length() ; i++){
            String province;
            try {
                province = orders.getJSONObject(i).getJSONObject("shipping_address").getString("province");

            } catch (JSONException e){
                // Continue because JSON missing key
                province = null;
            }

            try{
                if (province != null){
                    int amount = obp.get(province);
                    obp.put(province, ++amount);
                }
            }catch(NullPointerException e){
                obp.put(province, 1);
            }
        }
        return obp;
    };

    private SortedMap<String,Integer> orderByYears(JSONArray orders){

        SortedMap<String,Integer> obp = new TreeMap<String, Integer>();
        for(int i = 0; i < orders.length() ; i++){
            String formattedDate, createdAt;
            try{
                createdAt = orders.getJSONObject(i).getString("created_at");
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
                Date date = parser.parse(createdAt);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
                formattedDate = formatter.format(date);
            } catch (JSONException e) {
                formattedDate = null;

            } catch (ParseException e) {
                formattedDate = null;
            }
            try{
                if (formattedDate != null){
                    int amount = obp.get(formattedDate);
                    obp.put(formattedDate, ++amount);
                }
            }  catch (NullPointerException e){
                obp.put(formattedDate, 1);
            }
        }
        return obp;
    };

    private void displayListProvince(SortedMap<String,Integer> obp){
        LinearLayout list1 = (LinearLayout) activity.findViewById(R.id.list1);
        Set s = obp.entrySet();
        Iterator i = s.iterator();
        while (i.hasNext()){
            Map.Entry m = (Map.Entry)i.next();

            String key = (String)m.getKey();
            int value = (Integer) m.getValue();

            TextView item = new TextView(activity);
            item.setText(value + " number of orders from " + key);
            list1.addView(item);
        }
    }
    private void displayListYears(SortedMap<String,Integer> obp){
        LinearLayout list2 = (LinearLayout) activity.findViewById(R.id.list2);
        Set s = obp.entrySet();
        try {
            int amount = obp.get("2017");
            TextView item = new TextView(activity);
            item.setText(amount + " orders in 2017 ");
            list2.addView(item);
        }catch (NullPointerException e){
            TextView item = new TextView(activity);
            item.setText("0 orders in 2017");
            list2.addView(item);
        }

    }

}
