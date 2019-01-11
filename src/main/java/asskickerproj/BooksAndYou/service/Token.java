package asskickerproj.BooksAndYou.service;

public class Token {
    public static final int VALID_FORMAT =  0b100;
    public static final int VALID_DESK =    0b010;
    public static final int VALID_READER =  0b001;
    public static final int VALID =         0b111;

    private String deskID;
    private String readerID;
    private int validation = 0;

    public Token(String token){
        check(token);
    }

    private void check(String token){
        String[] segs = token.split("-");
        if(segs.length != 2){
            // 检查token格式
            this.validation = 0b000;
        }else{
            int v = VALID_FORMAT;
            this.deskID = segs[0];
            this.readerID = segs[1];

            // 检查书桌是否存在
            if (DeskManager.existDesk(this.deskID)){
                v |= VALID_DESK;
            }

            // 检查读者是否存在
            if (DeskManager.existReader(this.deskID, this.readerID)){
                v |= VALID_READER;
            }
            this.validation = v;
        }

    }

    public int getValidationResult(){
        return this.validation;
    }

    public boolean isValid(){
        return this.validation == VALID;
    }

    public String getDeskID(){
        return this.deskID;
    }

    public String getReaderID(){
        return this.readerID;
    }
}
