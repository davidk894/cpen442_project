package cpen442.securefileshare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RequestListAdapter extends ArrayAdapter<Job> {

    private int layoutResourceId;

    public RequestListAdapter(Context context, int resourceId) {
        super(context, resourceId);
        layoutResourceId = resourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            Job item = getItem(position);
            View v;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater)
                        getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(layoutResourceId, null);
            } else {
                v = convertView;
            }

            TextView userIdItem = (TextView) v.findViewById(R.id.list_item_user_id);
            TextView fileHashItem = (TextView) v.findViewById(R.id.list_item_file_hash);

            userIdItem.setText(item.getUserID());
            fileHashItem.setText(item.getFileHash());
            // Add text components here by using v.findViewBy(blah).
            return v;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
