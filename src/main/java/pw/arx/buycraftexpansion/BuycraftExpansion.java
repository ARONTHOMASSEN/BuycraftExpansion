package pw.arx.buycraftexpansion;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class BuycraftExpansion extends PlaceholderExpansion {

	public static String configLocation = "plugins/PlaceholderAPI/buycraft-config.txt";
	
	public static String defaultURL = "https://plugin.tebex.io/payments";
	public static String defaultSecret = "ENTERYOURSECRETKEYHERE";
	
    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getIdentifier() {
        return "buycraft";
    }

    @Override
    public String getAuthor() {
        return "Fireblazer";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    public String onRequest(OfflinePlayer player, String identifier){
    	if(identifier.equals("top"))
			try {
				return getTopPlayer();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	return "";
    }
    
	private static String convertInputStreamToString(InputStream inputStream) 
		throws IOException {

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        return result.toString(StandardCharsets.UTF_8.name());

    }
	
	public static boolean readConfigFile() {
		
    	File configFile = new File(configLocation);

    	try {
			if(configFile.createNewFile()) {
				FileWriter fr = new FileWriter(configFile);
				fr.append("url:" + defaultURL +  "\n");
				fr.append("secret:" + defaultSecret);
				fr.close();
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return true;
	}
    
    public String getTopPlayer() throws IOException {
    	
    	String setURL = defaultURL;
    	String setSecret = defaultSecret;
    	
    	File configFile = new File(configLocation);

    	if(readConfigFile()) {
    		BufferedReader Buff = new BufferedReader(new FileReader(configFile));
    	    String url = Buff.readLine().substring(4);
    	    String secret = Buff.readLine().substring(7);
    	    
    	    setURL = url;
    	    setSecret = secret;
    	    Buff.close();
    	}
    	
    	HttpURLConnection hc;
        try {
            URL address = new URL(setURL);
            hc = (HttpURLConnection) address.openConnection();

            hc.setDoOutput(true);
            hc.setDoInput(true);
            hc.setUseCaches(false);

            hc.setRequestMethod("GET");
            hc.setRequestProperty("X-Tebex-Secret", setSecret);
            
            InputStream inputStr = hc.getInputStream();
            String JSONResponse = convertInputStreamToString(inputStr);
            
            JsonArray JSONObj = new JsonParser().parse(JSONResponse).getAsJsonArray();
            if(JSONObj.size() > 0) {
            	
            	HashMap<String, Double> playerDonations = new HashMap<String, Double>();
            	Map.Entry<String, Double> maxEntry = null;
            	
            	for(JsonElement elem : JSONObj) {
            		JsonObject paymentObj = elem.getAsJsonObject();
            		
            		String playerName = paymentObj.get("player").getAsJsonObject().get("name").getAsString();
            		Double amount = paymentObj.get("amount").getAsDouble();
            		
            		if(playerDonations.containsKey(playerName)) {
            			playerDonations.put(playerName, playerDonations.get(playerName) + amount);
            		} else {
            			playerDonations.put(playerName, amount);
            		}
            		
            	    // Bukkit.broadcastMessage("amount: " + amount);
            	}
            	
        		for (Entry<String, Double> entry : playerDonations.entrySet()) {
        		    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
        		        maxEntry = entry;
        		    }
        		}
        		
        		String finalPlayer = maxEntry.getKey();
            	return finalPlayer;
            	
            }
            
        } catch(Exception e) {
        	//        	Bukkit.broadcastMessage("error encountered getting stuff");
        	Bukkit.getConsoleSender().sendRawMessage(e.toString());
        }
    	
    	return "no_data";
    }
}
