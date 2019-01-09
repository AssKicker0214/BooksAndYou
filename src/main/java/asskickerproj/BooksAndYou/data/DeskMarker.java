package asskickerproj.BooksAndYou.data;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DeskMarker implements Serializable {
    protected static String bookDir = "src/main/resources/books/";
    protected static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String bookName;
    private String deskToken;
    private Date time;

    public void save(){
        this.time = new Date();
        String content = String.format(
                "%s\n" +
                "%s\n" +
                "%s\n",
                bookName,deskToken,sdf
        );
        try {
            FileWriter fw = new FileWriter(bookDir+deskToken+".desk");
            fw.write(content);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DeskMarker load(String token){
        try {
            BufferedReader br = new BufferedReader(new FileReader(bookDir+token+".desk"));
            DeskMarker dm = new DeskMarker();
            dm.setBookName(br.readLine());
            dm.setDeskToken(br.readLine());
            dm.setTime(br.readLine());
            return dm;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getDeskToken() {
        return deskToken;
    }

    public void setDeskToken(String deskToken) {
        this.deskToken = deskToken;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(String time) {
        try {
            this.time = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
