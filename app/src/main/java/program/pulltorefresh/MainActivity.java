package program.pulltorefresh;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private PullToRefresh ptr;
    private ArrayList<String> list;
    private ArrayAdapter adapter;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    adapter.notifyDataSetChanged();
                    ptr.completeRefresh();
                    break;
                case 2:
                    adapter.notifyDataSetChanged();
                    ptr.completeRefresh();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ptr = ((PullToRefresh) findViewById(R.id.ptr));
        list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add("yy"+i);
        }
        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        ptr.setAdapter(adapter);


        ptr.setListener(new PullToRefresh.Listener() {
            @Override
            public void onPullRefresh() {
                list.add("sss");
                handler.sendEmptyMessage(1);
            }

            @Override
            public void onDownRefresh() {
                list.add("aaa");
                handler.sendEmptyMessage(2);
            }
        });
    }
}
