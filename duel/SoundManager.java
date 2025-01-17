/*
 * Swapnil Kabir and Syed Bazif Shah
 * Date: December 17, 2024
 * The SoundManager class handles loading and playing sound effects and background music in the Top-Down Duel game.
 * It also manages the background music and adjusts the volume of each sound.
 */

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.*;

public class SoundManager {
    private Clip backgroundMusic;
    private Clip gameStartSound;
    private Clip pausePlaySound;
    private Clip bulletSound;
    private Clip obstacleBounceSound;
    private Clip wallBounceSound;
    private Clip bombSound;
    private Clip freezeSound;
    private Clip bigBulletSound;

    //Constructor
    public SoundManager() {
        loadSounds();
    }

    private void loadSounds() {
        try {
            //Loading all the music and sound effects
            backgroundMusic = loadClip("/audio/BackgroundMusic.wav", -30.0f);
            gameStartSound = loadClip("/audio/GameStart.wav", -10.0f);
            pausePlaySound = loadClip("/audio/PauseAndPlay.wav", -10.0f);
            bulletSound = loadClip("/audio/Bullet.wav", -10.0f);
            obstacleBounceSound = loadClip("/audio/ObstacleBounce.wav", -10.0f);
            bombSound = loadClip("/audio/Bomb.wav", -5.0f);
            freezeSound = loadClip("/audio/Freeze.wav", -5.0f);
            bigBulletSound = loadClip("/audio/BigBullet.wav", -5.0f);
        } catch (Exception e) {
            System.err.println("Error loading sounds: " + e.getMessage());
        }
    }
    //A function to load up sounds from a given url
    private Clip loadClip(String soundFile, float volume) {
        try {
            //loading clip
            URL soundURL = getClass().getResource(soundFile);
            if (soundURL == null) {
                System.err.println("Sound file not found: " + soundFile);
                return null;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            setVolume(clip, volume);
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading clip " + soundFile + ": " + e.getMessage());
            return null;
        }
    }
    //A function to set the voule
    private void setVolume(Clip clip, float volume) {
        if (clip != null) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(volume);
        }
    }
    //A specific function to change background music volume
    public void adjustBackgroundMusicVolume(boolean isMainMenu) {
        if (backgroundMusic != null) {
            float volume = isMainMenu ? -30.0f : -40.0f;  // Louder on main menu
            setVolume(backgroundMusic, volume);
        }
    }
    //Turning background music on or off
    public void playBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.setFramePosition(0);
        }
    }

    public void playGameStart() {
        playSound(gameStartSound);
    }

    public void playPauseAndPlay() {
        playSound(pausePlaySound);
    }

    public void playBulletSound() {
        playSound(bulletSound);
    }

    public void playObstacleBounce() {
        playSound(obstacleBounceSound);
    }

    public void playBombSound() {
        playSound(bombSound);
    }

    public void playFreezeSound() {
        playSound(freezeSound);
    }

    public void playBigBulletSound() {
        playSound(bigBulletSound);
    }

    private void playSound(Clip clip) {
        if (clip != null) {
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public void cleanup() {
        Clip[] clips = {
            backgroundMusic, gameStartSound, pausePlaySound, bulletSound,
            obstacleBounceSound, wallBounceSound, bombSound, freezeSound, bigBulletSound
        };
        for (Clip clip : clips) {
            if (clip != null) {
                clip.stop();
                clip.close();
            }
        }
    }
}