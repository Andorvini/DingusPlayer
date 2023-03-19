package site.andorvini.gson.sseblo;

import java.util.ArrayList;

public class SsebloVoicesMain {
    private String requestID;
    private int cacheTime;
    private ArrayList<SsebloVoicesData> data;

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public int getCacheTime() {
        return cacheTime;
    }

    public void setCacheTime(int cacheTime) {
        this.cacheTime = cacheTime;
    }

    public ArrayList<SsebloVoicesData> getData() {
        return data;
    }

    public void setData(ArrayList<SsebloVoicesData> data) {
        this.data = data;
    }
}

class SsebloVoicesData {

}

//class SsebloVoices