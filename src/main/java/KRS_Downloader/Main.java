package KRS_Downloader;

import java.util.Scanner;

/**
 * Created by Woj on 2016-12-30.
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Type filepath: ");
        Engine.start(scanner.nextLine());
    }
}