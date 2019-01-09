package asskickerproj.BooksAndYou.service;

public class Reader {

    private String id;
    private boolean seated;

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
}
