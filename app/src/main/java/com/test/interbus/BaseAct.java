package com.test.interbus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.interbus.BusCallback;
import com.github.interbus.InterBus;

/***
 *   created by android on 2019/4/8
 */
public class BaseAct extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("=====","===2222=="+this.getClass().hashCode());
    }
}
