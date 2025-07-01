package aurora;

public class ForecastHistory {
    private String region;
    private int kp;
    private String time;

    public ForecastHistory(String region, int kp, String time) {
        this.region = region;
        this.kp = kp;
        this.time = time;
    }
    public String getRegion() { return region; }
    public int getKp() { return kp; }
    public String getTime() { return time; }
}
