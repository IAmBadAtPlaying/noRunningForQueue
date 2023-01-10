import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.json.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConnectionManager {
    enum conOptions {
        GET ("GET"),
        POST ("POST"),
        PATCH("PATCH"),
        DELETE ("DELETE"),
        PUT ("PUT");

        final String name;
        conOptions(String name) {
            this.name = name;
        }

        public static conOptions getByString(String s) {
            if (s == null) return null;
            switch (s) {
                case "GET":
                    return conOptions.GET;
                case "POST":
                    return conOptions.POST;
                case "PATCH":
                    return conOptions.PATCH;
                case "DELETE":
                    return conOptions.DELETE;
                case "PUT":
                    return conOptions.PUT;
                default:
                    return null;
            }
        }
    }
    enum responseFormat {
        STRING (0),
        SUMMONER (1),
        SUMMONER_ARRAY (2),
        LOOT (3),
        LOOT_ARRAY (4),
        IMAGE (5),
        JSON_OBJECT (6),
        JSON_ARRAY (7);

        final Integer id;
        responseFormat(Integer id) {
            this.id = id;
        }
    }

    public String conError = "Error: Connection cant be established";
    public String conRespError = "Error: Response Code Error";
    public String[] lockfileContents = null;
    public String authString = null;
    public String preUrl = null;
    public WebSocketClient client = null;
    public MainInitiator mainInitiator = null;

    public String version = null;

    public HashMap<String, Integer> ChampHash = new HashMap<>();

    public ConnectionManager(MainInitiator mainInitiator) {
        this.preUrl = null;
        this.authString = null;
        this.mainInitiator = mainInitiator;
        this.version = getLatestVersion();
    }


    public void fireParameterChanged(String input) {
        System.out.println(input);
    }

    public void init() {
        buildChampMap();
        InputStream is = null;
        File lockfile = null;
        try{
            for(ProcessHandle p : ProcessHandle.allProcesses().toList()) {
                if (p.info().command().isPresent()) {
                    String current = p.info().command().get();
                    if(current!=null && current.contains("LeagueClient.exe")) {
                        lockfile = new File(current.substring(0, current.lastIndexOf(System.getProperty("file.separator"))+1)+"lockfile");
                        break;
                    }
                }
            }
            if(lockfile == null) {
                System.out.println("League is not running, will not start!");
                return;
            }
            is = new FileInputStream(lockfile);
            if (is == null) {
                return;
            }
            String result = inputStreamToString(is);
            if (result != null) {
                this.lockfileContents = result.split(":");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Port: "+ lockfileContents[2]);
        System.out.println("Auth: "+ lockfileContents[3]);
        this.preUrl = "https://127.0.0.1:" + lockfileContents[2];
        this.authString = "Basic " + Base64.getEncoder().encodeToString(("riot:" + lockfileContents[3]).trim().getBytes());
    }


    public String inputStreamToString(InputStream is) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                result.append(line).append("\n");
            }
            br.close();
        }
        return result.toString();
    }

    public boolean buildPatchRequest(String path, String patchBody) {
        boolean success = false;
        try {
            HttpClient client1 = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(preUrl+path))
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(patchBody))
                    .header("Content-Type", "application/json")
                    .header("Authorization", authString)
                    .build();
            client1.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body).join();

            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public void buildChampMap() {
        try {
            String s = version;
            URL url = new URL("https://ddragon.leagueoflegends.com/cdn/"+s+"/data/en_US/champion.json");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            JSONObject resp = (JSONObject) getResponse(responseFormat.JSON_OBJECT, con);
            if (resp!=null) {
                System.out.println("Initializing Champ Hash Map for Version \"" + s + "\"");
                JSONObject champs = resp.getJSONObject("data");
                Iterator<String> keys = champs.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONObject current = champs.getJSONObject(key);
                    this.ChampHash.put(current.getString("name"), current.getInt("key"));
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /*private static void allowHttpMethods(String... methods) {
        try {
            Field declaredFieldMethods = HttpURLConnection.class.getDeclaredField("methods");
            Field declaredFieldModifiers = Field.class.getDeclaredField("modifiers");
            declaredFieldModifiers.setAccessible(true);
            declaredFieldModifiers.setInt(declaredFieldMethods, declaredFieldMethods.getModifiers() & ~Modifier.FINAL);
            declaredFieldMethods.setAccessible(true);
            String[] previous = (String[]) declaredFieldMethods.get(null);
            Set<String> current = new LinkedHashSet<>(Arrays.asList(previous));
            current.addAll(Arrays.asList(methods));
            String[] patched = current.toArray(new String[0]);
            declaredFieldMethods.set(null, patched);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }*/

    public HttpURLConnection buildConnection(conOptions options,String path , String post_body) {
        try {
            URL clientLockfileUrl = new URL(preUrl + path);
            HttpURLConnection con = (HttpURLConnection) clientLockfileUrl.openConnection();
            if (con == null || !(con instanceof HttpURLConnection)) {
                System.out.println(clientLockfileUrl.toString());
            }
            con.setRequestMethod(options.name);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", authString);
            if (conOptions.POST.equals(options) || conOptions.PUT.equals(options)) {
                if (post_body == null) {post_body = "";}
                con.setDoOutput(true);
                con.getOutputStream().write(post_body.getBytes(StandardCharsets.UTF_8));
            }
            return con;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public HttpURLConnection buildConnection(conOptions options, String path) {
        try {
            URL clientLockfileUrl = new URL(preUrl + path);
            HttpURLConnection con = (HttpURLConnection) clientLockfileUrl.openConnection();
            if (con == null || !(con instanceof HttpURLConnection)) {
                System.out.println(clientLockfileUrl.toString());
                return null;
            }
            con.setRequestMethod(options.name);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", authString);
            return con;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object getResponse(responseFormat respFormat, HttpURLConnection con) {
        switch (respFormat) {
            case STRING:
                return handleStringResponse(con);
            case LOOT:

                break;
            case JSON_ARRAY:
                return handleJSONArrayResponse(con);
            case JSON_OBJECT:
                return handleJSONObjectResponse(con);
            case SUMMONER:
                return handleSummonerResponse(con);
            case SUMMONER_ARRAY:
                return handleSummonerArrayResponse(con);
            case LOOT_ARRAY:
                return handleLootArrayResponse(con);
            case IMAGE:
                return handleImageResponse(con);
            default:
                return handleStringResponse(con);
        }
        return null;
    }

    public String getLatestVersion() {
        String ver = null;
        try {
            URL url = new URL("https://ddragon.leagueoflegends.com/api/versions.json");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            JSONArray resp = (JSONArray) getResponse(responseFormat.JSON_ARRAY, con);
            con.disconnect();
            if(resp == null) return null;
            ver = resp.getString(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ver;
    }

    private LootElement[] handleLootArrayResponse (HttpURLConnection con) {
        String resp = null;
        JSONArray jsonLootArray = null;
        try {
            resp = handleStringResponse(con);
            jsonLootArray = toJsonArray(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (resp != null && jsonLootArray != null) {
            LootElement[] lootArray = new LootElement[jsonLootArray.length()];
            for (int i = 0; i < jsonLootArray.length(); i++) {
                LootElement loot = new LootElement(jsonLootArray.getJSONObject(i).getString("lootId"));
                loot.setCount(jsonLootArray.getJSONObject(i).getInt("count"));
                loot.setDisenchantLootName(jsonLootArray.getJSONObject(i).getString("disenchantLootName"));
                loot.setSplashPath(jsonLootArray.getJSONObject(i).getString("splashPath"));
                loot.setValue(jsonLootArray.getJSONObject(i).getInt("value"));
                lootArray[i] = loot;
            }
            return lootArray;
        }

        return null;
    }

    private JSONObject handleJSONObjectResponse (HttpURLConnection con) {
        return toJsonObject(handleStringResponse(con));
    }

    private Summoner[] handleSummonerArrayResponse(HttpURLConnection con) {
        String resp = null;
        JSONArray jsonSummonerArray = null;
        Summoner[] summonerArray = null;
        try {
            resp = handleStringResponse(con);
            jsonSummonerArray = toJsonArray(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (resp != null && jsonSummonerArray != null) {
            summonerArray = new Summoner[jsonSummonerArray.length()];
            for (int i = 0, j = jsonSummonerArray.length(); i < j; i++ ) {
                Summoner summoner = new Summoner(jsonSummonerArray.getJSONObject(i).getString("puuid"));
                summoner.setSummonerId(jsonSummonerArray.getJSONObject(i).getInt("summonerId"));
                summoner.setSummonerLevel(jsonSummonerArray.getJSONObject(i).getInt("summonerLevel"));
                summoner.setDisplayName(jsonSummonerArray.getJSONObject(i).getString("summonerName"));
                summoner.setInternalName(jsonSummonerArray.getJSONObject(i).getString("summonerInternalName"));
                summoner.setProfileIconId(jsonSummonerArray.getJSONObject(i).getInt("summonerIconId"));
                summoner.setFirstPositionPreference(jsonSummonerArray.getJSONObject(i).getString("firstPositionPreference"));
                summoner.setSecondPositionPreference(jsonSummonerArray.getJSONObject(i).getString("secondPositionPreference"));

                summonerArray[i] = summoner;
            }
        }
        return summonerArray;
    }

    private JSONArray handleJSONArrayResponse (HttpURLConnection con) {

        return toJsonArray(handleStringResponse(con));
    }

    private Summoner handleSummonerResponse(HttpURLConnection con) {
        String resp = null;
        JSONObject jsonSummoner = null;
        try {
            resp = handleStringResponse(con);
            jsonSummoner = toJsonObject(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SummonerFromJsonObject(jsonSummoner);
    }

    public Summoner SummonerFromJsonObject(JSONObject jsonSummoner) {
        if(jsonSummoner == null) return null;
        Summoner summoner = new Summoner(jsonSummoner.getString("puuid"));
        summoner.setSummonerId(jsonSummoner.getInt("summonerId"));
        summoner.setSummonerLevel(jsonSummoner.getInt("summonerLevel"));
        if(jsonSummoner.has("summonerName")) {
            summoner.setDisplayName(jsonSummoner.getString("summonerName"));
        }
        if(jsonSummoner.has("displayName")) {
            summoner.setDisplayName(jsonSummoner.getString("internalName"));
        }
        if(jsonSummoner.has("summonerInternalName")) {
            summoner.setInternalName(jsonSummoner.getString("summonerInternalName"));
        }
        if(jsonSummoner.has("internalName")) {
            summoner.setInternalName(jsonSummoner.getString("internalName"));
        }
        if(jsonSummoner.has("summonerIconId")) {
            summoner.setProfileIconId(jsonSummoner.getInt("summonerIconId"));
        }
        if(jsonSummoner.has("profileIconId")) {
            summoner.setProfileIconId(jsonSummoner.getInt("profileIconId"));
        }
        if(jsonSummoner.has("firstPositionPreference")) {
            summoner.setFirstPositionPreference(jsonSummoner.getString("firstPositionPreference"));
            summoner.setSecondPositionPreference(jsonSummoner.getString("secondPositionPreference"));
        }
        if(jsonSummoner.has("percentCompleteForNextLevel")) {
            summoner.setPercentCompleteForNextLevel(jsonSummoner.getInt("percentCompleteForNextLevel"));
        }
        return summoner;
    }

    private BufferedImage handleImageResponse (HttpURLConnection con) {
        BufferedImage resp = null;
        try {
            InputStream is = con.getInputStream();
            resp = ImageIO.read(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resp;
    }

    private JSONArray toJsonArray(String s) {
        if(s == null) return null;
        return new JSONArray(s);
    }
    private JSONObject toJsonObject(String s) {
        if(s == null) return null;
        return new JSONObject(s);
    }

    public String handleStringResponse(HttpURLConnection conn) {
        String resp = null;
        try {
            if (100 <= conn.getResponseCode() && conn.getResponseCode() <= 399) {
                resp = inputStreamToString(conn.getInputStream());
            } else {
                resp = inputStreamToString(conn.getErrorStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return resp;
    }

    public String inviteIntoLobby(String summonerName) {
        String resp = null;
        Integer summonerId = null;
        resp = handleStringResponse(buildConnection(conOptions.GET, "/lol-summoner/v1/summoners?name=" + summonerName, null));
        if(resp == null || resp.isEmpty()) {
            return conError;
        }
        JSONObject jsonResp = new JSONObject(resp);
        if(jsonResp.has("summonerId")) {
            summonerId = jsonResp.getInt("summonerId");
            HttpURLConnection con = buildConnection(conOptions.POST, "/lol-lobby/v2/lobby/invitations" , "[{\"toSummonerId\": "+summonerId+",\"toSummonerName\":\""+summonerName+"\"}]");
            if (con == null) return conError;
            try {
                Integer respCode = con.getResponseCode();
                if(con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    System.out.println("inviteIntoLobby-Error: Expected HTTP_OKAY (200) got: " + respCode);
                    return "Error: Invitation failed";
                }
                return "Success! (WIP)";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return conRespError;
    }

    public String updatePlayerPreferences(Integer firstEntry, Integer secondEntry, Integer thirdEntry, String titleId) {
        String reqBody;
        reqBody = "{\"challengeIds\":["+firstEntry+","+secondEntry+","+thirdEntry+"],\"title\" : \""+titleId+"\"}";
        HttpURLConnection con = buildConnection(conOptions.POST, "/lol-challenges/v1/update-player-preferences", reqBody);
        if (con == null) return conError;
        try {
            Integer respCode = con.getResponseCode();
            con.disconnect();
            if(respCode== HttpURLConnection.HTTP_NO_CONTENT) {
                return "Success";
            }
            System.out.println("updatePlayerPreferences-Error: Expected HTTP_NO_CONTENT (204) but got: " + respCode);
            System.out.println(handleStringResponse(con));
            return "Error: You dont own either title or id";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conRespError;
    }

    public String updateProfileIcon(Integer iconId) {
        String reqBody = "{\"profileIconId\":"+iconId+"}";
        HttpURLConnection con = buildConnection(conOptions.PUT, "/lol-summoner/v1/current-summoner/icon", reqBody);
        if (con == null) return conError;
        try {
            Integer respCode = con.getResponseCode();
            con.disconnect();
            switch (respCode) {
                case HttpURLConnection.HTTP_CREATED:
                    return "Success!";
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    System.out.println("updateProfileIcon-Error: Expected HTTP_CREATED (201) but got: " +respCode);
                    System.out.println("The user probably doesnt own the icon");
                    return "Error: You dont own this icon!";
                default:
                    System.out.println("updateProfileIcon-Error: Expected HTTP_CREATED (201) but got: " +respCode);
                    System.out.println("The user probably entered an invalid IconId");
                    return "Error: Invalid Icon ID!";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conRespError;
    }

}
