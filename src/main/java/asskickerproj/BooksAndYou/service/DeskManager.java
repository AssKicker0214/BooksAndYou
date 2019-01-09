package asskickerproj.BooksAndYou.service;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DeskManager {
    private static Map<String, Desk> desks = new ConcurrentHashMap<>();
    public static final String logDir = "src/main/resources/desks/";
    public static final String bookDir = "src/main/resources/books/";

    public static synchronized Desk getDesk(String deskID) {
        if (!desks.containsKey(deskID)) {
            Desk desk = loadDesk(deskID);
            if (desk != null) desks.put(deskID, desk);
            else return null;
        }
        return desks.get(deskID);

    }

    public static boolean existDesk(String deskID) {
        File[] logs = new File(logDir).listFiles();
        assert logs != null;
        for (File log : logs) {
            if (log.getName().equals(deskID + ".desk")) {
                return true;
            }
        }
        return false;
    }

    public static boolean existReader(String deskID, String readerID) {
        Desk tmpDesk = loadDesk(deskID);
        if (tmpDesk != null) {
            return tmpDesk.readerCheckIn(readerID);
        }
        return false;
    }


    /**
     * 从`.desk`文件加载`Desk`实例，如果没有指定`deskID`的配置文件，返回`null`
     *
     * @param deskID
     * @return
     */
    private static Desk loadDesk(String deskID) {
        File dir = new File(logDir);
        File[] logs = dir.listFiles();
        assert logs != null;
        try {
            for (File deskLog : logs) {
                if (deskLog.getName().equals(deskID + ".desk")) {
                    BufferedReader br = new BufferedReader(new FileReader(deskLog));

                    // #1. 书名
                    String bookName = br.readLine();

                    // #2. 注册读者（以`;`分割）
                    String[] readerIDs = br.readLine().split(";");
                    ArrayList<Reader> readers = new ArrayList<>();
                    for (String rid : readerIDs) {
                        readers.add(new Reader(rid));
                    }

                    // #3. 阅读位置
                    String[] range = br.readLine().split("-");
                    int ptrStart = Integer.parseInt(range[0]);
                    int ptrEnd = Integer.parseInt(range[1]);
                    br.close();
                    return new Desk(deskID, bookName, readers, ptrStart, ptrEnd);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void dumpDesk(Desk desk) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(logDir + desk.getId() + ".desk"));
            bw.write(String.format(
                    "%s\n%s\n%d-%d\n",
                    desk.getBookName(),
                    desk.getReaders().stream().map(Reader::getID).collect(Collectors.joining(";")),
                    desk.getBlockPtrStart(),desk.getBlockPtrEnd()

            ));
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
