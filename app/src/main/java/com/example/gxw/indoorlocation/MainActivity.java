package com.example.gxw.indoorlocation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button Collect;
    private Button setloca;
    private Button PlanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Collect=findViewById(R.id.button);
        setloca=findViewById(R.id.button2);
        PlanButton=findViewById(R.id.button3);
        //监听事件
        Collect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(MainActivity.this,"这是信息采集啊",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(MainActivity.this,Collect.class);
                startActivity(intent);
            }
        });

        setloca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(MainActivity.this,"这是定位啊",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(MainActivity.this,setloca.class);
                startActivity(intent);

            }
        });

        PlanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(MainActivity.this,"路径规划",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(MainActivity.this,PlanButton.class);
                startActivity(intent);
            }
        });

    }
}
