package index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class IndexBuilder {
    static Map<String, List<String>> index = new HashMap();

    public static void main(String[] args) throws IOException {
        File folder = new File("./datatest");
        buildIndex(folder);
        for (Map.Entry entry : index.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
    }

    public static void buildIndex(File folder) throws IOException {
        List<File> folderEntries = Arrays.asList(folder.listFiles());
        for (File entry : folderEntries) {
            if (entry.isDirectory()) {
                buildIndex(entry);
                continue;
            }
            String line;
            String nameFile = entry.getPath();
            nameFile = nameFile.replaceAll(".\\\\data\\\\", "");
            BufferedReader reader = new BufferedReader(new FileReader(entry));
            while ((line = reader.readLine()) != null) {
                String[] words = line.split(" ");
                List<String> listOfWords = Arrays.asList(words);
                for (String word : listOfWords) {
                    word=word.replaceAll("[,./>]","");
                    word=word.replaceAll("<br","");
                    if(word == null) listOfWords.remove(word);
                    word = word.toLowerCase(Locale.ROOT);
                    index.computeIfAbsent(word, w -> new ArrayList<>()).add(nameFile);
                }
            }
            reader.close();
        }

    }
}


