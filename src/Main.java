import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserInterface ui = new ConsoleUserInterface(scanner);
    private static final DateTimeFormatter format12hour = DateTimeFormatter.ofPattern("h:mm a");
    private static final LocalTime defaultAlarmTime = LocalTime.of(12, 12, 1);
    private static final Path destinationPath = Paths.get("src", "Charli xcx - Talk talk wav.wav");

    public static void main(String[] args) {
        int selectOption = 0;
        AlarmClock ac = new AlarmClock(defaultAlarmTime, destinationPath, ui);
        Thread alarmThread = null;

        ui.displayMessage("--- Welcome to a Java Alarm Clock! ---");
        while (selectOption != 4){

            //setting up the option menu using StringBuilder
            StringBuilder sbOptionMenu = new StringBuilder();

            sbOptionMenu.append("\nPlease select one of the following options: ");

            String alarmTimeDisplay;
            String pathDisplay;

            alarmTimeDisplay = ac.getAlarmTime() == defaultAlarmTime ? "Not Set" : ac.getAlarmTimeDisplay();
            pathDisplay = ac.getAlarmSongName();

            sbOptionMenu.append("\n1. Set the alarm time. Current Alarm Time: ").append(alarmTimeDisplay);
            sbOptionMenu.append("\n2. Set the alarm song. Current Alarm Song: ").append(pathDisplay);
            sbOptionMenu.append("\n3. Start the clock and alarm.");
            sbOptionMenu.append("\n4. Stop the clock and exit. ");
            sbOptionMenu.append("\nEnter: ");

            String currentAlarmSettings = sbOptionMenu.toString();

            ui.displayMessage(currentAlarmSettings);

            try{
                selectOption = Integer.parseInt(ui.getUserInput());

                switch(selectOption){
                    case 1 -> {
                        LocalTime newAlarmTime = getUserInputForAlarmTime();
                        ac.updateAlarmTime(newAlarmTime);
                    }
                    case 2 -> {
                        Path newPath = getUserInputForAlarmSong();
                        if (newPath != null){
                            ac.updateAlarmSong(newPath);
                        }
                    }
                    case 3 -> {
                        if (ac.getAlarmTime() != defaultAlarmTime) {
                            ac.startClock();
                            alarmThread = new Thread(ac);
                            alarmThread.start();

                            try{
                                alarmThread.join();
                            }
                            catch (Exception e){
                                ui.displayMessage("Alarm thread was interrupted.");
                            }
                        }
                        else {
                            ui.displayMessage("Please make sure to set the alarm time before starting the alarm.");
                            continue;
                        }
                    }
                    case 4 -> {
                        ui.displayMessage("--- Goodbye ---");
                        if (alarmThread != null){
                            ac.stopClock();
                            alarmThread.interrupt();
                        }
                    }
                    default -> ui.displayMessage("That was not a valid option!");
                }

                if (selectOption == 4){
                    break;
                }
            }
            catch (NumberFormatException e){
                ui.displayMessage("Please only use an integer number to represent the options.");
            }
            catch(Exception e){
                ui.displayMessage("Something went wrong on the selection menu.");
            }
        }

        ui.displayMessage("Alarm clock has been turned off. Thank you!");
        scanner.close();
    }

    public static LocalTime getUserInputForAlarmTime(){
        String input;
        LocalTime alarmTime;

        ui.displayMessage("--- Set the Alarm Time ---");

        while (true){
            ui.displayMessage("Enter an alarm time in standard 12-hour format (HH:MM AM/PM, 12:00 AM): ");
            try {
                input = ui.getUserInput().toUpperCase();

                if (input.isEmpty()){
                    continue;
                }

                alarmTime = LocalTime.parse(input, format12hour);
            }
            catch(Exception e){
                ui.displayMessage("Please use the correct format for standard time (HH:MM AM/PM, 12:00 AM).");
                continue;
            }
            break;
        }

        ui.displayMessage("Alarm time set for: " + alarmTime.format(format12hour));
        return alarmTime;
    }

    public static Path getUserInputForAlarmSong(){
        String input;
        Path destinationPath = null;

        ui.displayMessage("--- Set the Alarm Song ---");

        while(true){
            ui.displayMessage("Enter the file path for a custom alarm song (.wav audio), or enter no to use the current song: ");
            try {
                input = ui.getUserInput();

                if (input.equalsIgnoreCase("no")){
                    break;
                }

                if (!input.toLowerCase().endsWith(".wav")) {
                    ui.displayMessage("Please only upload .wav files.");
                    continue;
                }

                String destination = "src/";
                destinationPath = Paths.get(destination, new File(input).getName());

                if (Files.exists(destinationPath)) {
                    ui.displayMessage("Audio File already exists at: " + destinationPath);
                } else {
                    Files.copy(Paths.get(input), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                }

                ui.displayMessage("File saved successfully to: " + destinationPath);
                break;
            }
            catch (IOException e) {
                ui.displayMessage("Error saving file: " + e.getMessage());
            }
            catch(Exception e){
                ui.displayMessage("Something went wrong when saving the file.");
            }
        }

        if (destinationPath != null){
            ui.displayMessage("Alarm song set to: " + destinationPath.getFileName());
        }

        return destinationPath;
    }

}