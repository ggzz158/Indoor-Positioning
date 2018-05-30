package com.example.lavender.wifilocation;

import java.util.ArrayList;

public class StepDisplayer {
    // 步数变量
    private int mCount = 0;
    // 应用设置
    PedometerSettings mSettings;


    public StepDisplayer(PedometerSettings settings) {
        mSettings = settings;
        notifyListener();
    }

    public int getmCount() {
        return mCount;
    }

    public void setSteps(int steps) {
        mCount = steps;
        notifyListener();
    }

    public void onStep() {
        mCount++;
        notifyListener();
    }

    public void reloadSettings() {
        notifyListener();
    }

    public interface Listener {
        public void stepsChanged(int value);
    }

    private ArrayList<Listener> mListeners = new ArrayList<Listener>();

    public void addListener(Listener l) {
        mListeners.add(l);
    }

    public void notifyListener() {
        for (Listener listener : mListeners) {
            listener.stepsChanged((int) mCount);
        }
    }

}
