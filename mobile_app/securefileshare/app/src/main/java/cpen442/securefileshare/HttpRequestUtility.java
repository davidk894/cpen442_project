package cpen442.securefileshare;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class HttpRequestUtility extends AsyncTask<Void, Void, String> {

    private String requestURL;
    private String JSONString;
    private int requestType;
    public HttpResponseUtility delegate = null;

    public static final int GET_METHOD = 0,
                            POST_METHOD = 1,
                            PUT_METHOD = 2,
                            HEAD_METHOD = 3,
                            DELETE_METHOD = 4,
                            TRACE_METHOD = 5,
                            OPTIONS_METHOD = 6;


    public static final int TIMEOUT = 60 * 1000;

    public HttpRequestUtility(HttpResponseUtility delegate) {
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(Void... params) {
        String response = "";
        try {
            URL url = new URL(this.getRequestURL());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            this.setRequestMethod(conn);
            conn.setConnectTimeout(HttpRequestUtility.TIMEOUT);
            conn.setReadTimeout(HttpRequestUtility.TIMEOUT);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            this.setHeaderData(conn);
            this.setJSONParams(conn);
            conn.connect();

            if (conn.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                response = this.getResponse(conn.getInputStream());
            } else {
                response = this.getResponse(conn.getErrorStream());
            }
            conn.disconnect();

        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        if(delegate != null) {
            delegate.processResponse(result);
        }
    }

    // #region setters and getters
    private String getRequestURL() {
        return this.requestURL;
    }
    public void setRequestURL(String s) {
        this.requestURL = s;
    }

    private int getRequestType() {
        return this.requestType;
    }
    public void setRequestType(int type) {
        this.requestType = type;
    }

    private String getJSONString() {
        return this.JSONString;
    }
    public void setJSONString(String params) {
        this.JSONString = params;
    }
    // #endregion setters and getters

    // #region request building
    private void setHeaderData(HttpURLConnection conn) throws IOException {
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
    }

    private void setRequestMethod(HttpURLConnection conn) throws ProtocolException {
        switch(this.getRequestType()) {
            case HttpRequestUtility.POST_METHOD:
                conn.setRequestMethod("POST");
                break;
            default:
                // do nothing for now
                break;
        }
    }
    private void setJSONParams(HttpURLConnection conn) throws IOException {
        if (this.getJSONString() != null) {
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(this.getJSONString());
            writer.flush();
            writer.close();
            os.close();
        }
    }

    private String getResponse(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuffer response = new StringBuffer();
        String line;
        while((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }
    // #endregion request building

    public interface HttpResponseUtility {
        void processResponse(String response);
    }

}
