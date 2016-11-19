package cpen442.securefileshare;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {


    /***
     * Note: finish() -- used to exit this activity
     * Activities are stored on a stack, so when MainActivity started this activity
     * MainActivity is paused in background and added to stack, with this in foreground
     * finish() finishes this activity and goes back to previous activity on stack.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final Button encryptBtn = (Button) findViewById(R.id.btn_encrypt);
        encryptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something
            }
        });

        final Button decryptBtn = (Button) findViewById(R.id.btn_decrypt);
        decryptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something
            }
        });

        final Button reqListBtn = (Button) findViewById(R.id.btn_viewlist);
        reqListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something
            }
        });
    }

}
