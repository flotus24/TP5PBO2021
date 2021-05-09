/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modulgame;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.Random;

/**
 *
 * @author Fauzan
 */
public class Game extends Canvas implements Runnable{
    Window window;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    
    private int score = 0;
    private int temp_score;
    
    private int time = 10;
    private int step = 0;
    private int move = 0;
    
    private String Username;
    
    private Thread thread;
    private boolean running = false;
    private boolean alive = true;
    
    private Handler handler;
    
    public enum STATE{
        Game,
        GameOver
    };
    
    public void setUsername(String newname){
        Username = newname;
    }
    
    public STATE gameState = STATE.Game;
    
    Random random = new Random();
    dbConnection dbconn = new dbConnection();
    
    MusicStuff bgm = new MusicStuff("Blue Skies.wav");
    
    public Game(){
        window = new Window(WIDTH, HEIGHT, "Mode Single Player", this);
        
        handler = new Handler();
        
        this.addKeyListener(new KeyInput(handler, this));
        
        if(gameState == STATE.Game){
            handler.addObject(new Items(random.nextInt(700),random.nextInt(500), ID.Item));
            handler.addObject(new Items(random.nextInt(700),random.nextInt(500), ID.Item));
            handler.addObject(new Player(200,200, ID.Player));            
            handler.addObject(new Enemy(100,100, ID.Enemy));
        }

    }

    public synchronized void start(){
        thread = new Thread(this);
        thread.start();
        running = true;
    }
    
