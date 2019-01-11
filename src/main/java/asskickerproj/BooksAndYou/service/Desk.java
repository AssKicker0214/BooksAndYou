package asskickerproj.BooksAndYou.service;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.epub.EpubReader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Desk {

    private String bookName;
    private Book book = null;
    private List<String> blocks;
    private List<Reader> readers;
    private int blockPtrStart;
    private int blockPtrEnd;

    private String directionVoting = "current";

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

    /**
     * @param readerID
     * @param direction
     * @return 投票是否通过
     */
    public boolean vote(String readerID, String direction) {
        // 落实读者投票
        readers.stream().filter(reader -> reader.getID().equals(readerID)).forEach(targetReader -> targetReader.vote(direction));

        // 检查投票结果
        int voting = readers.stream().map(Reader::getVoting).reduce((v1, v2) -> v1 & v2).get();

        // 不一致的投票结果，或是还有读者未投票
        if (voting == Reader.DIRECTION_UNKNOWN) {
            return false;
        } else {
            readers.forEach(Reader::clearVoting);
            if(voting == Reader.DIRECTION_NEXT){
                this.next();
            }else if(voting == Reader.DIRECTION_PREVIOUS){
                this.previous();
            }
            return true;
        }

        /*// 执行翻页，并初始化Reader的投票选项
        switch (voting) {
            case Reader.DIRECTION_CURRENT:
                return this.current();
            case Reader.DIRECTION_PREVIOUS:
                return this.previous();
            case Reader.DIRECTION_NEXT:
                return this.next();
            default:
                return null;
        }*/
    }

    public List<String> next() {
        return itr(300, 1);
    }


    public List<String> current() {
        return blocks.subList(blockPtrStart, blockPtrEnd);
    }

    public List<String> previous() {
        return itr(300, -1);

    }

    public List<String> skip(int pages, String direction){
        List<String> rs = new ArrayList<>();
        assert direction.equals("next") || direction.equals("previous");
        int step = direction.equals("next") ? 1 : -1;
        for(int i=0;i<pages;i++){
            rs = this.itr(300, step);
        }
        return rs;
    }

    public List<String> itr(int approximateChars, int step) {
        assert step == 1 || step == -1;
        int totalChars = 0;
        int p = step == 1 ? blockPtrEnd : blockPtrStart - 1;
        while (totalChars < approximateChars && p >= 0 && p < blocks.size()) {
            totalChars += blocks.get(p).length();
            p += step;
        }

        if (step == 1) {
            // next
            blockPtrStart = blockPtrEnd;
            blockPtrEnd = p;
        } else {
            blockPtrEnd = blockPtrStart;
            blockPtrStart = p;
        }
        System.out.println(blockPtrStart + " - " + blockPtrEnd);
        return current();

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

    public int getBlockPtrStart() {
        return blockPtrStart;
    }
}
