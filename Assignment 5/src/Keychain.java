import BasicIO.*;
import java.util.LinkedList;
/*COSC 1P03
 *ASSIGNMENT #5
 *Username: Ty Mabee
 *Student #: 7453301
 *Version: 2.0
 *
 *This is the starting point of a program for finding one's keys in a 'floor plan' data file.
 *You're provided with the code for loading the data file, and just need to fill in the recursive
 *algorithm. Make sure to include feedback on e.g. unsolveable floor plans!
 *
 *You've also been provided with three sample files, though many more are possible:
 *floor1.txt solves simply
 *floor2.txt requires quite a bit more 'backtracking'
 *floor3.txt is unsolveable
 */

/**
 * VERSION 2.0 PATCH NOTES
 *
 * ADDED backtrack METHOD THAT FINDS ITS WAY BACK INSTEAD OF SNAPPING TO STARTING POSITION
 * NOTE: THE USE OF THIS METHOD MAKES THE recursiveSolve METHOD LESS EFFICIENT IN IT'S PATH MAKING
 */

public class Keychain {
    //Array Declaration
    char[][] plans=null;
    LinkedList<String> moves = new LinkedList<>(); //Stores which moves were made
    //Instance variables
    int startRow=-1;//Starting row for 'Me'
    int startCol=-1;//Starting column for 'Me'
    //Any of the blocked commands check to see if there is something in the way of the corresponding direction
    boolean blockedUp = false;
    boolean blockedRight = false;
    boolean blockedLeft = false;
    boolean blockedDown = false;
    int tries = 0; //Keeps track to create breakpoint, avoiding stackoverflow (happens at try 3497)
    int backTries = 0; //Same as tries but for back track

    public Keychain() {
        
    }
    
    /**
     * Basic code for loading the data file into the array.
     */
    private void loadFloorPlans() {
        int height,width;
        String filename;
        ASCIIDataFile file=new ASCIIDataFile();
        String temp;
        
        height=file.readInt();
        width=file.readInt();
        plans=new char[height][width];
        for (int i=0;i<height;i++) {
            temp=file.readLine();
            plans[i]=temp.toCharArray();
            if (temp.indexOf('M')>=0) {
                startRow=i;startCol=temp.indexOf('M');
                System.out.println("Start at "+startRow+","+startCol+".");
            }
        }
        System.out.println("File transcription complete!\n");
        solve();
    }

    private void solve() {
        int currentCol = startCol;
        int currentRow = startRow;
        System.out.println("Initial State:");
        printFloorPlans();

        //Using currentRow and currentCol to keep track of location
        if (recursiveSolve(currentRow, currentCol)) {
            System.out.println("\nFinal Layout:");
            printFloorPlans();
            String path = makePath(); // Easier to read
            System.out.print("Findeth yon keys: " + path); //Directions for getting to my keys
        }
        else {
            System.out.println("\nOh no! The keys are lost to us!");
            printFloorPlans(); //Displaying anyway, since we presumably modified the floor plans
        }
        System.exit(0);
    }

    /**
     * Method that gathers all the moves used and puts them together in a sentence, with punctiation
     *
     * @return  a sentence of all moves made by algorithm
     */
    private String makePath(){
        StringBuilder path = new StringBuilder();
        for (int i = 0; i < moves.size(); i++){
            path.append(moves.get(i) + ", ");
        }
        path.replace(path.length()-2, path.length()-1, ".");
        return path.toString();
    }

