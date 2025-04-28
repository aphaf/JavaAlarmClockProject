import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class AlarmClock implements Runnable{

    private final UserInterface ui;

    //path to audio file for alarm song
    private Path filePath;

    //string for audio file name
    private String alarmSongName;

    //user-set alarm time
    private LocalTime alarmTime;

    //display of the alarm time in standard 12-hour format
    private String alarmTimeDisplay;

    //boolean to track if the clock is active
    private volatile boolean clockActive = true;

    //boolean to track if the alarm has triggered
    private volatile boolean alarmTriggered = false;

    //clip to store the audio for alarm song
    private Clip clip;

    //get method for alarm time
    public LocalTime getAlarmTime(){ return this.alarmTime; }

    //get method for alarmTimeDisplay (standard time string)
    public String getAlarmTimeDisplay(){ return this.alarmTimeDisplay; }

    //method to update the alarm time and alarm time display
    public void updateAlarmTime(LocalTime newAlarmTime){
        this.alarmTime = newAlarmTime;
        this.alarmTimeDisplay = convertMilitaryTimeToStandardStrHHMM(newAlarmTime);
    }

    //get method for alarm song name
    public String getAlarmSongName() { return this.alarmSongName; }

    //method to update the alarm song file path and alarm song name display
    public void updateAlarmSong(Path newPath){
        filePath = newPath;
        alarmSongName = newPath.getFileName().toString();
    }

    //constructor for alarm clock
    public AlarmClock(LocalTime alarmTime, Path filePath, UserInterface ui){
        alarmTimeDisplay = convertMilitaryTimeToStandardStrHHMM(alarmTime);
        this.alarmTime = alarmTime;
        this.filePath = filePath;
        this.alarmSongName = filePath.getFileName().toString();
        this.ui = ui;
    }

    //method to convert military time to a standard time format string (hh:mm:ss)
    private String convertMilitaryTimeToStandardStrHHMMSS(LocalTime militaryTime){

        DateTimeFormatter format12Hour = DateTimeFormatter.ofPattern("h:mm:ss a");

        return militaryTime.format(format12Hour);
    }

    //method to convert military time to a standard time format string (hh:mm)
    private String convertMilitaryTimeToStandardStrHHMM(LocalTime militaryTime){

        DateTimeFormatter format12Hour = DateTimeFormatter.ofPattern("h:mm a");

        return militaryTime.format(format12Hour);
    }

    @Override
    public void run() {
        ui.displayMessage("--- Clock ---\n");
        while(clockActive){
            try {
                //update the currentTime every second to LocalTime.now if the alarm is not active
                Thread.sleep(1000);

                LocalTime currentTime = LocalTime.now();

                if (!alarmTriggered){
                    //display the current time
                    ui.displayFormatMessage("\r" + convertMilitaryTimeToStandardStrHHMMSS(currentTime));

                    if (currentTime.truncatedTo(ChronoUnit.MINUTES).equals(alarmTime.truncatedTo(ChronoUnit.MINUTES))){
                        //if the alarmTime is equal to the currentTime (any second within the minute on the alarm time), activate the alarm
                        activateAlarm();
                    }
                }
                else{
                    //display a message to the user that the alarm has turned on
                    ui.displayMessage("\n--- Alarm!! ---\nTo turn off the alarm, enter any key: ");

                    try {
                        String input = ui.getUserInput();

                        if (!input.isEmpty()) {
                            stopAlarm();
                            ui.displayMessage("--- Alarm turned off ---");
                            stopClock();
                            break;
                        }
                    }
                    catch(Exception e){
                        ui.displayMessage("Something went wrong turning off the alarm.");
                    }
                }
            }
            catch (InterruptedException e) {
                ui.displayMessage("The alarm clock was interrupted.");
                Thread.currentThread().interrupt();
            }
            catch (Exception e){
                ui.displayMessage("Something went wrong running the alarm clock.");
            }
        }
    }

    public void startClock(){
        this.clockActive = true;
    }

    public void stopClock(){
        this.clockActive = false;
    }

    private void activateAlarm(){
        //change the alarmTriggered to true
        this.alarmTriggered = true;

        //play the custom alarm music
        playAlarmMusic();

        //play a beep sound
        Toolkit.getDefaultToolkit().beep();
    }

    public void stopAlarm(){
        alarmTriggered = false;

        //stop the custom alarm music
        stopAlarmMusic();
    }

    private void playAlarmMusic(){
        //find the file using the filePath
        File file = new File(filePath.toString());

        try(AudioInputStream audioStream = AudioSystem.getAudioInputStream(file)){
            this.clip = AudioSystem.getClip();

            //opens audio stream
            this.clip.open(audioStream);

            //start playing the song
            this.clip.start();
        }
        catch(FileNotFoundException e){
            ui.displayMessage("Could not locate audio file.");
        }
        catch(UnsupportedAudioFileException e){
            //catching audio files that aren't supported
            ui.displayMessage("Audio file is not supported.");
        }
        catch(LineUnavailableException e){
            ui.displayMessage("Unable to access audio file.");
        }
        catch(IOException e){
            ui.displayMessage("Something went wrong playing the audio.");
        }
    }

    private void stopAlarmMusic(){
        if (this.clip != null && this.clip.isRunning()) {
            //if the clip is active (alarm music is playing) then stop it when this method is called
            //stop the audio from playing
            this.clip.stop();

            //close the audio clip
            this.clip.close();
        }
    }
}