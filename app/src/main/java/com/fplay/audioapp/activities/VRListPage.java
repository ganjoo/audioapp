package com.fplay.audioapp.activities;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.fplay.audioapp.R;
import com.fplay.audioapp.adapter.VRAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;


@EActivity(R.layout.activity_vrlist_page)
public class VRListPage extends AppCompatActivity {


    ListView list;

    String[] vrtitles ={
            "Emu","Deer",
            "Giraffe","Rhino"

    };

    String[] data ={
            "https://s3.ap-south-1.amazonaws.com/zoopictures/zoo+videos/Emu_h264_short.mp4","https://s3.ap-south-1.amazonaws.com/zoopictures/zoo+videos/deer_h264_short.mp4",
            "https://s3.ap-south-1.amazonaws.com/zoopictures/zoo+videos/giraffe_h264_short.mp4","https://s3.ap-south-1.amazonaws.com/zoopictures/zoo+videos/rhino_h264_short.mp4"
    };

    Integer[] imgid={
            R.drawable.emu,R.drawable.spotted_deer,
            R.drawable.girrafe, R.drawable.rhino
    };


    @AfterViews
    public  void afterViews() {
        VRAdapter adapter=new VRAdapter(this, vrtitles, data,imgid);
        list=(ListView)findViewById(R.id.list);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                if(position == 0) {
                    //code specific to first list item
                    Toast.makeText(getApplicationContext(),"Place Your First Option Code",Toast.LENGTH_SHORT).show();
                }

                else if(position == 1) {
                    //code specific to 2nd list item
                    Toast.makeText(getApplicationContext(),"Place Your Second Option Code",Toast.LENGTH_SHORT).show();
                }

                else if(position == 2) {

                    Toast.makeText(getApplicationContext(),"Place Your Third Option Code",Toast.LENGTH_SHORT).show();
                }
                else if(position == 3) {

                    Toast.makeText(getApplicationContext(),"Place Your Forth Option Code",Toast.LENGTH_SHORT).show();
                }
                else if(position == 4) {

                    Toast.makeText(getApplicationContext(),"Place Your Fifth Option Code",Toast.LENGTH_SHORT).show();
                }

            }
        });
        ImageButton btn_back = (ImageButton)findViewById(R.id.btn_back_vr);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

}
