package cpen442.securefileshare;

import android.app.ListActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ReqListActivity extends ListActivity {

    private TextView text;
    private List<String> listValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_req_list);

        text = (TextView) findViewById(R.id.mainText);

        listValues = new ArrayList<>();
        listValues.add("Android");
        listValues.add("iOS");
        listValues.add("Symbian");
        listValues.add("BB");

        ArrayAdapter<String> myAdapter = new ArrayAdapter<>(this,
                R.layout.activity_req_row_layout, R.id.listText, listValues);

        setListAdapter(myAdapter);
    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        super.onListItemClick(list, view, position, id);
        String selectedItem = (String) getListView().getItemAtPosition(position);
        text.setText("You clicked " + selectedItem + " at position " + position);
    }
}
