import java.util.Scanner;

public class ConsoleUserInterface implements UserInterface{

    private final Scanner scanner;

    public ConsoleUserInterface(Scanner scanner){
        this.scanner = scanner;
    }

    @Override
    public void displayMessage(String message) {
        System.out.print("\n" + message);
    }

    @Override
    public void displayFormatMessage(String message) {System.out.printf(message); }

    @Override
    public String getUserInput() {
        String userInput = scanner.nextLine().trim();
        return userInput;
    }
}
