public class Summoner {
    private Integer accountId = null;
    private String displayName = null;
    private String internalName = null;
    private Integer percentCompleteForNextLevel = null;
    private Integer profileIconId = null;
    final private String puuid;
    private Integer summonerId = null;
    private Integer summonerLevel = null;
    private Integer xpSinceLastLevel = null;
    private Integer xpUntilLastLevel = null;
    private String firstPositionPreference = null;
    private String secondPositionPreference = null;

    public Summoner(String puuid) {
        this.puuid = puuid;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getInternalName() {
        return internalName;
    }

    public Integer getProfileIconId() {
        return profileIconId;
    }

    public String getPuuid() {
        return puuid;
    }

    public Integer getSummonerId() {
        return summonerId;
    }

    public Integer getSummonerLevel() {
        return summonerLevel;
    }

    public Integer getXpSinceLastLevel() {
        return xpSinceLastLevel;
    }

    public Integer getXpUntilLastLevel() {
        return xpUntilLastLevel;
    }

    public Integer getPercentCompleteForNextLevel() {
        return percentCompleteForNextLevel;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public void setPercentCompleteForNextLevel(Integer percentCompleteForNextLevel) {
        this.percentCompleteForNextLevel = percentCompleteForNextLevel;
    }

    public void setProfileIconId(Integer profileIconId) {
        this.profileIconId = profileIconId;
    }

    public void setSummonerId(Integer summonerId) {
        this.summonerId = summonerId;
    }

    public void setSummonerLevel(Integer summonerLevel) {
        this.summonerLevel = summonerLevel;
    }

    public void setXpSinceLastLevel(Integer xpSinceLastLevel) {
        this.xpSinceLastLevel = xpSinceLastLevel;
    }

    public void setXpUntilLastLevel(Integer xpUntilLastLevel) {
        this.xpUntilLastLevel = xpUntilLastLevel;
    }

    public String getFirstPositionPreference() {
        return firstPositionPreference;
    }

    public void setFirstPositionPreference(String firstPositionPreference) {
        this.firstPositionPreference = firstPositionPreference;
    }

    public String getSecondPositionPreference() {
        return secondPositionPreference;
    }

    public void setSecondPositionPreference(String secondPositionPreference) {
        this.secondPositionPreference = secondPositionPreference;
    }
}
