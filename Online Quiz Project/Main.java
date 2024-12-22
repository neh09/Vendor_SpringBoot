import java.time.LocalTime;
import java.util.function.Function;

public class Main {

    public static void main(String[] args) {
        LocalTime currentTime = LocalTime.now();

        // Lambda to add hours
        Function<Integer, LocalTime> addHours = hours -> currentTime.plusHours(hours);
        
        // Lambda to add minutes
        Function<Integer, LocalTime> addMinutes = minutes -> currentTime.plusMinutes(minutes);
        
        // Lambda to add seconds
        Function<Integer, LocalTime> addSeconds = seconds -> currentTime.plusSeconds(seconds);

        // Testing the lambdas
        System.out.println("Current time: " + currentTime);
        System.out.println("After adding 2 hours: " + addHours.apply(2));
        System.out.println("After adding 30 minutes: " + addMinutes.apply(30));
        System.out.println("After adding 45 seconds: " + addSeconds.apply(45));
    }
}