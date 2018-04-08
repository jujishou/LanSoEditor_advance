package com.example.advanceDemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.example.advanceDemo.cool.ParticleDemoActivity;
import com.example.advanceDemo.cool.VViewImage3DDemoActivity;
import com.example.advanceDemo.cool.VideoEffectDemoActivity;
import com.lansoeditor.advanceDemo.R;

public class ListCoolDemoActivity extends Activity implements OnClickListener {

    private String videoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.list_cool_demo_layout);
        videoPath = getIntent().getStringExtra("videopath");

        findViewById(R.id.id_cool_image3d).setOnClickListener(this);
        findViewById(R.id.id_cool_particle).setOnClickListener(this);
        findViewById(R.id.id_cool_videoeffect).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_cool_image3d:
                startDemoActivity(VViewImage3DDemoActivity.class);
                break;
            case R.id.id_cool_particle:
                startDemoActivity(ParticleDemoActivity.class);
                break;
            case R.id.id_cool_videoeffect:
                startDemoActivity(VideoEffectDemoActivity.class);
                break;
            default:
                break;
        }
    }

    private void startDemoActivity(Class<?> cls) {
        Intent intent = new Intent(ListCoolDemoActivity.this, cls);
        intent.putExtra("videopath", videoPath);
        startActivity(intent);
    }

}
