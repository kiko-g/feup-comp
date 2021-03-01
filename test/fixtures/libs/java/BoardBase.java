import java.util.Scanner;  // Import the Scanner class
public class BoardBase{

    public static int[] playerTurn(int player){
        System.out.print("Player " + player + " turn!");
        System.out.print(" Enter the row(0-2): ");
        Scanner sc = new Scanner(System.in);
        int i = sc.nextInt();
        System.out.print(" Enter the column(0-2): ");
        int j = sc.nextInt();
        int[] g = new int[2];
        g[0] = i;
        g[1] = j;
        return g;
    }
    public static void printBoard(int[] row0, int[] row1, int[] row2){
        System.out.println();
        System.out.println(row0[0]+"|"+row0[1]+"|"+row0[2]);
        System.out.println("- - -");
        System.out.println(row1[0]+"|"+row1[1]+"|"+row1[2]);
        System.out.println("- - -");
        System.out.println(row2[0]+"|"+row2[1]+"|"+row2[2]);
        System.out.println();

    }

    public static void placeTaken(){
        System.out.println("That place is taken");
    }

    public static void wrongMove(){
        System.out.println("That location is invalid");
    }

    public static void printWinner(int win){
        if (win == 0)
           System.out.println("Both of you played to a tie.");
        else {
            System.out.print("Congratulations, " + win);
            System.out.println(", you have won the game.");
        }
    }

    public static boolean sameArray(int[] row) {

        int check = row[0];
        for (int i=1; i<row.length; i++)
          if (check != row[i])
            return false;
        return true;
      }
}