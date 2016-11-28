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

            TextView statusItem = (TextView) v.findViewById(R.id.list_item_status_field);
            TextView userNameLabel = (TextView) v.findViewById(R.id.list_item_user_name_label);
            TextView userNameField = (TextView) v.findViewById(R.id.list_item_user_name_field);
            TextView contactNumberField = (TextView) v.findViewById(R.id.list_item_contact_number_field);
            TextView fileHashItem = (TextView) v.findViewById(R.id.list_item_file_hash_field);

                userNameField.setText(item.getName());
            contactNumberField.setText(item.getContactNumber());
            fileHashItem.setText(item.getFileHash());

            switch(item.getJobType()) {
                case Constants.JOB_PENDING_REQUEST: {
                    userNameLabel.setText(R.string.pending_request_name_label);
                    String text = getContext().getString(R.string.pending_request);
                    statusItem.setText(text);
                    break;
                }
                case Constants.JOB_PENDING_RESPONSE: {
                    userNameLabel.setText(R.string.pending_response_name_label);
                    String text = getContext().getString(R.string.pending_response);
                    statusItem.setText(text);
                    break;
                }
                case Constants.JOB_GOT_KEY: {
                    userNameLabel.setText(R.string.pending_request_name_label);
                    String text = getContext().getString(R.string.got_key);
                    statusItem.setText(text);
                    break;
                }
            }
            return v;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
