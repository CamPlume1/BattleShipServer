package game;

import java.util.*;

public class PlayerImp implements Player {
    /**
     * Board owned by the user represented by this program
     */
    protected List<Ship> fleet;
    /**
     * Board owned by the opponent of the user represented by this program
     */
    protected CellStatus[][] OpponentBoard;

    private static final String SERVER_NAME = "SERVER_AGENT";

    protected List<Coord> possibleShots;

    @Override
    public String name() {
        return SERVER_NAME;
    }

    /**
     * Recieves a list of opponents shots from the previous round. Updates board, and replies with a list of shots
     * for the new round
     * @param shots The Shots fired by opponent
     * @return  Shots Fired back at opponent, not exceeding the size of the number of boats alive on board
     */
    @Override
    public List<Coord> salvo(List<Coord> shots) {
        for (Ship s : this.fleet){
            for (Coord c : shots){
                s.receiveShot(c);
            }
        }
        int acc = 0;
        for (Ship s: this.fleet){
            if (!s.isSunk()){
                acc ++;
            }
        }
        return this.generateShots(acc);
    }


    public List<Coord> generateShots(int number) {
        List<Coord> retList = new ArrayList<>();
        Random r = new Random();
        for (int i = 0; i < number; i++){
            Coord currentCoord;
            if(this.possibleShots.isEmpty()) {
                return new ArrayList<>();
            }
            currentCoord = this.possibleShots.remove(r.nextInt(this.possibleShots.size()));
            retList.add(currentCoord);
        }
        for (Coord shot : retList){
            OpponentBoard[shot.y()][shot.x()] = CellStatus.SPLASH;
        }
        return retList;
    }


    protected boolean validShot(Coord c){
        return this.OpponentBoard[c.y()][c.x()] == CellStatus.EMPTY;
    }


    @Override
    public List<Ship> setup(int height, int width, Map<ShipType, Integer> spec) {
        Map<ShipType, Integer> specifications = new HashMap<>(spec);
        this.OpponentBoard = new CellStatus[width][height];
        this.possibleShots = new ArrayList<>();
        for(int row = 0; row < OpponentBoard.length; row ++){
            for (int col = 0; col < OpponentBoard[0].length; col++){
                OpponentBoard[row][col] = CellStatus.EMPTY;
                this.possibleShots.add(new Coord(col, row));
            }
        }
        this.fleet = new ArrayList<>();
        this.placeBoats(specifications);
        return this.fleet; //Format Ship list into format expected by server

    }


    //Given a fleet from the server, place boats
    private void placeBoats(Map<ShipType, Integer> boats) {

        //Initialize a hashmap of ship types paired to lengths
        Map<ShipType, Integer> reference = new HashMap<>();
        reference.put(ShipType.CARRIER, 6);
        reference.put(ShipType.BATTLESHIP, 5);
        reference.put(ShipType.DESTROYER, 4);
        reference.put(ShipType.SUBMARINE, 3);
        List<Ship> temp = new ArrayList<>();

        //Create initial set of Ship objects
        for (ShipType s : boats.keySet()) {
            for (int i = 0; i < boats.getOrDefault(s, 0); i++) {
                temp.add(new Ship(reference.get(s)));
            }
        }
        Random r = new Random();
        List<Dir> allDirs = new ArrayList<>(Arrays.asList(Dir.VERTICAL, Dir.HORIZONTAL));

        //Repeatedly place ships at random valid locations until all ships are placed.
        //WORKING STUB VERSION
//        for (int i = 0; i < temp.size(); i++) {
//            Ship s = temp.get(i);
//            s.place(new Coord(0, i), Dir.HORIZONTAL);
//            fleet.add(s);
//        }
        // END WORKING STUB VERSION
        for (Ship s : temp) {
            boolean flag = false;
            while (!flag) {
                System.out.println(this.name() + " looking for ships");
                int x = r.nextInt(this.OpponentBoard[0].length);
                int y = r.nextInt(this.OpponentBoard.length);
                Dir dir = allDirs.get(r.nextInt(2));
                s.place(new Coord(x, y), dir);
                if (this.validCoords(s)) {
                    flag = true;
                    for (Ship s2 : this.fleet) {
                        if (s.isColliding(s2)) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag)
                        fleet.add(s);
                    }
                }
            }
        }


    private boolean validCoords(Ship s) {
        return s.getStartPoint().x() >= 0 && s.getEndpoint().y() >= 0 && s.getStartPoint().y() < OpponentBoard.length
                && s.getEndpoint().x() < OpponentBoard[0].length;
    }

    /**
     * Process Hits. These hits represent shots fired by this player in the previous salvo that hit boats.
     * 0 indexed.
     * @param shots the shots
     */
    @Override
    public void hits(List<Coord> shots) {
        for (Coord shot : shots) {
            this.OpponentBoard[shot.y()][shot.x()] = CellStatus.HIT;
        }
    }

    @Override
    public void endGame(GameResult result, String reason) {
        System.out.println(result + ": " + reason);
    }
}
