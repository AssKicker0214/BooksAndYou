package asskickerproj.BooksAndYou.service;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.epub.EpubReader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Desk {

    private String bookName;
    private Book book = null;
    private List<String> blocks;
    private List<Reader> readers;
    private int blockPtrStart;
    private int blockPtrEnd;

    Desk(String id, String bookName, List<Reader> readers, int blockPtrStart, int blockPtrEnd) {
        this.id = id;
        this.bookName = bookName;
        this.readers = readers;
        this.blockPtrStart = blockPtrStart;
        this.blockPtrEnd = blockPtrEnd;
        try {
            book = new EpubReader().readEpub(new FileInputStream(DeskManager.bookDir + bookName));
            this.blocks = this.getBlocks();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(System.getProperty("user.dir"));
        }
    }

    public List<String> getBlocks() {
        try {
            List<String> texts = new LinkedList<>();
            Spine spine = book.getSpine();
            for (int i = 0; i < spine.size(); i++) {
                Resource resource = spine.getResource(i);
                if (resource.getMediaType().getName().contains("html")) {
                    // 解析html或xhtml
                    BufferedReader br = new BufferedReader(resource.getReader());
                    XmlResourceParser parser = new XmlResourceParser();
                    String line;
                    while ((line = br.readLine()) != null) {
                        parser.append(line);
                    }
                    parser.parse();
                    texts.addAll(parser.TextualElements());
                }
            }
            return texts;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    boolean readerCheckIn(String readerID) {
        for (Reader r : readers) {
            if (r.getID().equals(readerID)) {
                return true;
            }
        }
        return false;
    }

    public List<String> next() {
        return itr(400, 1);
    }

    public List<String> itr(int approximateChars, int step) {
        assert step == 1 || step == -1;
        int totalChars = 0;
        int p = step == 1 ? blockPtrEnd : blockPtrStart - 1;
        while (totalChars < approximateChars && p >= 0 && p < blocks.size()) {
            totalChars += blocks.get(p).length();
            p += step;
        }

        if(step == 1){
            // next
            blockPtrStart = blockPtrEnd;
            blockPtrEnd = p;
        }else{
            blockPtrEnd = blockPtrStart;
            blockPtrStart = p;
        }
        System.out.println(blockPtrStart+" - "+blockPtrEnd);
        return current();

    }

    public List<String> current(){
        return blocks.subList(blockPtrStart, blockPtrEnd);
    }

    public List<String> last() {
        return itr(400, -1);

    }


    public String getId() {
        return id;
    }

    private String id;

    public String getBookName() {
        return bookName;
    }

    public List<Reader> getReaders() {
        return readers;
    }

    public int getBlockPtrEnd() {
        return blockPtrEnd;
    }

    public int getBlockPtrStart(){
        return blockPtrStart;
    }
}