    /**
     * Main method
     * Takes in the currentRow and currentCol of the map, and finds the keys using if statements and recusion
     *
     * @param currentRow    the current row, or y coordinate of M (me)
     * @param currentCol    the current column, or x coordinate of M (me)
     * @return  three possibilites to return
     *          1) true = key was found
     *          2) false = un-findable (floor3)
     *          3) recursiveSolve(currentRow, currentCol) = the same function, with new coordinates
     */
    //Note, I will only be breaking down Up in detail, as the rest are exactly the same, just with right, down and left movements instead
    private boolean recursiveSolve(int currentRow, int currentCol) {
        tries++;
        String up = Character.toString(plans[currentRow-1][currentCol]); //Needed a string to use .equal()
        //If up is an obstacle or has been traveled before, you can't go there
        if (up.equals("O") || up.matches("[0-3]")){
            blockedUp = true;
        //Else, move there
        }else{
            currentRow--; //Update coordinates
            plans[currentRow][currentCol] = '1'; //Change S to a 1
            moves.add("Up"); //Adds Up as a movement used
            //If the key was above the original location
            if (up.equals("K")){
                return true; //Finish the program
            }
            blockedReset(0); //Since we moved, we will reset the blocked conditions put on last time, this allows backtracking
        }
        //If up was blocked, try moving right
        if (blockedUp){
            String right = Character.toString(plans[currentRow][currentCol+1]);
            if (right.equals("O") || right.matches("[0-3]")){
                blockedRight = true;
            }else{
                currentCol++;
                plans[currentRow][currentCol] = '2';
                moves.add("Right");
                if (right.equals("K")){
                    return true;
                }
                blockedReset(1);
            }
        }
        //If Up and Right were blocked, try down
        if (blockedRight){
            String down = Character.toString(plans[currentRow+1][currentCol]);
            if (down.equals("O") || down.matches("[0-3]")){
                blockedDown = true;
            }else{
                currentRow++;
                plans[currentRow][currentCol] = '3';
                moves.add("Down");
                if (down.equals("K")){
                    return true;
                }
                blockedReset(2);
            }
        }
        //If Up, Right and Down were blocked, try left
        if (blockedDown){
            String left = Character.toString(plans[currentRow][currentCol-1]);
            if (left.equals("O") || left.matches("[0-3]")){
                blockedLeft = true;
            }else{
                currentCol--;
                plans[currentRow][currentCol] = '4';
                moves.add("Left");
                if (left.equals("K")){
                    return true;
                }
                blockedReset(3);
            }
        }
        //If they were all blocked, do one of two things
        if (blockedDown && blockedLeft && blockedRight && blockedUp){
            //If you've done 50 moves and have not found the key
            //Reset your past moves so that you can try to find another path
            if (tries >= 50){
                for (int x = 0; x < plans.length; x++){
                    for (int y = 0; y < plans[0].length; y++){
                        if (Character.toString(plans[x][y]).matches("[0-3]")){
                            plans[x][y] = 'S';
                        }
                    }
                }
            //Otherwise, return to the starting position (backtrack) and try another path
            }else {
                int[] reset = backtrack(currentCol, currentRow);
                currentCol = reset[0];
                currentRow = reset[1];
                moves.clear(); //clear the moves taken, as they're not necessary to finding your keys
            }
        }
        //If for some reason you hit 100 tries and don't find the keys, consider them unfindable
        //Stack overflow prevention
        if (tries>=100) {
            return false;
        }
        return recursiveSolve(currentRow, currentCol); //The only recursive call needed, currentRow and col were already adjusted
    }

    /**
     * Helper method that backtracks from a given point and returns an array containing the starting column and row, in that order
     * New in version 2.0
     *
     * @param currentCol    column the algorithm got stuck in
     * @param currentRow    row the algorithm got stuck in
     * @return  returns int[] with {startingCol, startingRow} used by recursive solve
     *
     * Note: the use of this backtracking method makes the recursive solve less efficient
     */
    private int[] backtrack(int currentCol, int currentRow){
        backTries++;
        String direction = Character.toString(plans[currentRow][currentCol]); //Needed a string to use .equal()
        plans[currentRow][currentCol] = 'X'; // Makes the curernt spot X so solve won't go that way again
        // If block that is the same as recursive solve, just finds a way back out using path made by recursive solve
        if (direction.equals("3") || direction.equals("M")){
            currentRow--;
        }else if (direction.equals("4") || direction.equals("M")){
            currentCol++;
        }else if (direction.equals("1") || direction.equals("M")){
            currentRow++;
        }else if (direction.equals("2") || direction.equals("M")){
            currentCol--;
        }
        //If if finds the starting position, it can quit
        //If not, as long as you can find it in 150 tries, keep going
        //If you can't, give up
        if (currentCol == startCol && currentRow == startRow){
            int [] answer = {currentCol, currentRow};
            backTries = 0;
            return answer;
        }else{
            if (backTries == 150){
                System.out.println("Could not find starting position");
                return null;
            }
            return backtrack(currentCol, currentRow);
        }
    }

    /**
     * Method used to reset the blocked state
     * Depneding on caseNum, will reset different blocked states
     *
     * @param caseNum   specified number used to determine which states to reset
     */
    private void blockedReset(int caseNum){
        switch (caseNum){
            //Up case
            case 0:
                blockedRight = false;
                blockedDown = false;
                blockedLeft = false;
                break;
            //Right case
            case 1:
                blockedUp = false;
                blockedDown = false;
                blockedLeft = false;
                break;
            //Down case
            case 2:
                blockedLeft = false;
                blockedUp = false;
                blockedRight = false;
                break;
            //Left case
            case 3:
                blockedUp = false;
                blockedRight = false;
                blockedDown = false;
                break;
            //Everything was blocked
            case 4:
                blockedUp = false;
                blockedRight = false;
                blockedDown = false;
                blockedLeft = false;
        }
    }
    
    private void printFloorPlans() {
        for (char[] row:plans) {
            for (char c:row) System.out.print(c);
            System.out.println();
        }
    }
    
    public static void main(String args[]) {new Keychain().loadFloorPlans();}
}
