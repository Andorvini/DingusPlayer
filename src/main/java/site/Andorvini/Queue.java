package site.Andorvini;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.ArrayList;

public class Queue {

    private ArrayList<AudioTrack> trackQueue;

    public void addTrackToQueue(AudioTrack track) {
        trackQueue.add(track);
    }

    public ArrayList<AudioTrack> getQueue(){
        return trackQueue;
    }

    public static void queue() {

    }
}
