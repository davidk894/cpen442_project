package cpen442.securefileshare;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReqListActivity extends ListActivity {

    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_req_list);

        text = (TextView) findViewById(R.id.mainText);

        String s = getIntent().getStringExtra(Constants.JOBS_LIST_JSON);
        Gson gson = new Gson();
        Job[] jobs = gson.fromJson(s, Job[].class); // This works, yay! just make sure jobs class is 1:1 with response

        RequestListAdapter myAdapter = new RequestListAdapter(this, R.layout.activity_req_list_job_item);
        myAdapter.addAll(jobs);
        setListAdapter(myAdapter);
    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        super.onListItemClick(list, view, position, id);
        Job selectedItem = (Job) getListView().getItemAtPosition(position);
        text.setText("You clicked " + selectedItem.getUserID() + " at position " + position);
    }
}