    public synchronized void stop(){
        try{
            thread.join();
            running = false;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        bgm.playMusic();    //memulai bgm
        
        while(running){
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            
            while(delta >= 1){
                tick();
                delta--;
            }
            if(running){
                render();
                frames++;
            }
            
            if(System.currentTimeMillis() - timer > 1000){
                timer += 1000;
                //System.out.println("FPS: " + frames);
                frames = 0;
                if(gameState == STATE.Game){
                    if(time>0 && alive){
                        time--;
                    }else{
                        gameState = STATE.GameOver;
                        bgm.stopMusic();    //menghentikan bgm
                        score += time;      //menambahkan nilai score dengan waktu
                        temp_score = dbconn.findScore(Username); //mengecek username ada di leaderboard atau tidak
                        if(temp_score != 9999){     
                            if(score > temp_score){              //jika iya, mengupdate score
                                dbconn.updateHS(Username, score);
                            }
                        }else{
                        dbconn.insertHS(Username, score);        //jika tidak, memasukan nama dan score   
                        }
                    }
                }
            }
        }
        stop();
    }
    
    private void tick(){
        handler.tick();
        if(gameState == STATE.Game){
            GameObject playerObject = null;
            GameObject enemyObject = null;
            for(int i=0;i< handler.object.size(); i++){
                if(handler.object.get(i).getId() == ID.Player){
                   playerObject = handler.object.get(i);
                }
                if(handler.object.get(i).getId() == ID.Enemy){
                   enemyObject = handler.object.get(i);
                }
            }
            //gerakan musuh
            if(step == 0){  //gerakan acak memilih satu arah selama sekian tick
                step = random.nextInt(24) + 1;
                move = random.nextInt(4);
            }
            if(move == 0){  //musuh akan bergerak ke atas
               enemyObject.setVel_y(-3); 
               step--;
            }
            if(move == 1){  //musuh akan bergerak ke bawah
               enemyObject.setVel_y(+3); 
               step--;
            }
            if(move == 2){  //musuh akan bergerak ke kiri
               enemyObject.setVel_x(-3); 
               step--;
            }
            if(move == 3){  //musuh akan bergerak ke kanan
               enemyObject.setVel_x(+3); 
               step--;
            }
            //System.out.println("Step = " + step);
            //System.out.println(move);
            if(playerObject != null){
                for(int i=0;i< handler.object.size(); i++){
                    //System.out.println(handler.object.size());
                    if(handler.object.get(i).getId() == ID.Item){
                        if(checkCollision(playerObject, handler.object.get(i))){    //jika musuh menabrak item
                            playSound("/Eat.wav");
                            handler.removeObject(handler.object.get(i));            
                            score = score + random.nextInt(10);
                            time = time + random.nextInt(5);
                            break;
                        }
                        if(checkCollisionEnemy(playerObject, enemyObject)){         //jika musuh menabrak musuh
                            alive = false;
                        }
                    }
                    if(handler.object.size() == 2){ //jika item habis, memunculkan kembali
                        handler.addObject(new Items(random.nextInt(700),random.nextInt(500), ID.Item));
                        handler.addObject(new Items(random.nextInt(700),random.nextInt(500), ID.Item));
                    }
                }
            }
        }
    }
    
    public static boolean checkCollision(GameObject player, GameObject item){   //mengecek tabrakan player dengan item
        boolean result = false;
        
        int sizePlayer = 50;
        int sizeItem = 20;
        
        int playerLeft = player.x;
        int playerRight = player.x + sizePlayer;
        int playerTop = player.y;
        int playerBottom = player.y + sizePlayer;
        
        int itemLeft = item.x;
        int itemRight = item.x + sizeItem;
        int itemTop = item.y;
        int itemBottom = item.y + sizeItem;
        
        if((playerRight > itemLeft ) &&
        (playerLeft < itemRight) &&
        (itemBottom > playerTop) &&
        (itemTop < playerBottom)
        ){
            result = true;
        }
        
        return result;
    }
    
    public static boolean checkCollisionEnemy(GameObject player, GameObject enemy){ //mengecek tabrakan player dengan mush
        boolean result = false;
        
        int sizePlayer = 50;
        int sizeEnemy = 50;
        
        int playerLeft = player.x;
        int playerRight = player.x + sizePlayer;
        int playerTop = player.y;
        int playerBottom = player.y + sizePlayer;
        
        int itemLeft = enemy.x;
        int itemRight = enemy.x + sizeEnemy;
        int itemTop = enemy.y;
        int itemBottom = enemy.y + sizeEnemy;
        
        if((playerRight > itemLeft ) &&
        (playerLeft < itemRight) &&
        (itemBottom > playerTop) &&
        (itemTop < playerBottom)
        ){
            result = true;
        }
        
        return result;
    }
    
    private void render(){
        BufferStrategy bs = this.getBufferStrategy();
        if(bs == null){
            this.createBufferStrategy(3);
            return;
        }
        
        Graphics g = bs.getDrawGraphics();
        
        g.setColor(Color.decode("#F1f3f3"));
        g.fillRect(0, 0, WIDTH, HEIGHT);
                
        
        
        if(gameState ==  STATE.Game){
            handler.render(g);
            
            Font currentFont = g.getFont();
            Font newFont = currentFont.deriveFont(currentFont.getSize() * 1.4F);
            g.setFont(newFont);

            g.setColor(Color.BLACK);
            g.drawString("Score: " +Integer.toString(score), 20, 20);

            g.setColor(Color.BLACK);
            g.drawString("Time: " +Integer.toString(time), WIDTH-120, 20);
        }else{
            Font currentFont = g.getFont();
            Font newFont = currentFont.deriveFont(currentFont.getSize() * 3F);
            g.setFont(newFont);

            g.setColor(Color.BLACK);
            g.drawString("GAME OVER", WIDTH/2 - 120, HEIGHT/2 - 30);

            currentFont = g.getFont();
            Font newScoreFont = currentFont.deriveFont(currentFont.getSize() * 0.5F);
            g.setFont(newScoreFont);

            g.setColor(Color.BLACK);
            g.drawString("Score: " +Integer.toString(score), WIDTH/2 - 50, HEIGHT/2 - 10);
            
            g.setColor(Color.BLACK);
            g.drawString("Press Space to Continue", WIDTH/2 - 100, HEIGHT/2 + 30);
        }
                
        g.dispose();
        bs.show();
    }
    
    public static int clamp(int var, int min, int max){
        if(var >= max){
            return var = max;
        }else if(var <= min){
            return var = min;
        }else{
            return var;
        }
    }
    
    public void close(){
        window.CloseWindow();
    }
    
    public void playSound(String filename){
        try {
            // Open an audio input stream.
            URL url = this.getClass().getResource(filename);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            // Get a sound clip resource.
            Clip clip = AudioSystem.getClip();
            // Open audio clip and load samples from the audio input stream.
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException e) {
           e.printStackTrace();
        } catch (IOException e) {
           e.printStackTrace();
        } catch (LineUnavailableException e) {
           e.printStackTrace();
        }
    
    }
}
