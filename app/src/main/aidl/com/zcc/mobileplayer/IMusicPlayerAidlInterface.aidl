// IMusicPlayerAidlInterface.aidl
package com.zcc.mobileplayer;

// Declare any non-default types here with import statements

interface IMusicPlayerAidlInterface {
    void openAudio(int position);
    void start();
    void pause();
    void stop();
    int getCurrentPosition();
    int getDuration();
    String getArtist();
    String getName();
    String getAudioPath();
    void next();
    void pre();
    void setPlayMode(int playmode);
    int getPlayMode();
    boolean isPlaying();
    void seekTo(int progress);
}
