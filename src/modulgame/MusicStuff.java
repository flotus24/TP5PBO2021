/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package modulgame;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JOptionPane;
/**
 *
 * @author Asus
 */
public class MusicStuff {
    private File musicPath;
    private AudioInputStream audioInput;
    private Clip clip;

    public MusicStuff(){
    }

    public MusicStuff(String musicLocation){
        musicPath = new File(musicLocation);
        if(musicPath.exists()){
            try{
                audioInput = AudioSystem.getAudioInputStream(musicPath);
                clip = AudioSystem.getClip();
                clip.open(audioInput);
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }
        else{
            System.out.println("Can't find file");
        }
    }
 
    public void playMusic(){
        try{
            clip.start();
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void stopMusic(){
        try{
            clip.stop();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
