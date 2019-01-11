package asskickerproj.BooksAndYou.service;

public class Reader {
    public static final int DIRECTION_UNKNOWN = 0b000;
    public static final int DIRECTION_NEXT = 0b001;
    public static final int DIRECTION_CURRENT = 0b010;
    public static final int DIRECTION_PREVIOUS = 0b100;

    private String id;
    private boolean seated;
    private int voting = DIRECTION_UNKNOWN;

    public Reader(String id){
        this.id = id;
    }

    public void sit(){
        this.seated = true;
    }

    public void leave(){
        this.seated = false;
    }

    public String getID(){
        return id;
    }

    public int getVoting(){
        return voting;
    }

    public void clearVoting(){
        this.voting = DIRECTION_UNKNOWN;
    }

    public int vote(String direction){
        assert direction.equals("current") || direction.equals("next") || direction.equals("previous");
        switch (direction){
            case "next":    this.voting = DIRECTION_NEXT;   break;
            case "previous":    this.voting = DIRECTION_PREVIOUS; break;
            default:    this.voting = DIRECTION_CURRENT;
        }
        return this.voting;
    }
}
